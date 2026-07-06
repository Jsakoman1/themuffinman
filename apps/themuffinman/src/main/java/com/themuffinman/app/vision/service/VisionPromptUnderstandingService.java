package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.semantic.SemanticEnvelope;
import com.themuffinman.app.semantic.SemanticEntityResolution;
import com.themuffinman.app.semantic.SemanticEntityResolutionStatus;
import com.themuffinman.app.semantic.SemanticReplayRecord;
import com.themuffinman.app.semantic.VisionEntityResolverRegistry;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.EnumSet;
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

    private static final Set<VisionIntent> SAFE_LOCAL_EMERGENCY_INTENTS = EnumSet.of(
            VisionIntent.CREATE_QUEST,
            VisionIntent.CREATE_CIRCLE,
            VisionIntent.CREATE_APPLICATION,
            VisionIntent.UPDATE_PROFILE,
            VisionIntent.DISCOVER_QUESTS,
            VisionIntent.OPEN_CHAT,
            VisionIntent.VIEW_CHAT_WORKSPACE,
            VisionIntent.VIEW_SETTINGS,
            VisionIntent.VIEW_PROFILE,
            VisionIntent.VIEW_USER_PROFILE,
            VisionIntent.VIEW_CIRCLES,
            VisionIntent.VIEW_CIRCLE_DETAIL,
            VisionIntent.VIEW_QUEST_DETAIL,
            VisionIntent.VIEW_NOTIFICATIONS,
            VisionIntent.VIEW_QUEST_NEWS,
            VisionIntent.VIEW_APPLICATIONS,
            VisionIntent.VIEW_APPLICATION_DETAIL
    );
    private static final Map<SemanticEntityFamily, List<String>> TARGET_QUERY_PRIORITY = Map.of(
            SemanticEntityFamily.USER, List.of("plan:targetUserQuery", "slot:profile_username", "slot:profile_description"),
            SemanticEntityFamily.CIRCLE, List.of("slot:target_circle_query", "slot:circle_name"),
            SemanticEntityFamily.QUEST, List.of("slot:target_quest_query", "plan:searchQuery", "slot:quest_title", "slot:quest_description"),
            SemanticEntityFamily.APPLICATION, List.of("slot:target_application_query", "slot:target_quest_query", "plan:searchQuery"),
            SemanticEntityFamily.PROFILE, List.of("slot:profile_username", "slot:profile_description", "plan:targetUserQuery")
    );
    private static final Map<String, List<String>> REQUIRED_SLOT_FALLBACKS = Map.of(
            "target_user", List.of("profile_username", "profile_description", "plan:targetUserQuery"),
            "reward_amount", List.of("free_quest")
    );
    private static final Map<SemanticEntityFamily, Set<String>> NORMALIZED_TARGET_SLOT_IDS = Map.of(
            SemanticEntityFamily.USER, Set.of("target_user", "profile_username", "profile_description"),
            SemanticEntityFamily.CIRCLE, Set.of("target_circle_query", "circle_name"),
            SemanticEntityFamily.QUEST, Set.of("target_quest_query"),
            SemanticEntityFamily.APPLICATION, Set.of("target_application_query", "target_quest_query"),
            SemanticEntityFamily.PROFILE, Set.of("profile_username", "profile_description")
    );

    private final AgentProperties agentProperties;
    private final VisionSemanticMapper visionSemanticMapper;
    private final PromptSemanticsSupport promptSemanticsSupport;
    private final VisionSemanticOrchestrationContextService semanticContextService;
    private final VisionSemanticRouteCatalogService semanticRouteCatalogService;
    private final VisionSemanticContractSanitizer semanticContractSanitizer;
    private final VisionSemanticResponseValidator semanticResponseValidator;
    private final VisionEntityResolverRegistry visionEntityResolverRegistry;
    private final SemanticAliasRegistry semanticAliasRegistry;
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
            SemanticAliasRegistry semanticAliasRegistry
    ) {
        this.agentProperties = agentProperties;
        this.visionSemanticMapper = visionSemanticMapper;
        this.promptSemanticsSupport = promptSemanticsSupport;
        this.semanticContextService = semanticContextService;
        this.semanticRouteCatalogService = semanticRouteCatalogService;
        this.semanticContractSanitizer = semanticContractSanitizer;
        this.semanticResponseValidator = semanticResponseValidator;
        this.visionEntityResolverRegistry = visionEntityResolverRegistry;
        this.semanticAliasRegistry = semanticAliasRegistry;
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
            return localEmergencyUnderstand(trimmed, conversation, currentUser,
                    "OpenAI semantic understanding is not configured, so only emergency local routing is available.");
        }

        try {
                return openAiUnderstand(orchestrationRequest, conversation, currentUser);
        } catch (RestClientException exception) {
            return localEmergencyUnderstand(trimmed, conversation, currentUser,
                    "OpenAI semantic understanding is temporarily unavailable, so only emergency local routing is available.");
        } catch (RuntimeException exception) {
            return localEmergencyUnderstand(trimmed, conversation, currentUser,
                    "OpenAI semantic understanding failed for this turn, so only emergency local routing is available.");
        }
    }

    private VisionPromptUnderstandingResult openAiUnderstand(
            VisionSemanticOrchestrationRequest orchestrationRequest,
            VisionConversation conversation,
            AppUser currentUser
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", resolveSemanticModel());
        payload.put("input", buildInput(orchestrationRequest));
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
            understanding.setSemanticEnvelope(buildSemanticEnvelope(
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
                """.formatted(repairSlotId, buildInput(repairRequest));
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

        for (String missingSlotId : missingRequiredSlotIds(route, understanding)) {
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
        understanding.setSemanticEnvelope(buildSemanticEnvelope(
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
        if (understanding == null || understanding.semanticPlanOrEmpty().candidateIntentOrUnsupported() != VisionIntent.UNSUPPORTED) {
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

    private String providerName() {
        return "openai";
    }

    private SemanticEnvelope buildSemanticEnvelope(
            String rawPrompt,
            VisionPromptUnderstandingResult understanding,
            AppUser currentUser,
            String translationProvider,
            boolean translationApplied,
            boolean translationReliable,
            String contractVersion,
            String modelName
    ) {
        VisionSemanticPlan semanticPlan = understanding == null ? VisionSemanticPlan.empty() : understanding.semanticPlanOrEmpty();
        VisionIntent candidateIntent = semanticPlan.candidateIntentOrUnsupported();
        VisionSemanticRouteDescriptor selectedRoute = semanticRouteCatalogService.routeForIntent(candidateIntent.name());
        List<String> requiredSlotIds = selectedRoute == null ? List.of() : semanticRouteCatalogService.requiredSlotIds(selectedRoute);
        List<String> missingRequiredSlotIds = missingRequiredSlotIds(selectedRoute, understanding);
        SemanticEntityResolution<?> entityResolution = resolveEntityResolution(currentUser, semanticPlan, understanding, candidateIntent);
        boolean entityClarificationRequired = requiresEntityClarification(candidateIntent, entityResolution);
        boolean clarificationRequired = !missingRequiredSlotIds.isEmpty()
                || (understanding != null && understanding.isClarificationRequired())
                || entityClarificationRequired;
        if (understanding != null) {
            understanding.setClarificationRequired(clarificationRequired);
            understanding.setRequiredSlotIds(requiredSlotIds);
            understanding.setMissingRequiredSlotIds(missingRequiredSlotIds);
            if (understanding.getSemanticContractVersion() == null || understanding.getSemanticContractVersion().isBlank()) {
                understanding.setSemanticContractVersion(contractVersion);
            }
        }
        String normalizedPrompt = understanding == null || understanding.getNormalizedPrompt() == null
                ? rawPrompt
                : understanding.getNormalizedPrompt();
        SemanticReplayRecord replayRecord = SemanticReplayRecord.builder()
                .contractVersion(contractVersion)
                .provider(translationProvider)
                .model(modelName)
                .sourceLanguage(understanding == null ? "unknown" : understanding.getSourceLanguage())
                .rawUserText(rawPrompt)
                .normalizedEnglishText(normalizedPrompt)
                .intent(candidateIntent.name())
                .entityFamily(resolveEntityFamily(candidateIntent))
                .targetEntityQuery(resolveTargetEntityQuery(candidateIntent, semanticPlan, understanding))
                .entityResolutionStatus(entityResolution == null ? null : entityResolution.getStatus())
                .entityResolutionLabel(entityResolution == null ? null : entityResolution.getCanonicalLabel())
                .entityResolutionConfidence(entityResolution == null ? null : entityResolution.getConfidence())
                .entityResolutionReason(entityResolution == null ? null : entityResolution.getAmbiguityReason())
                .requiredSlotIds(requiredSlotIds)
                .missingRequiredSlotIds(missingRequiredSlotIds)
                .clarificationRequired(clarificationRequired)
                .confidence(semanticPlan.getCandidateIntentConfidence())
                .ambiguityReason(semanticPlan.getPlanningNote())
                .capturedAt(Instant.now().toString())
                .build();
        return SemanticEnvelope.builder()
                .rawUserText(rawPrompt)
                .normalizedEnglishText(normalizedPrompt)
                .localizedDisplayText(rawPrompt)
                .sourceLanguage(understanding == null ? "unknown" : understanding.getSourceLanguage())
                .contractVersion(contractVersion)
                .translationProvider(translationProvider)
                .translationApplied(translationApplied)
                .translationReliable(translationReliable)
                .intent(candidateIntent.name())
                .entityFamily(resolveEntityFamily(candidateIntent))
                .targetEntityQuery(resolveTargetEntityQuery(candidateIntent, semanticPlan, understanding))
                .entityResolutionStatus(entityResolution == null ? null : entityResolution.getStatus())
                .entityResolutionLabel(entityResolution == null ? null : entityResolution.getCanonicalLabel())
                .entityResolutionConfidence(entityResolution == null ? null : entityResolution.getConfidence())
                .entityResolutionReason(entityResolution == null ? null : entityResolution.getAmbiguityReason())
                .slotCandidates(canonicalizeSlotCandidates(candidateIntent, understanding))
                .confidence(semanticPlan.getCandidateIntentConfidence())
                .ambiguityReason(semanticPlan.getPlanningNote())
                .clarificationRequired(clarificationRequired)
                .requiredSlotIds(requiredSlotIds)
                .missingRequiredSlotIds(missingRequiredSlotIds)
                .replayRecord(replayRecord)
                .build();
    }

    private List<String> missingRequiredSlotIds(VisionSemanticRouteDescriptor route, VisionPromptUnderstandingResult understanding) {
        if (route == null || understanding == null) {
            return List.of();
        }

        List<String> missing = new ArrayList<>();
        Map<String, String> slotCandidates = understanding.toExtractedSlotMap();
        VisionSemanticPlan semanticPlan = understanding.semanticPlanOrEmpty();
        for (String slotId : semanticRouteCatalogService.requiredSlotIds(route)) {
            if (!hasRequiredSlotValue(slotId, semanticPlan, slotCandidates)) {
                missing.add(slotId);
            }
        }
        return missing;
    }

    private boolean hasRequiredSlotValue(String slotId, VisionSemanticPlan semanticPlan, Map<String, String> slotCandidates) {
        if (slotId == null || slotId.isBlank()) {
            return true;
        }
        if (hasValue(slotCandidates.get(slotId))) {
            return true;
        }
        return REQUIRED_SLOT_FALLBACKS.getOrDefault(slotId, List.of()).stream()
                .anyMatch(candidate -> hasValue(resolveSlotOrPlanCandidate(candidate, semanticPlan, slotCandidates)));
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private SemanticEntityFamily resolveEntityFamily(VisionIntent intent) {
        return semanticRouteCatalogService.entityFamilyForIntent(intent);
    }

    private SemanticEntityFamily resolveTargetEntityFamily(VisionIntent intent) {
        return semanticRouteCatalogService.targetEntityFamilyForIntent(intent);
    }

    private SemanticEntityResolution<?> resolveEntityResolution(
            AppUser currentUser,
            VisionSemanticPlan semanticPlan,
            VisionPromptUnderstandingResult understanding,
            VisionIntent candidateIntent
    ) {
        if (semanticPlan == null || candidateIntent == null || !semanticRouteCatalogService.requiresTargetEntityResolution(candidateIntent)) {
            return null;
        }

        SemanticEntityFamily entityFamily = resolveTargetEntityFamily(candidateIntent);
        if (entityFamily == SemanticEntityFamily.UNKNOWN) {
            return null;
        }

        String targetEntityQuery = resolveTargetEntityQuery(candidateIntent, semanticPlan, understanding);
        if (targetEntityQuery == null || targetEntityQuery.isBlank()) {
            return null;
        }

        return visionEntityResolverRegistry.resolve(entityFamily, currentUser, targetEntityQuery);
    }

    private boolean requiresEntityClarification(VisionIntent candidateIntent, SemanticEntityResolution<?> entityResolution) {
        if (candidateIntent == null || entityResolution == null) {
            return false;
        }
        if (entityResolution.ambiguous() || entityResolution.notFound()) {
            return true;
        }
        Double confidence = entityResolution.getConfidence();
        if (confidence == null) {
            return false;
        }
        return confidence <= semanticRouteCatalogService.minimumEntityResolutionConfidenceForIntent(candidateIntent);
    }

    private String resolveTargetEntityQuery(
            VisionIntent candidateIntent,
            VisionSemanticPlan semanticPlan,
            VisionPromptUnderstandingResult understanding
    ) {
        Map<String, String> slotCandidates = understanding == null ? Map.of() : understanding.toExtractedSlotMap();
        SemanticEntityFamily entityFamily = resolveTargetEntityFamily(candidateIntent);
        List<String> resolvedCandidates = new ArrayList<>();
        for (String candidateSource : TARGET_QUERY_PRIORITY.getOrDefault(entityFamily, List.of())) {
            resolvedCandidates.add(resolveSlotOrPlanCandidate(candidateSource, semanticPlan, slotCandidates));
        }
        String resolvedQuery = firstNonBlank(resolvedCandidates.toArray(String[]::new));
        return normalizeTargetEntityQuery(entityFamily, resolvedQuery);
    }

    private Map<String, String> canonicalizeSlotCandidates(VisionIntent candidateIntent, VisionPromptUnderstandingResult understanding) {
        if (understanding == null) {
            return Map.of();
        }

        Map<String, String> source = understanding.toExtractedSlotMap();
        if (source.isEmpty()) {
            return source;
        }

        SemanticEntityFamily entityFamily = resolveTargetEntityFamily(candidateIntent);
        Map<String, String> canonical = new LinkedHashMap<>();
        source.forEach((key, value) -> canonical.put(key, canonicalizeSlotValue(entityFamily, key, value)));
        return canonical;
    }

    private String canonicalizeSlotValue(SemanticEntityFamily entityFamily, String slotId, String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (shouldNormalizeTargetSlot(entityFamily, slotId)) {
            return normalizeTargetEntityQuery(entityFamily, value);
        }
        return value.trim();
    }

    private boolean shouldNormalizeTargetSlot(SemanticEntityFamily entityFamily, String slotId) {
        if (entityFamily == null || slotId == null || slotId.isBlank()) {
            return false;
        }
        return NORMALIZED_TARGET_SLOT_IDS.getOrDefault(entityFamily, Set.of()).contains(slotId);
    }

    private String semanticPlanValue(VisionSemanticPlan semanticPlan, String fieldName) {
        if (semanticPlan == null || fieldName == null || fieldName.isBlank()) {
            return null;
        }
        return switch (fieldName) {
            case "targetUserQuery" -> semanticPlan.getTargetUserQuery();
            case "searchQuery" -> semanticPlan.getSearchQuery();
            default -> null;
        };
    }

    private String resolveSlotOrPlanCandidate(String candidateSource, VisionSemanticPlan semanticPlan, Map<String, String> slotCandidates) {
        if (candidateSource == null || candidateSource.isBlank()) {
            return null;
        }
        if (candidateSource.startsWith("plan:")) {
            return semanticPlanValue(semanticPlan, candidateSource.substring("plan:".length()));
        }
        if (candidateSource.startsWith("slot:")) {
            return slotCandidates.get(candidateSource.substring("slot:".length()));
        }
        return slotCandidates.get(candidateSource);
    }

    private String normalizeTargetEntityQuery(SemanticEntityFamily family, String query) {
        if (query == null || query.isBlank()) {
            return query;
        }
        if (family == null || family == SemanticEntityFamily.UNKNOWN) {
            return query.trim();
        }
        return semanticAliasRegistry.normalizeQuery(family, query).trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
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
                "user", semanticAliasRegistry.aliasesFor(SemanticEntityFamily.USER)
        ));
        return compact;
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
                Use memoryContext.userMemory for stable user preferences, learned input-type habits, preferred entity family, and compact learning summary signals.
                Use memoryContext.sessionMemory for the current thread, open questions, last prompts, and slot state.
                Use memoryContext.recentConversations only as lightweight reminders when topics switch.
                Use runtimeContext to know whether the turn came from text or voice.
                Use conversationContext for already collected slot data, activeSlot, and draftSnapshot.
                Use semanticHints.familyAliases to map multilingual aliases and paraphrases to the correct family before choosing targetEntityQuery.
                Use route examples and slot aliases to resolve the shortest valid slot value from the user wording.
                Use slot anti-examples to avoid mapping instruction words into slot values.
                Determine candidateIntent and capability first.
                Extract only values that are explicit or directly implied.
                When an intent phrase is followed by an entity phrase, fill the matching slot from the shortest cleaned entity phrase and do not include the intent words in the slot value.
                Example: create new circle Lover -> circleName = Lover; open circle Family -> targetCircleQuery = Family; show application #42 -> targetApplicationQuery = #42; show user Josip -> profileUsername = Josip.
                Use the route examples, slot aliases, slot descriptions, and allowed values from allowedRoutes as the source of truth for each slot.
                Normalize the internal semantic meaning into English only.
                Always return normalizedPrompt in English, even if the user spoke another language or used broken grammar.
                If the prompt is vague or slot-follow-up, prefer the current session entity family unless the user clearly changes topic.
                If required slots are missing or the target entity is ambiguous, set clarificationRequired to true and list missingRequiredSlotIds.
                Supported intents: CREATE_QUEST, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST, UPDATE_CIRCLE, DELETE_CIRCLE, CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION, DISCOVER_QUESTS, SEARCH, OPEN_CHAT, VIEW_PROFILE, VIEW_SETTINGS, VIEW_CIRCLES, VIEW_CIRCLE_DETAIL, VIEW_NOTIFICATIONS, VIEW_QUEST_NEWS, VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL, VIEW_THINGS, UNSUPPORTED.
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

    private Map<String, Object> responseContract() {
        Map<String, Object> responseContract = new LinkedHashMap<>();
        responseContract.put("format", "VisionPromptUnderstandingResult");
        responseContract.put("semanticContractVersion", SEMANTIC_CONTRACT_VERSION);
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
                "SEARCH",
                "OPEN_CHAT",
                "VIEW_CHAT_WORKSPACE",
                "VIEW_PROFILE",
                "VIEW_SETTINGS",
                "VIEW_USER_PROFILE",
                "VIEW_CIRCLES",
                "VIEW_CIRCLE_DETAIL",
                "VIEW_QUEST_DETAIL",
                "VIEW_NOTIFICATIONS",
                "VIEW_QUEST_NEWS",
                "VIEW_APPLICATIONS",
                "VIEW_APPLICATION_DETAIL",
                "VIEW_THINGS",
                "UNSUPPORTED"
        ));
        responseContract.put("clarificationRequired", java.util.List.of(Boolean.TRUE, Boolean.FALSE));
        responseContract.put("requiredSlotIds", java.util.List.of());
        responseContract.put("missingRequiredSlotIds", java.util.List.of());
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
                "search",
                "open_chat",
                "view_chat_workspace",
                "view_profile",
                "view_settings",
                "view_user_profile",
                "view_circles",
                "view_circle_detail",
                "view_quest_detail",
                "view_notifications",
                "view_quest_news",
                "view_applications",
                "view_application_detail",
                "view_things",
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
