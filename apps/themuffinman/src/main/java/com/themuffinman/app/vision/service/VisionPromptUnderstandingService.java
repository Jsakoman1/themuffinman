package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.agent.service.LocalAdminAgentPromptTranslator;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
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
    private final VisionSemanticResponseValidator semanticResponseValidator;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    public VisionPromptUnderstandingService(
            AgentProperties agentProperties,
            LocalAdminAgentPromptTranslator localAdminAgentPromptTranslator,
            VisionSemanticMapper visionSemanticMapper,
            PromptSemanticsSupport promptSemanticsSupport,
            VisionSemanticOrchestrationContextService semanticContextService,
            VisionSemanticRouteCatalogService semanticRouteCatalogService,
            VisionSemanticContractSanitizer semanticContractSanitizer,
            VisionSemanticResponseValidator semanticResponseValidator
    ) {
        this.agentProperties = agentProperties;
        this.localAdminAgentPromptTranslator = localAdminAgentPromptTranslator;
        this.visionSemanticMapper = visionSemanticMapper;
        this.promptSemanticsSupport = promptSemanticsSupport;
        this.semanticContextService = semanticContextService;
        this.semanticRouteCatalogService = semanticRouteCatalogService;
        this.semanticContractSanitizer = semanticContractSanitizer;
        this.semanticResponseValidator = semanticResponseValidator;
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
        payload.put("model", resolveSemanticModel());
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
            if (understanding.semanticPlanOrEmpty().candidateIntentOrUnsupported() == VisionIntent.UNSUPPORTED) {
                VisionSemanticPlan fallbackPlan = VisionSemanticPlan.from(promptSemanticsSupport.inferPlan(understanding.getNormalizedPrompt()));
                if (fallbackPlan.candidateIntentOrUnsupported() != VisionIntent.UNSUPPORTED) {
                    understanding.setSemanticPlan(fallbackPlan);
                }
            }
            semanticResponseValidator.validate(understanding, orchestrationRequest);
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
                .memoryContext(semanticContextService.buildMemoryContext(effectiveUser, conversation))
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

    private String resolveSemanticModel() {
        if (agentProperties.isSemanticModelUpgradeEnabled()
                && agentProperties.getSemanticModelUpgrade() != null
                && !agentProperties.getSemanticModelUpgrade().isBlank()) {
            return agentProperties.getSemanticModelUpgrade();
        }
        if (agentProperties.getSemanticModel() != null && !agentProperties.getSemanticModel().isBlank()) {
            return agentProperties.getSemanticModel();
        }
        return agentProperties.getModel();
    }

    private Map<String, Object> compactRequest(VisionSemanticOrchestrationRequest orchestrationRequest) {
        Map<String, Object> compact = new LinkedHashMap<>();
        compact.put("contractVersion", orchestrationRequest.getContractVersion());
        compact.put("rawPrompt", orchestrationRequest.getRawPrompt());
        compact.put("userContext", compactUserContext(orchestrationRequest.getUserContext()));
        compact.put("conversationContext", compactConversationContext(orchestrationRequest.getConversationContext()));
        compact.put("memoryContext", compactMemoryContext(orchestrationRequest.getMemoryContext()));
        compact.put("runtimeContext", compactRuntimeContext(orchestrationRequest.getRuntimeContext()));
        compact.put("allowedRoutes", compactRoutes(orchestrationRequest.getAllowedRoutes()));
        compact.put("responseContract", orchestrationRequest.getResponseContract());
        return compact;
    }

    private Map<String, Object> compactUserContext(VisionSemanticUserContext context) {
        if (context == null) {
            return Map.of();
        }

        Map<String, Object> compact = new LinkedHashMap<>();
        compact.put("preferredLocale", context.getPreferredLocale());
        compact.put("timezone", context.getTimezone());
        compact.put("countryCode", context.getCountryCode());
        compact.put("locality", context.getLocality());
        return compact;
    }

    private Map<String, Object> compactConversationContext(VisionSemanticConversationContext context) {
        if (context == null) {
            return Map.of();
        }

        Map<String, Object> compact = new LinkedHashMap<>();
        compact.put("conversationId", context.getConversationId());
        compact.put("currentIntent", context.getCurrentIntent());
        compact.put("requestedSlot", context.getRequestedSlot());
        compact.put("slotData", context.getSlotData());
        return compact;
    }

    private Map<String, Object> compactMemoryContext(VisionSemanticMemoryContext context) {
        if (context == null) {
            return Map.of();
        }

        Map<String, Object> compact = new LinkedHashMap<>();
        compact.put("userMemory", compactUserMemory(context.getUserMemory()));
        compact.put("sessionMemory", compactSessionMemory(context.getSessionMemory()));
        compact.put("recentConversations", compactRecentConversations(context.getRecentConversations()));
        return compact;
    }

    private Map<String, Object> compactUserMemory(VisionSemanticUserMemoryContext context) {
        if (context == null) {
            return Map.of();
        }

        Map<String, Object> compact = new LinkedHashMap<>();
        compact.put("username", context.getUsername());
        compact.put("role", context.getRole());
        compact.put("preferredLocale", context.getPreferredLocale());
        compact.put("timezone", context.getTimezone());
        compact.put("countryCode", context.getCountryCode());
        compact.put("locality", context.getLocality());
        compact.put("recentIntentTypes", context.getRecentIntentTypes());
        compact.put("recentEntityFamilies", context.getRecentEntityFamilies());
        return compact;
    }

    private Map<String, Object> compactSessionMemory(VisionSemanticSessionMemoryContext context) {
        if (context == null) {
            return Map.of();
        }

        Map<String, Object> compact = new LinkedHashMap<>();
        compact.put("conversationId", context.getConversationId());
        compact.put("currentIntent", context.getCurrentIntent());
        compact.put("currentEntityFamily", context.getCurrentEntityFamily());
        compact.put("status", context.getStatus());
        compact.put("requestedSlot", context.getRequestedSlot());
        compact.put("sessionSummary", context.getSessionSummary());
        compact.put("lastNormalizedPrompt", context.getLastNormalizedPrompt());
        compact.put("translationReliable", context.isTranslationReliable());
        compact.put("openQuestions", context.getOpenQuestions());
        compact.put("recentActions", context.getRecentActions());
        compact.put("slotData", context.getSlotData());
        return compact;
    }

    private List<Map<String, Object>> compactRecentConversations(List<VisionSemanticConversationMemoryItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .limit(2)
                .map(item -> {
                    Map<String, Object> compact = new LinkedHashMap<>();
                    compact.put("conversationId", item.getConversationId());
                    compact.put("intent", item.getIntent());
                    compact.put("status", item.getStatus());
                    compact.put("requestedSlot", item.getRequestedSlot());
                    return compact;
                })
                .toList();
    }

    private List<Map<String, Object>> compactRecentTurns(List<VisionSemanticTurnMemoryItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .limit(2)
                .map(item -> {
                    Map<String, Object> compact = new LinkedHashMap<>();
                    compact.put("turnIndex", item.getTurnIndex());
                    compact.put("prompt", item.getPrompt());
                    compact.put("normalizedPrompt", item.getNormalizedPrompt());
                    compact.put("detectedIntent", item.getDetectedIntent());
                    compact.put("requestedSlot", item.getRequestedSlot());
                    return compact;
                })
                .toList();
    }

    private Map<String, Object> compactRuntimeContext(VisionSemanticRuntimeContext context) {
        if (context == null) {
            return Map.of();
        }

        Map<String, Object> compact = new LinkedHashMap<>();
        compact.put("inputType", context.getInputType());
        compact.put("clientLocale", context.getClientLocale());
        compact.put("clientTimezone", context.getClientTimezone());
        return compact;
    }

    private List<Map<String, Object>> compactRoutes(List<VisionSemanticRouteDescriptor> routes) {
        if (routes == null || routes.isEmpty()) {
            return List.of();
        }

        return routes.stream()
                .map(route -> {
                    Map<String, Object> compact = new LinkedHashMap<>();
                    compact.put("routeKey", route.getRouteKey());
                    compact.put("intent", route.getIntent());
                    compact.put("capabilityId", route.getCapabilityId());
                    compact.put("mutating", route.isMutating());
                    compact.put("requiresReview", route.isRequiresReview());
                    return compact;
                })
                .toList();
    }

    private List<Map<String, Object>> compactSlots(List<VisionSemanticSlotDescriptor> slots) {
        if (slots == null || slots.isEmpty()) {
            return List.of();
        }

        return slots.stream()
                .map(slot -> {
                    Map<String, Object> compact = new LinkedHashMap<>();
                    compact.put("slotId", slot.getSlotId());
                    compact.put("kind", slot.getKind());
                    compact.put("required", slot.isRequired());
                    return compact;
                })
                .toList();
    }

    private String buildInput(VisionSemanticOrchestrationRequest orchestrationRequest) {
        String orchestrationRequestJson;
        try {
            orchestrationRequestJson = objectMapper.writeValueAsString(compactRequest(orchestrationRequest));
        } catch (Exception exception) {
            throw ServiceErrors.badRequest("Vision semantic orchestration request serialization failed: " + exception.getMessage());
        }
        return """
                You are the semantic orchestration layer for a backend-governed vision system.
                Return valid JSON only.
                Use only allowedRoutes from the request. Do not invent routes, DTO fields, capabilities, or permissions.
                Use userContext for locale, phrasing, and timezone-aware date/time parsing.
                Use memoryContext.userMemory for stable user preferences.
                Use memoryContext.sessionMemory for the current thread, open questions, last prompts, and slot state.
                Use memoryContext.recentConversations only as lightweight reminders when topics switch.
                Use runtimeContext to know whether the turn came from text or voice.
                Use conversationContext for already collected slot data.
                Determine candidateIntent and capability first.
                Extract only values that are explicit or directly implied.
                Keep normalizedPrompt concise and in English when possible.
                If the prompt is vague or slot-follow-up, prefer the current session entity family unless the user clearly changes topic.
                Supported intents: CREATE_QUEST, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST, UPDATE_CIRCLE, DELETE_CIRCLE, CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION, DISCOVER_QUESTS, OPEN_CHAT, VIEW_PROFILE, VIEW_SETTINGS, VIEW_CIRCLES, VIEW_CIRCLE_DETAIL, VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL, UNSUPPORTED.
                For CREATE_QUEST, keep questTitle, questDescription, visibility, reward.amount, reward.freeQuest, schedule.mode, schedule.scheduledDate, schedule.scheduledTime, location.mode, and location.label separate.
                For circle, application, profile, chat, and discover intents, fill only the matching slot family and leave unrelated slots null.
                Use the userContext timezone when interpreting dates and times.
                If the prompt says free/no pay/unpaid, set reward.freeQuest to true and reward.amount to "0".
                Use low confidence for inferred values and high confidence only for explicitly stated values.
                Output JSON in the requested response contract.

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
                "VIEW_CHAT_WORKSPACE",
                "VIEW_PROFILE",
                "VIEW_SETTINGS",
                "VIEW_USER_PROFILE",
                "VIEW_CIRCLES",
                "VIEW_CIRCLE_DETAIL",
                "VIEW_QUEST_DETAIL",
                "VIEW_APPLICATIONS",
                "VIEW_APPLICATION_DETAIL",
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
                "view_chat_workspace",
                "view_profile",
                "view_settings",
                "view_user_profile",
                "view_circles",
                "view_circle_detail",
                "view_quest_detail",
                "view_applications",
                "view_application_detail",
                "unsupported"
        ));
        responseContract.put("focusSlotIds", java.util.List.of(
                "circle_name",
                "target_circle_query",
                "target_application_query",
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
