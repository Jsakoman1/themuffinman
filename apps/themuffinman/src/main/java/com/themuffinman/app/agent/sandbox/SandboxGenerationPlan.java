package com.themuffinman.app.agent.sandbox;

import java.util.List;

public record SandboxGenerationPlan(
        List<String> matchedSignals,
        List<String> suggestedWorkflows,
        List<String> warnings,
        List<String> unresolvedInputs,
        List<String> nextSteps
) {

    public static SandboxGenerationPlan empty() {
        return new SandboxGenerationPlan(List.of(), List.of(), List.of(), List.of(), List.of());
    }

    public boolean isEmpty() {
        return matchedSignals.isEmpty()
                && suggestedWorkflows.isEmpty()
                && warnings.isEmpty()
                && unresolvedInputs.isEmpty()
                && nextSteps.isEmpty();
    }
}
