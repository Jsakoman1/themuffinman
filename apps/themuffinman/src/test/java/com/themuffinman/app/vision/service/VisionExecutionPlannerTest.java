package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionExecutionCandidateDTO;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.testing.VisionConversationTestBuilder;
import com.themuffinman.app.vision.testing.VisionSlotStatePresets;
import com.themuffinman.app.vision.service.VisionSemanticRouteCatalogService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionExecutionPlannerTest {

    @Test
    void plansReadyExecutionForReviewReadyCreateQuestConversation() {
        VisionProperties visionProperties = new VisionProperties();
        visionProperties.setExecutionEnabled(true);
        VisionExecutionPlanner planner = new VisionExecutionPlanner(new VisionClarificationService(), new VisionSemanticRouteCatalogService(), visionProperties);
        AppUser user = new AppUser();
        user.setId(7L);

        VisionExecutionCandidateDTO candidate = planner.plan(
                VisionConversationTestBuilder.createQuest(1L, user)
                        .status(VisionConversationStatus.REVIEW_READY)
                        .slots(VisionSlotStatePresets.createQuestReviewReadyProfileLocation())
                        .build(),
                VisionPromptUnderstandingResult.builder()
                        .semanticPlan(VisionSemanticPlan.createQuest(0.91d, "semantic plan note"))
                        .build()
        );

        assertEquals("CREATE_QUEST", candidate.getCandidateIntent());
        assertEquals("create_quest", candidate.getCapabilityId());
        assertTrue(candidate.isReviewReady());
        assertTrue(candidate.isExecutionReady());
        assertTrue(candidate.isConfirmationRequired());
        assertTrue(candidate.getBlockingReason() == null || candidate.getBlockingReason().isBlank());
        assertEquals("semantic plan note", candidate.getPlanningNote());
        assertEquals("Quest review is ready and can be confirmed.", candidate.getSummary());
    }

    @Test
    void plansBlockedExecutionWhenQuestStillNeedsFields() {
        VisionProperties visionProperties = new VisionProperties();
        VisionExecutionPlanner planner = new VisionExecutionPlanner(new VisionClarificationService(), new VisionSemanticRouteCatalogService(), visionProperties);
        AppUser user = new AppUser();
        user.setId(7L);

        VisionExecutionCandidateDTO candidate = planner.plan(
                VisionConversationTestBuilder.createQuest(2L, user)
                        .slots(VisionSlotStatePresets.createQuestBaseDetails())
                        .build(),
                VisionPromptUnderstandingResult.builder()
                        .semanticPlan(VisionSemanticPlan.createQuest(0.76d, "needs schedule next"))
                        .build()
        );

        assertEquals("CREATE_QUEST", candidate.getCandidateIntent());
        assertFalse(candidate.isReviewReady());
        assertFalse(candidate.isExecutionReady());
        assertFalse(candidate.isConfirmationRequired());
        assertEquals("schedule_mode", candidate.getNextRequiredSlot());
        assertEquals("Missing required field: schedule_mode.", candidate.getBlockingReason());
        assertEquals("Collect schedule_mode next.", candidate.getSummary());
    }

    @Test
    void flagsLowConfidenceCreateQuestDraftBeforeReview() {
        VisionProperties visionProperties = new VisionProperties();
        VisionExecutionPlanner planner = new VisionExecutionPlanner(new VisionClarificationService(), new VisionSemanticRouteCatalogService(), visionProperties);
        AppUser user = new AppUser();
        user.setId(7L);

        VisionExecutionCandidateDTO candidate = planner.plan(
                VisionConversationTestBuilder.createQuest(3L, user)
                        .slots(VisionSlotStatePresets.createQuestReviewReadyProfileLocation())
                        .build(),
                VisionPromptUnderstandingResult.builder()
                        .semanticPlan(VisionSemanticPlan.createQuest(0.42d, "too vague"))
                        .build()
        );

        assertEquals("CREATE_QUEST", candidate.getCandidateIntent());
        assertFalse(candidate.isReviewReady());
        assertFalse(candidate.isExecutionReady());
        assertEquals("quest_title", candidate.getNextRequiredSlot());
        assertEquals("Need a clearer quest title or task before review.", candidate.getBlockingReason());
        assertEquals("Collect quest_title next.", candidate.getSummary());
    }
}
