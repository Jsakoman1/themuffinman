package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.agent.service.LocalAdminAgentPromptTranslator;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class VisionPromptUnderstandingService {

    private final AgentProperties agentProperties;
    private final LocalAdminAgentPromptTranslator localAdminAgentPromptTranslator;
    private final VisionSemanticMapper visionSemanticMapper;
    private final PromptSemanticsSupport promptSemanticsSupport;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    public VisionPromptUnderstandingService(
            AgentProperties agentProperties,
            LocalAdminAgentPromptTranslator localAdminAgentPromptTranslator,
            VisionSemanticMapper visionSemanticMapper,
            PromptSemanticsSupport promptSemanticsSupport
    ) {
        this.agentProperties = agentProperties;
        this.localAdminAgentPromptTranslator = localAdminAgentPromptTranslator;
        this.visionSemanticMapper = visionSemanticMapper;
        this.promptSemanticsSupport = promptSemanticsSupport;
    }

    public VisionPromptUnderstandingResult understandPrompt(String prompt, VisionConversation conversation) {
        if (prompt == null || prompt.isBlank()) {
            throw ServiceErrors.badRequest("Prompt is required");
        }

        String trimmed = prompt.trim();
        if (!isConfigured()) {
            return localUnderstand(trimmed, conversation);
        }

        try {
                return openAiUnderstand(trimmed, conversation);
        } catch (RestClientException exception) {
            return localUnderstand(trimmed, conversation);
        } catch (RuntimeException exception) {
            return localUnderstand(trimmed, conversation);
        }
    }

    private VisionPromptUnderstandingResult openAiUnderstand(String prompt, VisionConversation conversation) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", agentProperties.getModel());
        payload.put("input", buildInput(prompt, conversation));
        payload.put("reasoning", Map.of("effort", "low"));
        payload.put("text", Map.of("verbosity", "low"));

        String responseBody;
        try {
            responseBody = restClient.post()
                    .uri(agentProperties.getBaseUrl() + "/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException exception) {
            throw exception;
        }

        JsonNode response = parseResponseBody(responseBody);
        String outputText = extractOutputText(response);
        if (outputText == null || outputText.isBlank()) {
            throw ServiceErrors.badRequest("OpenAI vision understanding response did not include usable text output");
        }

        try {
            VisionPromptUnderstandingResult understanding = objectMapper.readValue(outputText, VisionPromptUnderstandingResult.class);
            understanding.setOriginalPrompt(prompt);
            understanding.setTranslationProvider(providerName());
            if (understanding.getNormalizedPrompt() == null || understanding.getNormalizedPrompt().isBlank()) {
                understanding.setNormalizedPrompt(prompt);
            } else {
                understanding.setNormalizedPrompt(understanding.getNormalizedPrompt().trim());
            }
            if (understanding.getSourceLanguage() == null || understanding.getSourceLanguage().isBlank()) {
                understanding.setSourceLanguage("unknown");
            }
            if (understanding.getSemanticPlan() == null) {
                understanding.setSemanticPlan(VisionSemanticPlan.from(promptSemanticsSupport.inferPlan(understanding.getNormalizedPrompt())));
            }
            visionSemanticMapper.applyFallbackFocus(understanding, conversation);
            return understanding;
        } catch (Exception exception) {
            throw ServiceErrors.badRequest("OpenAI vision understanding parsing failed: " + exception.getMessage());
        }
    }

    private VisionPromptUnderstandingResult localUnderstand(String prompt, VisionConversation conversation) {
        var translation = localAdminAgentPromptTranslator.translateForPlanning(prompt);
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .sourceLanguage(translation.getSourceLanguage())
                .originalPrompt(prompt)
                .normalizedPrompt(translation.getTranslatedPrompt().trim())
                .translationProvider(translation.getTranslationProvider())
                .focusSlotId(null)
                .focusSlotConfidence(null)
                .semanticPlan(VisionSemanticPlan.from(promptSemanticsSupport.inferPlan(translation.getTranslatedPrompt())))
                .translationApplied(translation.isTranslationApplied())
                .translationReliable(translation.isTranslationReliable())
                .slots(new VisionPromptUnderstandingSlots())
                .build();
        visionSemanticMapper.applyFallbackFocus(understanding, conversation);
        return understanding;
    }

    private boolean isConfigured() {
        return "openai".equalsIgnoreCase(agentProperties.getProvider())
                && agentProperties.getApiKey() != null
                && !agentProperties.getApiKey().isBlank();
    }

    private String providerName() {
        return "openai";
    }

    private String buildInput(String prompt, VisionConversation conversation) {
        String slotDataJson = "{}";
        String requestedSlot = "";
        if (conversation != null) {
            try {
                slotDataJson = objectMapper.writeValueAsString(conversation.getSlotData());
            } catch (Exception ignored) {
                slotDataJson = "{}";
            }
            requestedSlot = conversation.getRequestedSlot() == null ? "" : conversation.getRequestedSlot();
        }

        return """
                You extract structured planning metadata and create_quest fields from a user prompt for a backend vision system.
                Return valid JSON only.
                Do not invent values.
                First identify the candidate intent and capability, then extract create_quest slots only when the candidate intent is CREATE_QUEST.
                If the user is looking for open work, browsing quests, or asking what they could do next, use DISCOVER_QUESTS and extract a short searchQuery.
                If the user wants to open or start a chat with another person, use OPEN_CHAT and extract a short targetUserQuery with the person's username, email, or name fragment.
                Supported candidateIntent values are CREATE_QUEST, DISCOVER_QUESTS, OPEN_CHAT, and UNSUPPORTED.
                Supported capabilityId values are create_quest, discover_quests, open_chat, and unsupported.
                Only extract values that are explicitly stated or strongly and directly implied by the user's words.
                Keep the normalizedPrompt concise and in English when possible.
                If the candidate intent is DISCOVER_QUESTS, use searchQuery to capture the concrete topic the user wants to browse, such as moving help, delivery, or odd jobs.
                If the candidate intent is OPEN_CHAT, use targetUserQuery to capture the person the user wants to chat with.
                Do not include generic discovery words like find, browse, show, open, available, jobs, work, or quests in searchQuery unless the user explicitly named them as the topic.
                Use these slot ids only when the user explicitly provided the information:
                questTitle, questTitleConfidence, questDescription, questDescriptionConfidence, visibility, visibilityConfidence, reward.freeQuest, reward.freeQuestConfidence, reward.amount, reward.amountConfidence, schedule.mode, schedule.modeConfidence, schedule.scheduledDate, schedule.scheduledDateConfidence, schedule.scheduledTime, schedule.scheduledTimeConfidence, location.mode, location.modeConfidence, location.label, location.labelConfidence, location.candidateConfirmation, location.candidateConfirmationConfidence.
                Also choose exactly one focusSlotId when one slot is the main target of the turn, or null when the turn is only a general statement.
                Use these focus slot ids only: quest_title, quest_description, reward_amount, visibility, schedule_mode, scheduled_date, scheduled_time, location_mode, location_label, location_candidate_confirmation.
                If the prompt says free, no pay, unpaid, or similar, set reward.freeQuest to true and reward.amount to "0".
                If the prompt does not explicitly mention a reward amount, leave reward.amount null.
                If the prompt does not explicitly mention a slot, leave that field null.
                Use confidence values from 0.0 to 1.0.
                Set confidence close to 1.0 only for fields the prompt states directly.
                Set confidence below 0.75 for any field that is only inferred, so the backend can ignore it.
                If the prompt says the user's profile/current location should be used, set location.mode to "profile".
                If the prompt says the location should be hidden, set location.mode to "off".
                If the prompt says a custom place/address, set location.mode to "custom" and include location.label with the typed place exactly as stated.
                If the prompt clearly confirms a location candidate, set location.candidateConfirmation to "resolved" or "typed".
                If the prompt indicates fixed timing, set schedule.mode to "fixed".
                If the prompt includes a calendar day, put it in schedule.scheduledDate using yyyy-MM-dd when possible.
                If the prompt includes a time of day, put it in schedule.scheduledTime using HH:mm 24-hour local time when possible.
                If the prompt indicates a flexible or by-agreement timing, set schedule.mode to "agreement" and leave schedule.scheduledDate and schedule.scheduledTime null.

                Current requested slot: %s
                Current slot data: %s

                Output JSON with this shape:
                {
                  "sourceLanguage": "en",
                  "normalizedPrompt": "concise normalized English prompt",
                  "translationApplied": true,
                  "translationReliable": true,
                    "semanticPlan": {
                    "candidateIntent": "OPEN_CHAT",
                    "candidateIntentConfidence": 0.95,
                    "capabilityId": "open_chat",
                    "planningNote": "The user wants to open a chat with another person.",
                    "targetUserQuery": "Josip"
                  },
                  "focusSlotId": "location_mode",
                  "focusSlotConfidence": 1.0,
                  "slots": {
                    "questTitle": "Move a sofa",
                    "questTitleConfidence": 1.0,
                    "questDescription": "Need help moving a sofa",
                    "questDescriptionConfidence": 1.0,
                    "visibility": "PUBLIC",
                    "visibilityConfidence": 1.0,
                    "reward": {
                      "freeQuest": false,
                      "freeQuestConfidence": 1.0,
                      "amount": "25",
                      "amountConfidence": 1.0
                    },
                    "schedule": {
                      "mode": "fixed",
                      "modeConfidence": 1.0,
                      "scheduledDate": "2026-07-03",
                      "scheduledDateConfidence": 1.0,
                      "scheduledTime": "14:30",
                      "scheduledTimeConfidence": 1.0
                    },
                    "location": {
                      "mode": "custom",
                      "modeConfidence": 1.0,
                      "label": "Ban Jelacic Square",
                      "labelConfidence": 1.0,
                      "candidateConfirmation": null,
                      "candidateConfirmationConfidence": 0.0
                    }
                  }
                }

                Prompt:
                %s
                """.formatted(requestedSlot, slotDataJson, prompt);
    }

    private JsonNode parseResponseBody(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw ServiceErrors.badRequest("OpenAI response body was empty");
        }

        try {
            return objectMapper.readTree(responseBody);
        } catch (Exception exception) {
            throw ServiceErrors.badRequest("OpenAI response parsing failed: " + exception.getMessage());
        }
    }

    private String extractOutputText(JsonNode response) {
        if (response == null || response.isNull()) {
            return null;
        }

        JsonNode outputTextNode = response.get("output_text");
        if (outputTextNode != null && outputTextNode.isTextual()) {
            return outputTextNode.asText();
        }

        JsonNode outputNode = response.get("output");
        if (outputNode == null || !outputNode.isArray()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (JsonNode item : outputNode) {
            JsonNode contentNode = item.get("content");
            if (contentNode == null || !contentNode.isArray()) {
                continue;
            }

            for (JsonNode contentItem : contentNode) {
                JsonNode textNode = contentItem.get("text");
                if (textNode != null && textNode.isTextual()) {
                    if (!builder.isEmpty()) {
                        builder.append("\n\n");
                    }
                    builder.append(textNode.asText().trim());
                } else if (textNode != null && textNode.isObject()) {
                    JsonNode valueNode = textNode.get("value");
                    if (valueNode != null && valueNode.isTextual()) {
                        if (!builder.isEmpty()) {
                            builder.append("\n\n");
                        }
                        builder.append(valueNode.asText().trim());
                    }
                }
            }
        }

        return builder.isEmpty() ? null : builder.toString();
    }
}
