package com.themuffinman.app.agent.service;

import com.themuffinman.app.prompt.PromptSemanticPlan;
import com.themuffinman.app.semantic.SemanticEnvelope;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminAgentPromptTranslation {
    private final String sourceLanguage;
    private final String originalPrompt;
    private final String translatedPrompt;
    private final String translationProvider;
    private final boolean translationApplied;
    private final boolean translationReliable;
    @Builder.Default
    private final SemanticEnvelope semanticEnvelope = SemanticEnvelope.builder().build();
    @Builder.Default
    private final PromptSemanticPlan semanticPlan = PromptSemanticPlan.empty();

    public PromptSemanticPlan semanticPlanOrEmpty() {
        return semanticPlan == null ? PromptSemanticPlan.empty() : semanticPlan;
    }

    public SemanticEnvelope semanticEnvelopeOrEmpty() {
        return semanticEnvelope == null ? SemanticEnvelope.builder().build() : semanticEnvelope;
    }

    public AdminAgentPromptTranslation withSemanticPlan(PromptSemanticPlan semanticPlan) {
        return AdminAgentPromptTranslation.builder()
                .sourceLanguage(sourceLanguage)
                .originalPrompt(originalPrompt)
                .translatedPrompt(translatedPrompt)
                .translationProvider(translationProvider)
                .translationApplied(translationApplied)
                .translationReliable(translationReliable)
                .semanticEnvelope(semanticEnvelopeOrEmpty())
                .semanticPlan(semanticPlan == null ? PromptSemanticPlan.empty() : semanticPlan)
                .build();
    }
}
