package com.themuffinman.app.agent.sandbox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SandboxGenerationPlannerTest {

    private final SandboxGenerationPlanner planner = new SandboxGenerationPlanner();

    @Test
    void contributesSandboxOnlyWorkflowForQuestGenerationPrompt() {
        SandboxGenerationPlan plan = planner.planFor("generate 20 unique quests tomorrow", false);

        assertTrue(plan.matchedSignals().contains("sandbox_generation"));
        assertTrue(plan.suggestedWorkflows().contains("create_sandbox_user_with_circle_and_quest_flow"));
        assertTrue(plan.warnings().stream().anyMatch(item -> item.contains("Synthetic generation")));
        assertTrue(plan.unresolvedInputs().contains("synthetic marker strategy"));
    }

    @Test
    void addsUserQuestBatchWorkflowWhenPromptAlsoCreatesUsers() {
        SandboxGenerationPlan plan = planner.planFor("create user and generate quests", true);

        assertTrue(plan.suggestedWorkflows().contains("create_user_with_quests"));
        assertTrue(plan.suggestedWorkflows().contains("create_sandbox_user_with_circle_and_quest_flow"));
    }

    @Test
    void ignoresNonGenerationPrompt() {
        SandboxGenerationPlan plan = planner.planFor("approve the first applicant", false);

        assertEquals(SandboxGenerationPlan.empty(), plan);
    }
}
