package com.themuffinman.app.agent.service;

import com.themuffinman.app.prompt.PromptSemanticPlan;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import com.themuffinman.app.semantic.SemanticEnvelope;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.service.VisionSemanticRouteCatalogService;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AdminAgentPromptPreparationService {

    private final PromptSemanticsSupport promptSemanticsSupport;
    private final VisionSemanticRouteCatalogService visionSemanticRouteCatalogService;

    public AdminAgentPromptPreparationService(
            PromptSemanticsSupport promptSemanticsSupport,
            VisionSemanticRouteCatalogService visionSemanticRouteCatalogService
    ) {
        this.promptSemanticsSupport = promptSemanticsSupport;
        this.visionSemanticRouteCatalogService = visionSemanticRouteCatalogService;
    }

    public AdminAgentPromptTranslation preparePrompt(String prompt) {
        String normalizedPrompt = prompt == null ? "" : prompt.trim().replaceAll("\\s+", " ");
        String lower = TextValueNormalizer.lowerToEmpty(normalizedPrompt);
        boolean englishPrompt = isLikelyEnglish(lower);
        PromptSemanticPlan semanticPlan = promptSemanticsSupport.inferPlan(normalizedPrompt);
        SemanticEnvelope semanticEnvelope = SemanticEnvelope.builder()
                .rawUserText(normalizedPrompt)
                .normalizedEnglishText(normalizedPrompt)
                .localizedDisplayText(normalizedPrompt)
                .sourceLanguage(englishPrompt ? "en" : "unknown")
                .contractVersion("admin-agent-prompt-v1")
                .translationProvider("none")
                .translationApplied(false)
                .translationReliable(englishPrompt)
                .intent(semanticPlan.getCandidateIntent())
                .entityFamily(visionSemanticRouteCatalogService.entityFamilyForIntent(resolveIntent(semanticPlan)))
                .targetEntityQuery(resolveTargetEntityQuery(semanticPlan))
                .confidence(semanticPlan.getCandidateIntentConfidence())
                .ambiguityReason(semanticPlan.getPlanningNote())
                .clarificationRequired(false)
                .build();

        return AdminAgentPromptTranslation.builder()
                .sourceLanguage(englishPrompt ? "en" : "unknown")
                .originalPrompt(normalizedPrompt)
                .translatedPrompt(normalizedPrompt)
                .translationProvider("none")
                .translationApplied(false)
                .translationReliable(englishPrompt)
                .semanticEnvelope(semanticEnvelope)
                .semanticPlan(promptSemanticsSupport.inferPlan(normalizedPrompt))
                .build();
    }

    private boolean isLikelyEnglish(String normalizedPrompt) {
        return normalizedPrompt.chars().allMatch(codePoint -> codePoint < 128)
                && (normalizedPrompt.contains("quest")
                || normalizedPrompt.contains("generate")
                || normalizedPrompt.contains("create")
                || normalizedPrompt.contains("approve")
                || normalizedPrompt.contains("delete")
                || normalizedPrompt.contains("friend request")
                || normalizedPrompt.contains("profile")
                || normalizedPrompt.contains("circle")
                || normalizedPrompt.contains("application")
                || normalizedPrompt.contains("location")
                || normalizedPrompt.contains("message"));
    }

    private String resolveTargetEntityQuery(PromptSemanticPlan semanticPlan) {
        if (semanticPlan == null) {
            return null;
        }
        if (semanticPlan.getTargetUserQuery() != null && !semanticPlan.getTargetUserQuery().isBlank()) {
            return semanticPlan.getTargetUserQuery().trim();
        }
        if (semanticPlan.getSearchQuery() != null && !semanticPlan.getSearchQuery().isBlank()) {
            return semanticPlan.getSearchQuery().trim();
        }
        return null;
    }

    private VisionIntent resolveIntent(PromptSemanticPlan semanticPlan) {
        if (semanticPlan == null || semanticPlan.getCandidateIntent() == null || semanticPlan.getCandidateIntent().isBlank()) {
            return VisionIntent.UNSUPPORTED;
        }
        try {
            return VisionIntent.valueOf(TextValueNormalizer.upperTrimToEmpty(semanticPlan.getCandidateIntent()));
        } catch (IllegalArgumentException exception) {
            return VisionIntent.UNSUPPORTED;
        }
    }
}
