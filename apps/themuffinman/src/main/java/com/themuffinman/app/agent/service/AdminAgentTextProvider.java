package com.themuffinman.app.agent.service;

import com.themuffinman.app.prompt.PromptSemanticPlan;

import java.util.List;

public interface AdminAgentTextProvider {

    String providerName();

    boolean isConfigured();

    String generatePlanningSummary(
            String prompt,
            List<String> suggestedWorkflows,
            List<String> matchedSignals,
            List<String> unresolvedInputs,
            List<String> warnings
    );

    default String generatePlanningSummary(
            String prompt,
            List<String> suggestedWorkflows,
            List<String> matchedSignals,
            List<String> unresolvedInputs,
            List<String> warnings,
            AgentModelProfile modelProfile
    ) {
        return generatePlanningSummary(prompt, suggestedWorkflows, matchedSignals, unresolvedInputs, warnings);
    }

    default AdminAgentPromptTranslation translatePromptToEnglish(String prompt) {
        return AdminAgentPromptTranslation.builder()
                .sourceLanguage("unknown")
                .originalPrompt(prompt)
                .translatedPrompt(prompt)
                .translationProvider(providerName())
                .translationApplied(false)
                .translationReliable(false)
                .semanticPlan(PromptSemanticPlan.empty())
                .build();
    }
}
