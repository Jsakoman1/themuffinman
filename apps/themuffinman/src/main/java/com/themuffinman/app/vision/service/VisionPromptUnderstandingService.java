package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
import com.themuffinman.app.semantic.VisionEntityResolverRegistry;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class VisionPromptUnderstandingService {
    static final String SEMANTIC_CONTRACT_VERSION = "vision-semantic-orchestration-v1";
    static final String UNDERSTANDING_PROVIDER_OPENAI = "openai";
    static final String UNDERSTANDING_PROVIDER_LOCAL = "local";
    static final String UNDERSTANDING_PROVIDER_NONE = "none";
    static final String UNDERSTANDING_STATUS_OPENAI_PRIMARY = "openai_primary";
    static final String UNDERSTANDING_STATUS_OPENAI_LOCAL_RESCUE = "openai_local_rescue";
    static final String UNDERSTANDING_STATUS_OPENAI_UNSUPPORTED = "openai_unsupported";
    static final String UNDERSTANDING_STATUS_LOCAL_EMERGENCY = "local_emergency";
    static final String UNDERSTANDING_STATUS_LOCAL_FAIL_CLOSED = "local_fail_closed";
    static final String UNDERSTANDING_STATUS_OPENAI_UNAVAILABLE = "openai_unavailable";

    private static final Set<VisionIntent> SAFE_LOCAL_EMERGENCY_INTENTS = EnumSet.of(
            VisionIntent.CREATE_QUEST,
            VisionIntent.CREATE_CIRCLE,
            VisionIntent.CREATE_APPLICATION,
            VisionIntent.UPDATE_PROFILE,
            VisionIntent.DISCOVER_QUESTS,
            VisionIntent.OPEN_CHAT,
            VisionIntent.VIEW_CHAT_WORKSPACE,
            VisionIntent.VIEW_CHAT_ATTACHMENT,
            VisionIntent.VIEW_SETTINGS,
            VisionIntent.VIEW_PROFILE,
            VisionIntent.VIEW_USER_PROFILE,
            VisionIntent.VIEW_CIRCLES,
            VisionIntent.VIEW_CIRCLE_DETAIL,
            VisionIntent.VIEW_QUEST_DETAIL,
            VisionIntent.VIEW_NOTIFICATIONS,
            VisionIntent.VIEW_ACTIVITY,
            VisionIntent.VIEW_BUSINESS,
            VisionIntent.VIEW_BUSINESS_AVAILABILITY,
            VisionIntent.VIEW_BUSINESS_BOOKINGS,
            VisionIntent.VIEW_THINGS,
            VisionIntent.VIEW_RIDES,
            VisionIntent.VIEW_QUEST_NEWS,
            VisionIntent.VIEW_APPLICATIONS,
            VisionIntent.VIEW_MY_WORK,
            VisionIntent.VIEW_APPLICATION_DETAIL,
            VisionIntent.VIEW_BORROW_REQUESTS
            ,VisionIntent.EDIT_CHAT_MESSAGE,
            VisionIntent.REPLY_TO_CHAT_MESSAGE,
            VisionIntent.REACT_TO_CHAT_MESSAGE
            ,VisionIntent.CANCEL_QUEST,
            VisionIntent.PAUSE_QUEST,
            VisionIntent.RESUME_QUEST
            ,VisionIntent.CREATE_BUSINESS_PROFILE,
            VisionIntent.UPDATE_BUSINESS_PROFILE
            ,VisionIntent.CONFIRM_BOOKING,
            VisionIntent.CANCEL_BOOKING
            ,VisionIntent.REJECT_BOOKING,
            VisionIntent.COMPLETE_BOOKING,
            VisionIntent.MARK_BOOKING_NO_SHOW
            ,VisionIntent.ARCHIVE_OFFERING,
            VisionIntent.UPDATE_QUEST,
            VisionIntent.CREATE_OFFERING,
            VisionIntent.UPDATE_OFFERING
            ,VisionIntent.CREATE_BOOKING
    );
    private final AgentProperties agentProperties;
    private final VisionSemanticMapper visionSemanticMapper;
    private final PromptSemanticsSupport promptSemanticsSupport;
    private final VisionSemanticOrchestrationContextService semanticContextService;
    private final VisionSemanticRouteCatalogService semanticRouteCatalogService;
    private final VisionSemanticContractSanitizer semanticContractSanitizer;
    private final VisionSemanticResponseValidator semanticResponseValidator;
    private final VisionSemanticPromptPayloadBuilder visionSemanticPromptPayloadBuilder;
    private final VisionSemanticEnvelopeSupport visionSemanticEnvelopeSupport;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    public VisionPromptUnderstandingService(
            AgentProperties agentProperties,
            VisionSemanticMapper visionSemanticMapper,
            PromptSemanticsSupport promptSemanticsSupport,
            VisionSemanticOrchestrationContextService semanticContextService,
            VisionSemanticRouteCatalogService semanticRouteCatalogService,
            VisionSemanticContractSanitizer semanticContractSanitizer,
            VisionSemanticResponseValidator semanticResponseValidator,
            VisionEntityResolverRegistry visionEntityResolverRegistry,
            com.themuffinman.app.semantic.SemanticAliasRegistry semanticAliasRegistry
    ) {
        this.agentProperties = agentProperties;
        this.visionSemanticMapper = visionSemanticMapper;
        this.promptSemanticsSupport = promptSemanticsSupport;
        this.semanticContextService = semanticContextService;
        this.semanticRouteCatalogService = semanticRouteCatalogService;
        this.semanticContractSanitizer = semanticContractSanitizer;
        this.semanticResponseValidator = semanticResponseValidator;
        this.visionSemanticPromptPayloadBuilder = new VisionSemanticPromptPayloadBuilder(objectMapper, semanticAliasRegistry);
        this.visionSemanticEnvelopeSupport = new VisionSemanticEnvelopeSupport(
                semanticRouteCatalogService,
                visionEntityResolverRegistry,
                semanticAliasRegistry
        );
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
            return unavailableOrDevelopmentFixture(trimmed, conversation, currentUser,
                    "OpenAI semantic understanding is not configured.");
        }

        try {
                return openAiUnderstand(orchestrationRequest, conversation, currentUser);
        } catch (RestClientException exception) {
            return unavailableOrDevelopmentFixture(trimmed, conversation, currentUser,
                    "OpenAI semantic understanding is temporarily unavailable, so only emergency local routing is available.");
        } catch (RuntimeException exception) {
            return unavailableOrDevelopmentFixture(trimmed, conversation, currentUser,
                    "OpenAI semantic understanding failed for this turn, so only emergency local routing is available.");
        }
    }

    private VisionPromptUnderstandingResult unavailableOrDevelopmentFixture(
            String prompt,
            VisionConversation conversation,
            AppUser currentUser,
            String reason
    ) {
        if (developmentFixtureEnabled()) {
            return localEmergencyUnderstand(prompt, conversation, currentUser, reason + " Development-only local routing is enabled.");
        }
        VisionPromptUnderstandingResult unavailable = VisionPromptUnderstandingResult.empty(prompt);
        unavailable.setUnderstandingProvider(UNDERSTANDING_PROVIDER_NONE);
        unavailable.setUnderstandingStatus(UNDERSTANDING_STATUS_OPENAI_UNAVAILABLE);
        unavailable.setTranslationProvider(UNDERSTANDING_PROVIDER_NONE);
        unavailable.setTranslationReliable(false);
        unavailable.getSemanticPlan().setPlanningNote("Vision is paused until the OpenAI semantic provider is available. Retry is safe; no local parser is used in this environment.");
        return unavailable;
    }

    private VisionPromptUnderstandingResult openAiUnderstand(
            VisionSemanticOrchestrationRequest orchestrationRequest,
            VisionConversation conversation,
            AppUser currentUser
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", resolveSemanticModel());
        payload.put("input", visionSemanticPromptPayloadBuilder.buildInput(orchestrationRequest));
        payload.put("reasoning", Map.of("effort", "low"));
        payload.put("text", Map.of("verbosity", "low"));

        String outputText = requestOpenAiOutputText(payload);
        if (outputText == null || outputText.isBlank()) {
            throw ServiceErrors.badRequest("OpenAI vision understanding response did not include usable text output");
        }

        try {
            VisionPromptUnderstandingResult understanding = objectMapper.readValue(outputText, VisionPromptUnderstandingResult.class);
            alignUnderstandingContractDefaults(understanding, orchestrationRequest.getRawPrompt(), orchestrationRequest.getContractVersion(), providerName());
            understanding.setUnderstandingProvider(UNDERSTANDING_PROVIDER_OPENAI);
            understanding.setUnderstandingStatus(UNDERSTANDING_STATUS_OPENAI_PRIMARY);
            VisionPromptUnderstandingResult repairedUnderstanding = attemptFocusedRepair(understanding, orchestrationRequest, conversation);
            if (repairedUnderstanding != null) {
                understanding = repairedUnderstanding;
            }
            applySafeOpenAiRescueIfNeeded(understanding);
            semanticResponseValidator.validate(understanding, orchestrationRequest);
            semanticContractSanitizer.sanitize(understanding, orchestrationRequest.getAllowedRoutes());
            visionSemanticMapper.applyFallbackFocus(understanding, conversation);
            understanding.setSemanticEnvelope(visionSemanticEnvelopeSupport.buildSemanticEnvelope(
                    orchestrationRequest.getRawPrompt(),
                    understanding,
                    currentUser,
                    providerName(),
                    true,
                    true,
                    orchestrationRequest.getContractVersion(),
                    resolveSemanticModel()
            ));
            return understanding;
        } catch (Exception exception) {
            throw ServiceErrors.badRequest("OpenAI vision understanding parsing failed: " + exception.getMessage());
        }
    }

    private VisionPromptUnderstandingResult attemptFocusedRepair(
            VisionPromptUnderstandingResult understanding,
            VisionSemanticOrchestrationRequest orchestrationRequest,
            VisionConversation conversation
    ) {
        if (understanding == null || orchestrationRequest == null) {
            return null;
        }

        VisionSemanticPlan semanticPlan = understanding.semanticPlanOrEmpty();
        VisionIntent candidateIntent = semanticPlan.candidateIntentOrUnsupported();
        if (candidateIntent == VisionIntent.UNSUPPORTED) {
            return null;
        }

        VisionSemanticRouteDescriptor route = semanticRouteCatalogService.routeForIntent(candidateIntent.name());
        if (route == null || route.getSlots() == null || route.getSlots().isEmpty()) {
            return null;
        }

        String repairSlotId = selectRepairSlotId(understanding, route, conversation);
        if (repairSlotId == null) {
            return null;
        }

        VisionSemanticOrchestrationRequest repairRequest = filteredRepairRequest(orchestrationRequest, route, repairSlotId);
        if (repairRequest == null) {
            return null;
        }

        try {
            String repairOutput = requestOpenAiOutputText(Map.of(
                    "model", resolveSemanticModel(),
                    "input", buildRepairInput(repairRequest, repairSlotId),
                    "reasoning", Map.of("effort", "low"),
                    "text", Map.of("verbosity", "low")
            ));
            if (repairOutput == null || repairOutput.isBlank()) {
                understanding.setRepairAttempted(true);
                understanding.setRepairSlotId(repairSlotId);
                understanding.setRepairNote("repair_pass_empty");
                return understanding;
            }

            VisionPromptUnderstandingResult repairResult = objectMapper.readValue(repairOutput, VisionPromptUnderstandingResult.class);
            alignUnderstandingContractDefaults(repairResult, orchestrationRequest.getRawPrompt(), orchestrationRequest.getContractVersion(), providerName());
            repairResult.setUnderstandingProvider(UNDERSTANDING_PROVIDER_OPENAI);
            repairResult.setUnderstandingStatus(UNDERSTANDING_STATUS_OPENAI_PRIMARY);
            repairResult.setRepairAttempted(true);
            repairResult.setRepairSlotId(repairSlotId);
            repairResult.setRepairNote("focused_slot_repair");

            if (!repairResult.hasConfidentSlot(repairSlotId, VisionPromptUnderstandingResult.MIN_SLOT_CONFIDENCE)
                    && !repairResult.hasConfidentSlot(repairSlotId, 0.45d)) {
                understanding.setRepairAttempted(true);
                understanding.setRepairSlotId(repairSlotId);
                understanding.setRepairNote("repair_pass_no_change");
                return understanding;
            }

            understanding.copySlotValueFrom(repairResult, repairSlotId);
            understanding.setRepairAttempted(true);
            understanding.setRepairSlotId(repairSlotId);
            understanding.setRepairNote("focused_slot_repair_applied");
            if (repairResult.getFocusSlotId() != null && !repairResult.getFocusSlotId().isBlank()) {
                understanding.setFocusSlotId(repairResult.getFocusSlotId());
                understanding.setFocusSlotConfidence(repairResult.getFocusSlotConfidence());
            }
            if (repairResult.getSemanticPlan() != null && repairResult.getSemanticPlan().candidateIntentOrUnsupported() != VisionIntent.UNSUPPORTED) {
                understanding.setSemanticPlan(repairResult.getSemanticPlan());
            }
            if (repairResult.isClarificationRequired()) {
                understanding.setClarificationRequired(true);
            }
            return understanding;
        } catch (Exception exception) {
            understanding.setRepairAttempted(true);
            understanding.setRepairSlotId(repairSlotId);
            understanding.setRepairNote("repair_pass_failed");
            return understanding;
        }
    }

    private VisionSemanticOrchestrationRequest filteredRepairRequest(
            VisionSemanticOrchestrationRequest originalRequest,
            VisionSemanticRouteDescriptor route,
            String repairSlotId
    ) {
        if (originalRequest == null || route == null || repairSlotId == null || repairSlotId.isBlank()) {
            return null;
        }

        VisionSemanticConversationContext conversationContext = originalRequest.getConversationContext();
        VisionSemanticConversationContext repairConversationContext = conversationContext == null
                ? null
                : VisionSemanticConversationContext.builder()
                .conversationId(conversationContext.getConversationId())
                .currentIntent(conversationContext.getCurrentIntent())
                .requestedSlot(conversationContext.getRequestedSlot())
                .slotData(conversationContext.getSlotData())
                .activeSlot(repairSlotId)
                .draftSnapshot(conversationContext.getDraftSnapshot())
                .build();

        return VisionSemanticOrchestrationRequest.builder()
                .contractVersion(originalRequest.getContractVersion())
                .rawPrompt(originalRequest.getRawPrompt())
                .userContext(originalRequest.getUserContext())
                .memoryContext(originalRequest.getMemoryContext())
                .conversationContext(repairConversationContext)
                .runtimeContext(originalRequest.getRuntimeContext())
                .allowedRoutes(List.of(route))
                .responseContract(originalRequest.getResponseContract())
                .build();
    }

    private String buildRepairInput(VisionSemanticOrchestrationRequest repairRequest, String repairSlotId) {
        return """
                Focus on a single slot repair.
                Repair slot: %s
                Repair only the target slot if the user utterance clearly supports it.
                Keep all unrelated slots null.
                If the target slot is still unclear, preserve the existing understanding and set clarificationRequired to true.

                %s
                """.formatted(repairSlotId, visionSemanticPromptPayloadBuilder.buildInput(repairRequest));
    }

    private String selectRepairSlotId(
            VisionPromptUnderstandingResult understanding,
            VisionSemanticRouteDescriptor route,
            VisionConversation conversation
    ) {
        if (understanding == null || route == null || route.getSlots() == null || route.getSlots().isEmpty()) {
            return null;
        }

        Set<String> routeSlotIds = route.getSlots().stream()
                .map(VisionSemanticSlotDescriptor::getSlotId)
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.toSet());

        String requestedSlot = conversation == null ? null : conversation.getRequestedSlot();
        if (requestedSlot != null && routeSlotIds.contains(requestedSlot)
                && !understanding.hasConfidentSlot(requestedSlot, VisionPromptUnderstandingResult.MIN_SLOT_CONFIDENCE)) {
            return requestedSlot;
        }

        for (String missingSlotId : visionSemanticEnvelopeSupport.missingRequiredSlotIds(route, understanding)) {
            if (routeSlotIds.contains(missingSlotId)) {
                return missingSlotId;
            }
        }

        String focusSlotId = understanding.getFocusSlotId();
        if (focusSlotId != null && routeSlotIds.contains(focusSlotId)
                && !understanding.hasConfidentSlot(focusSlotId, VisionPromptUnderstandingResult.MIN_SLOT_CONFIDENCE)) {
            return focusSlotId;
        }

        return null;
    }

    protected String requestOpenAiOutputText(Map<String, Object> payload) {
        String responseBody = restClient.post()
                .uri(agentProperties.getBaseUrl() + "/responses")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentProperties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);
        JsonNode response = parseResponseBody(responseBody);
        return extractOutputText(response);
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

    private VisionPromptUnderstandingResult localEmergencyUnderstand(
            String prompt,
            VisionConversation conversation,
            AppUser currentUser,
            String failureReason
    ) {
        String normalizedPrompt = promptSemanticsSupport.normalizePrompt(prompt);
        boolean englishPrompt = isLikelyEnglishPrompt(normalizedPrompt);
        VisionSemanticPlan emergencyPlan = emergencyLocalPlan(normalizedPrompt, englishPrompt, failureReason);
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .sourceLanguage(englishPrompt ? "en" : "unknown")
                .originalPrompt(prompt)
                .normalizedPrompt(normalizedPrompt)
                .translationProvider(UNDERSTANDING_PROVIDER_NONE)
                .understandingProvider(UNDERSTANDING_PROVIDER_LOCAL)
                .understandingStatus(emergencyPlan.candidateIntentOrUnsupported() == VisionIntent.UNSUPPORTED
                        ? UNDERSTANDING_STATUS_LOCAL_FAIL_CLOSED
                        : UNDERSTANDING_STATUS_LOCAL_EMERGENCY)
                .semanticContractVersion(SEMANTIC_CONTRACT_VERSION)
                .focusSlotId(null)
                .focusSlotConfidence(null)
                .semanticPlan(emergencyPlan)
                .translationApplied(false)
                .translationReliable(englishPrompt)
                .slots(new VisionPromptUnderstandingSlots())
                .build();
        alignUnderstandingContractDefaults(understanding, prompt, SEMANTIC_CONTRACT_VERSION, UNDERSTANDING_PROVIDER_NONE);
        visionSemanticMapper.applyFallbackFocus(understanding, conversation);
        understanding.setSemanticEnvelope(visionSemanticEnvelopeSupport.buildSemanticEnvelope(
                prompt,
                understanding,
                currentUser,
                UNDERSTANDING_PROVIDER_NONE,
                false,
                englishPrompt,
                SEMANTIC_CONTRACT_VERSION,
                UNDERSTANDING_PROVIDER_LOCAL
        ));
        return understanding;
    }

    private void alignUnderstandingContractDefaults(
            VisionPromptUnderstandingResult understanding,
            String rawPrompt,
            String contractVersion,
            String translationProvider
    ) {
        if (understanding == null) {
            return;
        }
        understanding.setOriginalPrompt(rawPrompt);
        understanding.setTranslationProvider(translationProvider);
        if (understanding.getNormalizedPrompt() == null || understanding.getNormalizedPrompt().isBlank()) {
            understanding.setNormalizedPrompt(rawPrompt);
        } else {
            understanding.setNormalizedPrompt(understanding.getNormalizedPrompt().trim());
        }
        if (understanding.getSourceLanguage() == null || understanding.getSourceLanguage().isBlank()) {
            understanding.setSourceLanguage("unknown");
        }
        if (understanding.getSemanticContractVersion() == null || understanding.getSemanticContractVersion().isBlank()) {
            understanding.setSemanticContractVersion(contractVersion);
        }
        if (understanding.getSemanticPlan() == null) {
            understanding.setSemanticPlan(VisionSemanticPlan.empty());
        }
    }

    private void applySafeOpenAiRescueIfNeeded(VisionPromptUnderstandingResult understanding) {
        if (!developmentFixtureEnabled()
                || understanding == null || understanding.semanticPlanOrEmpty().candidateIntentOrUnsupported() != VisionIntent.UNSUPPORTED) {
            return;
        }
        String normalizedPrompt = understanding.getNormalizedPrompt() == null ? "" : understanding.getNormalizedPrompt().trim();
        VisionSemanticPlan rescuePlan = emergencyLocalPlan(
                normalizedPrompt,
                isLikelyEnglishPrompt(normalizedPrompt),
                "OpenAI semantic understanding stayed unsupported, so only emergency local routing was attempted."
        );
        if (rescuePlan.candidateIntentOrUnsupported() == VisionIntent.UNSUPPORTED) {
            understanding.setSemanticPlan(rescuePlan);
            understanding.setUnderstandingStatus(UNDERSTANDING_STATUS_OPENAI_UNSUPPORTED);
            return;
        }
        understanding.setSemanticPlan(rescuePlan);
        understanding.setUnderstandingStatus(UNDERSTANDING_STATUS_OPENAI_LOCAL_RESCUE);
    }

    private VisionSemanticPlan emergencyLocalPlan(String normalizedPrompt, boolean englishPrompt, String failureReason) {
        if (!englishPrompt) {
            return unsupportedPlan(failureReason + " Local emergency mode only accepts English prompts.");
        }

        VisionSemanticPlan candidatePlan = VisionSemanticPlan.from(promptSemanticsSupport.inferPlan(normalizedPrompt));
        VisionIntent candidateIntent = candidatePlan.candidateIntentOrUnsupported();
        if (candidateIntent == VisionIntent.UNSUPPORTED) {
            return unsupportedPlan(failureReason + " Local emergency mode could not classify this prompt safely.");
        }
        if (!SAFE_LOCAL_EMERGENCY_INTENTS.contains(candidateIntent)) {
            return unsupportedPlan(failureReason + " Local emergency mode blocks mutation and workflow-changing intents until OpenAI understanding is available again.");
        }
        candidatePlan.setPlanningNote("Emergency local routing handled a safe English prompt while OpenAI semantic understanding was unavailable.");
        return candidatePlan;
    }

    private VisionSemanticPlan unsupportedPlan(String planningNote) {
        VisionSemanticPlan plan = VisionSemanticPlan.empty();
        plan.setPlanningNote(planningNote == null ? "" : planningNote.trim());
        return plan;
    }

    private boolean isLikelyEnglishPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return true;
        }
        return prompt.chars().allMatch(codePoint -> codePoint < 128);
    }

    private boolean isConfigured() {
        return "openai".equalsIgnoreCase(agentProperties.getProvider())
                && agentProperties.getApiKey() != null
                && !agentProperties.getApiKey().isBlank();
    }

    private boolean developmentFixtureEnabled() {
        return agentProperties.isLocalEmergencyEnabled()
                && !"production".equalsIgnoreCase(System.getProperty("spring.profiles.active", ""));
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

    private Map<String, Object> responseContract() {
        Map<String, Object> responseContract = new LinkedHashMap<>();
        responseContract.put("format", "VisionPromptUnderstandingResult");
        responseContract.put("semanticContractVersion", SEMANTIC_CONTRACT_VERSION);
        responseContract.put("candidateIntents", semanticRouteCatalogService.supportedCandidateIntents());
        responseContract.put("clarificationRequired", java.util.List.of(Boolean.TRUE, Boolean.FALSE));
        responseContract.put("requiredSlotIds", java.util.List.of());
        responseContract.put("missingRequiredSlotIds", java.util.List.of());
        responseContract.put("capabilityIds", semanticRouteCatalogService.supportedCapabilityIds());
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
                "search_query",
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
