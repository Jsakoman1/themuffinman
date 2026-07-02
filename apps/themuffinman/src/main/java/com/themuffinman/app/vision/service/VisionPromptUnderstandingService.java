package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.agent.service.LocalAdminAgentPromptTranslator;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.identity.model.AppUser;
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
    private final VisionSemanticOrchestrationContextService semanticContextService;
    private final VisionSemanticRouteCatalogService semanticRouteCatalogService;
    private final VisionSemanticContractSanitizer semanticContractSanitizer;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    public VisionPromptUnderstandingService(
            AgentProperties agentProperties,
            LocalAdminAgentPromptTranslator localAdminAgentPromptTranslator,
            VisionSemanticMapper visionSemanticMapper,
            PromptSemanticsSupport promptSemanticsSupport,
            VisionSemanticOrchestrationContextService semanticContextService,
            VisionSemanticRouteCatalogService semanticRouteCatalogService,
            VisionSemanticContractSanitizer semanticContractSanitizer
    ) {
        this.agentProperties = agentProperties;
        this.localAdminAgentPromptTranslator = localAdminAgentPromptTranslator;
        this.visionSemanticMapper = visionSemanticMapper;
        this.promptSemanticsSupport = promptSemanticsSupport;
        this.semanticContextService = semanticContextService;
        this.semanticRouteCatalogService = semanticRouteCatalogService;
        this.semanticContractSanitizer = semanticContractSanitizer;
    }

    public VisionPromptUnderstandingResult understandPrompt(String prompt, VisionConversation conversation) {
        return understandPrompt(prompt, conversation, conversation == null ? null : conversation.getOwner(), null);
    }

    public VisionPromptUnderstandingResult understandPrompt(String prompt, VisionConversation conversation, AppUser currentUser) {
        return understandPrompt(prompt, conversation, currentUser, null);
    }

    public VisionPromptUnderstandingResult understandPrompt(
            String prompt,
            VisionConversation conversation,
            AppUser currentUser,
            VisionSemanticRuntimeHints runtimeHints
    ) {
        if (prompt == null || prompt.isBlank()) {
            throw ServiceErrors.badRequest("Prompt is required");
        }

        String trimmed = prompt.trim();
        VisionSemanticOrchestrationRequest orchestrationRequest = buildOrchestrationRequest(trimmed, conversation, currentUser, runtimeHints);
        if (!isConfigured()) {
            return localUnderstand(trimmed, conversation);
        }

        try {
                return openAiUnderstand(orchestrationRequest, conversation);
        } catch (RestClientException exception) {
            return localUnderstand(trimmed, conversation);
        } catch (RuntimeException exception) {
            return localUnderstand(trimmed, conversation);
        }
    }

    private VisionPromptUnderstandingResult openAiUnderstand(VisionSemanticOrchestrationRequest orchestrationRequest, VisionConversation conversation) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", agentProperties.getModel());
        payload.put("input", buildInput(orchestrationRequest));
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
            understanding.setOriginalPrompt(orchestrationRequest.getRawPrompt());
            understanding.setTranslationProvider(providerName());
            if (understanding.getNormalizedPrompt() == null || understanding.getNormalizedPrompt().isBlank()) {
                understanding.setNormalizedPrompt(orchestrationRequest.getRawPrompt());
            } else {
                understanding.setNormalizedPrompt(understanding.getNormalizedPrompt().trim());
            }
            if (understanding.getSourceLanguage() == null || understanding.getSourceLanguage().isBlank()) {
                understanding.setSourceLanguage("unknown");
            }
            if (understanding.getSemanticPlan() == null) {
                understanding.setSemanticPlan(VisionSemanticPlan.from(promptSemanticsSupport.inferPlan(understanding.getNormalizedPrompt())));
            }
            semanticContractSanitizer.sanitize(understanding, orchestrationRequest.getAllowedRoutes());
            visionSemanticMapper.applyFallbackFocus(understanding, conversation);
            return understanding;
        } catch (Exception exception) {
            throw ServiceErrors.badRequest("OpenAI vision understanding parsing failed: " + exception.getMessage());
        }
    }

    private VisionSemanticOrchestrationRequest buildOrchestrationRequest(
            String prompt,
            VisionConversation conversation,
            AppUser currentUser,
            VisionSemanticRuntimeHints runtimeHints
    ) {
        AppUser effectiveUser = currentUser != null
                ? currentUser
                : conversation == null ? null : conversation.getOwner();
        return VisionSemanticOrchestrationRequest.builder()
                .contractVersion("vision-semantic-orchestration-v1")
                .rawPrompt(prompt)
                .userContext(semanticContextService.buildUserContext(effectiveUser, runtimeHints))
                .conversationContext(semanticContextService.buildConversationContext(conversation))
                .runtimeContext(semanticContextService.buildRuntimeContext(runtimeHints))
                .allowedRoutes(semanticRouteCatalogService.allowedRoutes(effectiveUser))
                .responseContract(responseContract())
                .build();
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

    private String buildInput(VisionSemanticOrchestrationRequest orchestrationRequest) {
        String orchestrationRequestJson;
        try {
            orchestrationRequestJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(orchestrationRequest);
        } catch (Exception exception) {
            throw ServiceErrors.badRequest("Vision semantic orchestration request serialization failed: " + exception.getMessage());
        }
        return """
                You are the semantic orchestration layer for a backend-governed vision system.
                Return valid JSON only.
                Do not invent values.
                Use only the allowedRoutes in the request. Do not create new routes, DTO fields, capabilities, slot ids, or execution permissions.
                The backend will validate all values and decide whether execution is allowed.
                Use userContext for language expectations, local phrasing, and timezone-aware date/time interpretation.
                userContext.preferredLocaleSource and userContext.timezoneSource tell you whether values came from runtime hints, configured defaults, or country fallback.
                runtimeContext describes whether the turn came from text or voice and what the client runtime reported.
                Use conversationContext for the current requested slot and already collected slot data.
                First identify the candidate intent and capability, then extract create_quest slots only when the selected route intent is CREATE_QUEST.
                If the selected route intent is CREATE_CIRCLE, only extract circleName when the user explicitly gave the circle name.
                If the user wants to send or create a circle request, use CREATE_CIRCLE_REQUEST and extract only targetUserQuery when the user stated it.
                If the user wants to accept an incoming circle request, use ACCEPT_CIRCLE_REQUEST and extract only targetUserQuery when the user stated it.
                If the user wants to decline or cancel a pending circle request, use DELETE_CIRCLE_REQUEST and extract only targetUserQuery when the user stated it.
                If the user wants to apply to a quest or job, use CREATE_APPLICATION and extract only applicationQuestQuery, applicationMessage, and applicationProposedPrice when the user stated them.
                If the user wants to update one of their pending applications, use UPDATE_APPLICATION and extract applicationQuestQuery plus any new applicationMessage or applicationProposedPrice the user explicitly stated.
                If the user wants to withdraw one of their pending applications, use WITHDRAW_APPLICATION and extract only applicationQuestQuery when the user stated it.
                If the user wants to approve or decline an applicant on their own quest, use APPROVE_APPLICATION or DECLINE_APPLICATION and extract applicationQuestQuery plus a short targetUserQuery for the applicant when the user stated them.
                If the user wants to rename or delete one of their own circles, use UPDATE_CIRCLE or DELETE_CIRCLE and extract targetCircleQuery plus circleName only when the user stated the replacement name.
                If the user wants to update their own profile username or description, use UPDATE_PROFILE and extract only profileUsername and profileDescription when the user stated them.
                If the user wants to update their own profile location, use UPDATE_PROFILE_LOCATION and extract only profileLocationMode and profileLocationLabel when the user stated them.
                If the user is looking for open work, browsing quests, or asking what they could do next, use DISCOVER_QUESTS and extract a short searchQuery.
                If the user wants to open or start a chat with another person, use OPEN_CHAT and extract a short targetUserQuery with the person's username, email, or name fragment.
                If the user asks to see their own profile, use VIEW_PROFILE.
                If the user asks to see their own circles or network, use VIEW_CIRCLES.
                If the user asks to see their own applications, use VIEW_APPLICATIONS.
                Supported candidateIntent values are CREATE_QUEST, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST, UPDATE_CIRCLE, DELETE_CIRCLE, CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION, DISCOVER_QUESTS, OPEN_CHAT, VIEW_PROFILE, VIEW_CIRCLES, VIEW_APPLICATIONS, and UNSUPPORTED.
                Supported capabilityId values are create_quest, create_circle, create_circle_request, accept_circle_request, delete_circle_request, update_circle, delete_circle, create_application, update_application, withdraw_application, approve_application, decline_application, update_profile, update_profile_location, discover_quests, open_chat, view_profile, view_circles, view_applications, and unsupported.
                Only extract values that are explicitly stated or strongly and directly implied by the user's words.
                Keep the normalizedPrompt concise and in English when possible.
                If the candidate intent is DISCOVER_QUESTS, use searchQuery to capture the concrete topic the user wants to browse, such as moving help, delivery, or odd jobs.
                If the candidate intent is OPEN_CHAT, use targetUserQuery to capture the person the user wants to chat with.
                Do not include generic discovery words like find, browse, show, open, available, jobs, work, or quests in searchQuery unless the user explicitly named them as the topic.
                If the candidate intent is CREATE_CIRCLE, use circleName for the intended circle name when it is explicitly stated.
                If the candidate intent is CREATE_CIRCLE_REQUEST, use targetUserQuery for the intended request recipient.
                If the candidate intent is ACCEPT_CIRCLE_REQUEST, use targetUserQuery for the requester whose request should be accepted.
                If the candidate intent is DELETE_CIRCLE_REQUEST, use targetUserQuery for the counterpart whose pending request should be declined or cancelled.
                If the candidate intent is UPDATE_CIRCLE, use targetCircleQuery for the existing circle target and circleName for the replacement name only when they are explicitly stated.
                If the candidate intent is DELETE_CIRCLE, use targetCircleQuery only.
                If the candidate intent is CREATE_APPLICATION, use applicationQuestQuery for the quest id or quest wording, applicationMessage for the applicant's message, and applicationProposedPrice only when the user explicitly gave a price.
                If the candidate intent is UPDATE_APPLICATION, use applicationQuestQuery for the quest id or quest wording, plus only the replacement applicationMessage or applicationProposedPrice values the user explicitly gave.
                If the candidate intent is WITHDRAW_APPLICATION, use applicationQuestQuery for the quest id or quest wording only.
                If the candidate intent is APPROVE_APPLICATION or DECLINE_APPLICATION, use applicationQuestQuery for the quest id or quest wording plus targetUserQuery for the applicant.
                If the candidate intent is UPDATE_PROFILE, use profileUsername and profileDescription only when the user explicitly asks to change those values.
                If the candidate intent is UPDATE_PROFILE_LOCATION, use profileLocationMode and profileLocationLabel only when the user explicitly asks to change them.
                Use these slot ids only when the user explicitly provided the information:
                questTitle, questTitleConfidence, questDescription, questDescriptionConfidence, visibility, visibilityConfidence, reward.freeQuest, reward.freeQuestConfidence, reward.amount, reward.amountConfidence, schedule.mode, schedule.modeConfidence, schedule.scheduledDate, schedule.scheduledDateConfidence, schedule.scheduledTime, schedule.scheduledTimeConfidence, location.mode, location.modeConfidence, location.label, location.labelConfidence, location.candidateConfirmation, location.candidateConfirmationConfidence.
                Also use circleName and circleNameConfidence only for CREATE_CIRCLE or UPDATE_CIRCLE.
                Also use targetCircleQuery and targetCircleQueryConfidence only for UPDATE_CIRCLE or DELETE_CIRCLE.
                Also use applicationQuestQuery, applicationQuestQueryConfidence, applicationMessage, applicationMessageConfidence, applicationProposedPrice, and applicationProposedPriceConfidence only for CREATE_APPLICATION or UPDATE_APPLICATION.
                Also use profileUsername, profileUsernameConfidence, profileDescription, and profileDescriptionConfidence only for UPDATE_PROFILE.
                Also use profileLocationMode, profileLocationModeConfidence, profileLocationLabel, and profileLocationLabelConfidence only for UPDATE_PROFILE_LOCATION.
                Also choose exactly one focusSlotId when one slot is the main target of the turn, or null when the turn is only a general statement.
                Use these focus slot ids only: circle_name, target_circle_query, target_quest_query, target_user, application_message, application_proposed_price, profile_username, profile_description, profile_location_mode, profile_location_label, quest_title, quest_description, reward_amount, visibility, schedule_mode, scheduled_date, scheduled_time, location_mode, location_label, location_candidate_confirmation.
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
                If the prompt includes a calendar day, put it in schedule.scheduledDate using yyyy-MM-dd in the userContext timezone when possible.
                If the prompt includes a time of day, put it in schedule.scheduledTime using HH:mm 24-hour local time in the userContext timezone when possible.
                If the prompt indicates a flexible or by-agreement timing, set schedule.mode to "agreement" and leave schedule.scheduledDate and schedule.scheduledTime null.

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
                    "circleName": "Neighbours",
                    "circleNameConfidence": 1.0,
                    "targetCircleQuery": "Family",
                    "targetCircleQueryConfidence": 1.0,
                    "applicationQuestQuery": "Move a sofa",
                    "applicationQuestQueryConfidence": 1.0,
                    "applicationMessage": "I can help tomorrow evening.",
                    "applicationMessageConfidence": 1.0,
                    "applicationProposedPrice": "20",
                    "applicationProposedPriceConfidence": 1.0,
                    "profileUsername": "josip",
                    "profileUsernameConfidence": 1.0,
                    "profileDescription": "Reliable help for furniture moves.",
                    "profileDescriptionConfidence": 1.0,
                    "profileLocationMode": "APPROXIMATE",
                    "profileLocationModeConfidence": 1.0,
                    "profileLocationLabel": "Zurich, Switzerland",
                    "profileLocationLabelConfidence": 1.0,
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

                Semantic orchestration request:
                %s
                """.formatted(orchestrationRequestJson);
    }

    private Map<String, Object> responseContract() {
        Map<String, Object> responseContract = new LinkedHashMap<>();
        responseContract.put("format", "VisionPromptUnderstandingResult");
        responseContract.put("candidateIntents", java.util.List.of(
                "CREATE_QUEST",
                "CREATE_CIRCLE",
                "CREATE_CIRCLE_REQUEST",
                "ACCEPT_CIRCLE_REQUEST",
                "DELETE_CIRCLE_REQUEST",
                "UPDATE_CIRCLE",
                "DELETE_CIRCLE",
                "CREATE_APPLICATION",
                "UPDATE_APPLICATION",
                "WITHDRAW_APPLICATION",
                "APPROVE_APPLICATION",
                "DECLINE_APPLICATION",
                "UPDATE_PROFILE",
                "UPDATE_PROFILE_LOCATION",
                "DISCOVER_QUESTS",
                "OPEN_CHAT",
                "VIEW_PROFILE",
                "VIEW_CIRCLES",
                "VIEW_APPLICATIONS",
                "UNSUPPORTED"
        ));
        responseContract.put("capabilityIds", java.util.List.of(
                "create_quest",
                "create_circle",
                "create_circle_request",
                "accept_circle_request",
                "delete_circle_request",
                "update_circle",
                "delete_circle",
                "create_application",
                "update_application",
                "withdraw_application",
                "approve_application",
                "decline_application",
                "update_profile",
                "update_profile_location",
                "discover_quests",
                "open_chat",
                "view_profile",
                "view_circles",
                "view_applications",
                "unsupported"
        ));
        responseContract.put("focusSlotIds", java.util.List.of(
                "circle_name",
                "target_circle_query",
                "target_quest_query",
                "target_user",
                "application_message",
                "application_proposed_price",
                "profile_username",
                "profile_description",
                "profile_location_mode",
                "profile_location_label",
                "quest_title",
                "quest_description",
                "reward_amount",
                "visibility",
                "schedule_mode",
                "scheduled_date",
                "scheduled_time",
                "location_mode",
                "location_label",
                "location_candidate_confirmation"
        ));
        responseContract.put("minimumAcceptedSlotConfidence", VisionPromptUnderstandingResult.MIN_SLOT_CONFIDENCE);
        return responseContract;
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
