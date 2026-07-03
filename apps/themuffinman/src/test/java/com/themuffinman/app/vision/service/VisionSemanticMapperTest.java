package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionReviewTarget;
import com.themuffinman.app.vision.testing.VisionConversationTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisionSemanticMapperTest {

    private final VisionSemanticMapper visionSemanticMapper = new VisionSemanticMapper();

    @Test
    void appliesFallbackFocusFromConversationRequestedSlot() {
        AppUser user = new AppUser();
        user.setId(7L);
        var conversation = VisionConversationTestBuilder.createQuest(1L, user)
                .requestedSlot("reward_amount")
                .build();

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.empty("20 euros");

        visionSemanticMapper.applyFallbackFocus(understanding, conversation);

        assertEquals("reward_amount", understanding.getFocusSlotId());
        assertEquals(0.85d, understanding.getFocusSlotConfidence());
    }

    @Test
    void doesNotApplyFallbackFocusWhenPromptClearlySwitchesEntityFamily() {
        AppUser user = new AppUser();
        user.setId(7L);
        var conversation = VisionConversationTestBuilder.createQuest(1L, user)
                .requestedSlot("reward_amount")
                .build();

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("create circle Neighbours")
                .semanticPlan(VisionSemanticPlan.createCircle(0.9d, "Local prompt signals match create_circle."))
                .build();

        visionSemanticMapper.applyFallbackFocus(understanding, conversation);

        assertEquals(null, understanding.getFocusSlotId());
        assertEquals(null, understanding.getFocusSlotConfidence());
    }

    @Test
    void resolvesReviewTargetSlotId() {
        assertEquals("location_mode", visionSemanticMapper.reviewTargetSlotId(VisionReviewTarget.LOCATION));
    }
}
