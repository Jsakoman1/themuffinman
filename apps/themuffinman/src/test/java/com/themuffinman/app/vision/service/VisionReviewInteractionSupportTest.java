package com.themuffinman.app.vision.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionReviewInteractionSupportTest {

    @Test
    void recognizesConfirmationPromptsUsedByReviewFlows() {
        assertTrue(VisionReviewInteractionSupport.isConfirmationPrompt("confirm"));
        assertTrue(VisionReviewInteractionSupport.isConfirmationPrompt(" Yes "));
        assertTrue(VisionReviewInteractionSupport.isConfirmationPrompt("go ahead"));
        assertTrue(VisionReviewInteractionSupport.isConfirmationPrompt("create the quest"));
    }

    @Test
    void rejectsNonConfirmationPrompts() {
        assertFalse(VisionReviewInteractionSupport.isConfirmationPrompt(""));
        assertFalse(VisionReviewInteractionSupport.isConfirmationPrompt("maybe later"));
        assertFalse(VisionReviewInteractionSupport.isConfirmationPrompt("review"));
    }
}
