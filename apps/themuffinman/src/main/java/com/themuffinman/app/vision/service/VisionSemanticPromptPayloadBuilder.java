package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.semantic.SemanticEntityFamily;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class VisionSemanticPromptPayloadBuilder {

    private final ObjectMapper objectMapper;
    private final SemanticAliasRegistry semanticAliasRegistry;

    VisionSemanticPromptPayloadBuilder(ObjectMapper objectMapper, SemanticAliasRegistry semanticAliasRegistry) {
        this.objectMapper = objectMapper;
        this.semanticAliasRegistry = semanticAliasRegistry;
    }

    String buildInput(VisionSemanticOrchestrationRequest orchestrationRequest) {
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
                Use memoryContext.userMemory for stable user preferences, learned input-type habits, preferred entity family, and compact learning summary signals.
                Use memoryContext.sessionMemory for the current thread, open questions, last prompts, and slot state.
                Use memoryContext.recentConversations only as lightweight reminders when topics switch.
                Use runtimeContext to know whether the turn came from text or voice.
                Use conversationContext for already collected slot data, activeSlot, and draftSnapshot.
                Use semanticHints.familyAliases to map multilingual aliases and paraphrases to the correct family before choosing targetEntityQuery.
                Use route examples and slot aliases to resolve the shortest valid slot value from the user wording.
                Use slot anti-examples to avoid mapping instruction words into slot values.
                Keep business page and business availability snapshots separate from future booking mutations.
                Determine candidateIntent and capability first.
                Extract only values that are explicit or directly implied.
                When an intent phrase is followed by an entity phrase, fill the matching slot from the shortest cleaned entity phrase and do not include the intent words in the slot value.
                Example: create new circle Lover -> circleName = Lover; open circle Family -> targetCircleQuery = Family; show application #42 -> targetApplicationQuery = #42; show user Josip -> profileUsername = Josip.
                Use the route examples, slot aliases, slot descriptions, and allowed values from allowedRoutes as the source of truth for each slot.
                Normalize the internal semantic meaning into English only.
                Always return normalizedPrompt in English, even if the user spoke another language or used broken grammar.
                If the prompt is vague or slot-follow-up, prefer the current session entity family unless the user clearly changes topic.
                If required slots are missing or the target entity is ambiguous, set clarificationRequired to true and list missingRequiredSlotIds.
                Supported intents: CREATE_QUEST, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST, UPDATE_CIRCLE, DELETE_CIRCLE, CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION, DISCOVER_QUESTS, SEARCH, OPEN_CHAT, VIEW_PROFILE, VIEW_SETTINGS, VIEW_BUSINESS, VIEW_BUSINESS_AVAILABILITY, VIEW_BUSINESS_BOOKINGS, VIEW_CIRCLES, VIEW_CIRCLE_DETAIL, VIEW_NOTIFICATIONS, VIEW_QUEST_NEWS, VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL, VIEW_THINGS, UNSUPPORTED.
                For CREATE_QUEST, keep questTitle, questDescription, visibility, reward.amount, reward.freeQuest, schedule.mode, schedule.scheduledDate, schedule.scheduledTime, location.mode, and location.label separate.
                For circle, application, profile, chat, search, things, notifications, and discover intents, fill only the matching slot family and leave unrelated slots null.
                Use the userContext timezone when interpreting dates and times.
                If the prompt says free/no pay/unpaid, set reward.freeQuest to true and reward.amount to "0".
                Use low confidence for inferred values and high confidence only for explicitly stated values.
                Output JSON in the requested response contract.

                Semantic orchestration request:
                %s
                """.formatted(orchestrationRequestJson);
    }

    private Map<String, Object> compactRequest(VisionSemanticOrchestrationRequest orchestrationRequest) {
        Map<String, Object> compact = new LinkedHashMap<>();
        compact.put("contractVersion", orchestrationRequest.getContractVersion());
        compact.put("rawPrompt", orchestrationRequest.getRawPrompt());
        compact.put("semanticHints", compactSemanticHints());
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
        compact.put("activeSlot", context.getActiveSlot());
        compact.put("slotData", context.getSlotData());
        compact.put("draftSnapshot", context.getDraftSnapshot());
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
        compact.put("preferredInputType", context.getPreferredInputType());
        compact.put("preferredEntityFamily", context.getPreferredEntityFamily());
        compact.put("learningSummary", context.getLearningSummary());
        compact.put("retrievalSummary", context.getRetrievalSummary());
        compact.put("recentFeedbackTypes", context.getRecentFeedbackTypes());
        compact.put("recentIntentTypes", context.getRecentIntentTypes());
        compact.put("recentEntityFamilies", context.getRecentEntityFamilies());
        compact.put("explainabilityRecords", context.getExplainabilityRecords());
        compact.put("retrievedEntityFamily", context.getRetrievedEntityFamily());
        compact.put("retrievedEntityFamilyConfidence", context.getRetrievedEntityFamilyConfidence());
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

    private Map<String, Object> compactRuntimeContext(VisionSemanticRuntimeContext context) {
        if (context == null) {
            return Map.of();
        }

        Map<String, Object> compact = new LinkedHashMap<>();
        compact.put("inputType", context.getInputType());
        compact.put("clientLocale", context.getClientLocale());
        compact.put("clientTimezone", context.getClientTimezone());
        compact.put("clientDeviceRole", context.getClientDeviceRole());
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
                    compact.put("entityType", route.getEntityType());
                    compact.put("entityFamily", route.getEntityFamily());
                    compact.put("dtoType", route.getDtoType());
                    compact.put("purpose", route.getPurpose());
                    compact.put("mutating", route.isMutating());
                    compact.put("requiresReview", route.isRequiresReview());
                    compact.put("examples", compactExamples(route.getExamples()));
                    compact.put("slots", compactSlots(route.getSlots()));
                    return compact;
                })
                .toList();
    }

    private List<Map<String, Object>> compactExamples(List<VisionSemanticRouteExampleDescriptor> examples) {
        if (examples == null || examples.isEmpty()) {
            return List.of();
        }

        return examples.stream()
                .map(example -> {
                    Map<String, Object> compact = new LinkedHashMap<>();
                    compact.put("input", example.getInput());
                    compact.put("expectedSlots", example.getExpectedSlots());
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
                    compact.put("fieldName", slot.getFieldName());
                    compact.put("kind", slot.getKind());
                    compact.put("required", slot.isRequired());
                    compact.put("description", slot.getDescription());
                    compact.put("allowedValues", slot.getAllowedValues());
                    compact.put("aliases", slot.getAliases());
                    compact.put("antiExamples", slot.getAntiExamples());
                    return compact;
                })
                .toList();
    }

    private Map<String, Object> compactSemanticHints() {
        Map<String, Object> compact = new LinkedHashMap<>();
        compact.put("familyAliases", Map.of(
                "quest", semanticAliasRegistry.aliasesFor(SemanticEntityFamily.QUEST),
                "notifications", semanticAliasRegistry.aliasesFor(SemanticEntityFamily.NOTIFICATIONS),
                "circle", semanticAliasRegistry.aliasesFor(SemanticEntityFamily.CIRCLE),
                "application", semanticAliasRegistry.aliasesFor(SemanticEntityFamily.APPLICATION),
                "user", semanticAliasRegistry.aliasesFor(SemanticEntityFamily.USER),
                "business", semanticAliasRegistry.aliasesFor(SemanticEntityFamily.BUSINESS)
        ));
        return compact;
    }
}
