package com.themuffinman.app.agent.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminSyntheticQuestExecutionPlannerTest {

    private final AdminSyntheticQuestExecutionPlanner planner = new AdminSyntheticQuestExecutionPlanner();

    @Test
    void extractsSyntheticQuestBatchExecutionShape() {
        AdminSyntheticQuestExecutionPlan plan = planner.plan(AdminAgentPromptTranslation.builder()
                .originalPrompt("Generate 10 unique synthetic quests for user Josip about moving help")
                .translatedPrompt("Generate 10 unique synthetic quests for user Josip about moving help")
                .translationReliable(true)
                .build());

        assertEquals(AdminAgentSurfacePolicy.SYNTHETIC_QUEST_BATCH_CAPABILITY, plan.getCapabilityId());
        assertEquals("Josip", plan.getTargetUserQuery());
        assertEquals(10, plan.getRequestedCount());
        assertEquals("moving help", plan.getTopic());
        assertTrue(plan.isExecutable());
    }

    @Test
    void blocksPromptWithoutTargetUser() {
        AdminSyntheticQuestExecutionPlan plan = planner.plan(AdminAgentPromptTranslation.builder()
                .originalPrompt("Generate 4 synthetic quests")
                .translatedPrompt("Generate 4 synthetic quests")
                .translationReliable(true)
                .build());

        assertTrue(plan.getBlockingReasons().stream().anyMatch(item -> item.contains("target user")));
    }
}
