package com.themuffinman.app.agent.sandbox;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SandboxGenerationPlanner {

    public SandboxGenerationPlan planFor(String normalizedPrompt, boolean userCreationMentioned) {
        if (!mentionsQuestGeneration(normalizedPrompt)) {
            return SandboxGenerationPlan.empty();
        }

        return new SandboxGenerationPlan(
                List.of("sandbox_generation"),
                userCreationMentioned
                        ? List.of("create_user_with_quests", "create_sandbox_user_with_circle_and_quest_flow")
                        : List.of("create_sandbox_user_with_circle_and_quest_flow"),
                List.of("Synthetic generation must stay admin-only and keep a synthetic marker strategy."),
                List.of("synthetic marker strategy"),
                List.of("Review sandbox marker rules before enabling execution.")
        );
    }

    private boolean mentionsQuestGeneration(String normalizedPrompt) {
        return mentionsQuest(normalizedPrompt)
                && (normalizedPrompt.contains("generate")
                || normalizedPrompt.contains("create ")
                || normalizedPrompt.contains("napravi ")
                || normalizedPrompt.contains("batch")
                || normalizedPrompt.contains("unikat")
                || normalizedPrompt.contains("unique")
                || normalizedPrompt.contains("new quest")
                || normalizedPrompt.contains("novi quest")
                || normalizedPrompt.contains("novu quest"));
    }

    private boolean mentionsQuest(String normalizedPrompt) {
        return normalizedPrompt.contains("quest")
                || normalizedPrompt.contains("questa")
                || normalizedPrompt.contains("questa ")
                || normalizedPrompt.contains("questove")
                || normalizedPrompt.contains("questova")
                || normalizedPrompt.contains("job");
    }
}
