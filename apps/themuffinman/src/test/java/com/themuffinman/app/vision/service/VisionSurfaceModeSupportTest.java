package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionNextAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisionSurfaceModeSupportTest {

    @Test
    void mapsNextActionsToCanonicalCanvasModes() {
        assertEquals("clarification", VisionSurfaceModeSupport.canvasModeFor(VisionNextAction.ASK_FOR_SLOT));
        assertEquals("review", VisionSurfaceModeSupport.canvasModeFor(VisionNextAction.SHOW_REVIEW));
        assertEquals("results", VisionSurfaceModeSupport.canvasModeFor(VisionNextAction.SHOW_RESULTS));
        assertEquals("complete", VisionSurfaceModeSupport.canvasModeFor(VisionNextAction.COMPLETE));
        assertEquals("blocked", VisionSurfaceModeSupport.canvasModeFor(VisionNextAction.BLOCKED));
    }

    @Test
    void describesReviewAndBlockedConversationStatesConsistently() {
        VisionConversation reviewConversation = new VisionConversation();
        reviewConversation.setStatus(VisionConversationStatus.REVIEW_READY);
        reviewConversation.setIntent(VisionIntent.CREATE_QUEST);
        reviewConversation.setRequestedSlot("quest_title");

        VisionConversation completeConversation = new VisionConversation();
        completeConversation.setStatus(VisionConversationStatus.COMPLETED);
        completeConversation.setIntent(VisionIntent.CREATE_QUEST);

        VisionConversation blockedConversation = new VisionConversation();
        blockedConversation.setStatus(VisionConversationStatus.BLOCKED);
        blockedConversation.setIntent(VisionIntent.CREATE_QUEST);

        assertEquals("Review ready", VisionSurfaceModeSupport.stageLabelFor(reviewConversation));
        assertEquals("Ready for review and confirmation.", VisionSurfaceModeSupport.progressLabelFor(reviewConversation, new VisionClarificationService()));
        assertEquals("review_ready", VisionSurfaceModeSupport.groupKeyFor(VisionConversationStatus.REVIEW_READY));

        assertEquals("Complete", VisionSurfaceModeSupport.stageLabelFor(completeConversation));
        assertEquals("Task finished.", VisionSurfaceModeSupport.progressLabelFor(completeConversation, new VisionClarificationService()));
        assertEquals("completed", VisionSurfaceModeSupport.groupKeyFor(VisionConversationStatus.COMPLETED));

        assertEquals("Blocked", VisionSurfaceModeSupport.stageLabelFor(blockedConversation));
        assertEquals("Conversation stopped until the user starts a supported task.", VisionSurfaceModeSupport.progressLabelFor(blockedConversation, new VisionClarificationService()));
        assertEquals("blocked", VisionSurfaceModeSupport.groupKeyFor(VisionConversationStatus.BLOCKED));
    }
}
