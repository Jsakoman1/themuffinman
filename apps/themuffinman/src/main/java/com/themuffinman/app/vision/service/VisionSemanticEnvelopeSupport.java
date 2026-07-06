package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.semantic.SemanticEntityResolution;
import com.themuffinman.app.semantic.SemanticEnvelope;
import com.themuffinman.app.semantic.SemanticReplayRecord;
import com.themuffinman.app.semantic.VisionEntityResolverRegistry;
import com.themuffinman.app.vision.model.VisionIntent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class VisionSemanticEnvelopeSupport {

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

    private final VisionSemanticRouteCatalogService semanticRouteCatalogService;
    private final VisionEntityResolverRegistry visionEntityResolverRegistry;
    private final SemanticAliasRegistry semanticAliasRegistry;

    VisionSemanticEnvelopeSupport(
            VisionSemanticRouteCatalogService semanticRouteCatalogService,
            VisionEntityResolverRegistry visionEntityResolverRegistry,
            SemanticAliasRegistry semanticAliasRegistry
    ) {
        this.semanticRouteCatalogService = semanticRouteCatalogService;
        this.visionEntityResolverRegistry = visionEntityResolverRegistry;
        this.semanticAliasRegistry = semanticAliasRegistry;
    }

    SemanticEnvelope buildSemanticEnvelope(
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

    List<String> missingRequiredSlotIds(VisionSemanticRouteDescriptor route, VisionPromptUnderstandingResult understanding) {
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
}
