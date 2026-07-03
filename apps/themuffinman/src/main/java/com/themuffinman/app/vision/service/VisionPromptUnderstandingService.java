package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
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
            VisionIntent.DISCOVER_QUESTS,
            VisionIntent.OPEN_CHAT,
            VisionIntent.VIEW_CHAT_WORKSPACE,
            VisionIntent.VIEW_SETTINGS,
            VisionIntent.VIEW_PROFILE,
            VisionIntent.VIEW_USER_PROFILE,
            VisionIntent.VIEW_CIRCLES,
            VisionIntent.VIEW_CIRCLE_DETAIL,
            VisionIntent.VIEW_QUEST_DETAIL,
            VisionIntent.VIEW_APPLICATIONS,
            VisionIntent.VIEW_APPLICATION_DETAIL
    );

    private final AgentProperties agentProperties;
    private final VisionSemanticMapper visionSemanticMapper;
    private final PromptSemanticsSupport promptSemanticsSupport;
    private final VisionSemanticOrchestrationContextService semanticContextService;
    private final VisionSemanticRouteCatalogService semanticRouteCatalogService;
    private final VisionSemanticContractSanitizer semanticContractSanitizer;
    private final VisionSemanticResponseValidator semanticResponseValidator;
    private final VisionEntityResolverRegistry visionEntityResolverRegistry;
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
            VisionEntityResolverRegistry visionEntityResolverRegistry
    ) {
        this.agentProperties = agentProperties;
        this.visionSemanticMapper = visionSemanticMapper;
        this.promptSemanticsSupport = promptSemanticsSupport;
        this.semanticContextService = semanticContextService;
        this.semanticRouteCatalogService = semanticRouteCatalogService;
        this.semanticContractSanitizer = semanticContractSanitizer;
        this.semanticResponseValidator = semanticResponseValidator;
        this.visionEntityResolverRegistry = visionEntityResolverRegistry;
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
            understanding.setOriginalPrompt(orchestrationRequest.getRawPrompt());
            understanding.setTranslationProvider(providerName());
            understanding.setUnderstandingProvider(UNDERSTANDING_PROVIDER_OPENAI);
            understanding.setUnderstandingStatus(UNDERSTANDING_STATUS_OPENAI_PRIMARY);
            understanding.setSemanticContractVersion(orchestrationRequest.getContractVersion());
            if (understanding.getNormalizedPrompt() == null || understanding.getNormalizedPrompt().isBlank()) {
                understanding.setNormalizedPrompt(orchestrationRequest.getRawPrompt());
            } else {
                understanding.setNormalizedPrompt(understanding.getNormalizedPrompt().trim());
            }
            if (understanding.getSourceLanguage() == null || understanding.getSourceLanguage().isBlank()) {
                understanding.setSourceLanguage("unknown");
            }
            if (understanding.getSemanticPlan() == null) {
                understanding.setSemanticPlan(VisionSemanticPlan.empty());
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
                .slotCandidates(understanding == null ? Map.of() : understanding.toExtractedSlotMap())
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
        return switch (slotId) {
            case "target_user" -> hasValue(semanticPlan.getTargetUserQuery()) || hasValue(slotCandidates.get(slotId));
            case "reward_amount" -> hasValue(slotCandidates.get(slotId)) || hasValue(slotCandidates.get("free_quest"));
            default -> hasValue(slotCandidates.get(slotId));
        };
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
        return confidence < semanticRouteCatalogService.minimumEntityResolutionConfidenceForIntent(candidateIntent);
    }

    private String resolveTargetEntityQuery(
            VisionIntent candidateIntent,
            VisionSemanticPlan semanticPlan,
            VisionPromptUnderstandingResult understanding
    ) {
        Map<String, String> slotCandidates = understanding == null ? Map.of() : understanding.toExtractedSlotMap();
        SemanticEntityFamily entityFamily = resolveTargetEntityFamily(candidateIntent);
        return switch (entityFamily) {
            case USER -> firstNonBlank(semanticPlan.getTargetUserQuery(), slotCandidates.get("profile_username"));
            case CIRCLE -> firstNonBlank(slotCandidates.get("target_circle_query"), slotCandidates.get("circle_name"));
            case QUEST -> firstNonBlank(slotCandidates.get("target_quest_query"), semanticPlan.getSearchQuery(), slotCandidates.get("quest_title"));
            case APPLICATION -> firstNonBlank(slotCandidates.get("target_application_query"), slotCandidates.get("target_quest_query"));
            case PROFILE -> firstNonBlank(slotCandidates.get("profile_username"), slotCandidates.get("profile_description"));
            default -> firstNonBlank(semanticPlan.getTargetUserQuery(), semanticPlan.getSearchQuery());
        };
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
                Normalize the internal semantic meaning into English only.
                Always return normalizedPrompt in English, even if the user spoke another language or used broken grammar.
                If the prompt is vague or slot-follow-up, prefer the current session entity family unless the user clearly changes topic.
                If required slots are missing or the target entity is ambiguous, set clarificationRequired to true and list missingRequiredSlotIds.
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
