package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.testing.VisionConversationTestBuilder;
import com.themuffinman.app.testing.TestFixtures;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class VisionSlotServiceTest {

    private final VisionSlotService visionSlotService = new VisionSlotService(
            new VisionScheduleParserService(),
            new VisionLocationResolutionService(new VisionLocationParserService()),
            new VisionSemanticMapper()
    );

    @Test
    void locationPromptDoesNotLeakIntoDescription() {
        AppUser currentUser = TestFixtures.user(7L, "vision-user");
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(1L, currentUser)
                .requestedSlot("location_mode")
                .build();

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .sourceLanguage("en")
                .originalPrompt("Use profile location")
                .normalizedPrompt("Use profile location")
                .translationProvider("mock")
                .focusSlotId("location_mode")
                .focusSlotConfidence(1.0)
                .translationApplied(false)
                .translationReliable(true)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .location(VisionPromptUnderstandingLocationSlots.builder()
                                .mode("profile")
                                .modeConfidence(1.0)
                                .build())
                        .build())
                .build();

        Map<String, String> merged = visionSlotService.mergeCreateQuestSlots(conversation, "Use profile location", understanding);

        assertEquals("profile", merged.get("location_mode"));
        assertFalse(merged.containsKey("quest_description"));
    }

    @Test
    void rewardPromptDoesNotLeakIntoDescription() {
        AppUser currentUser = TestFixtures.user(7L, "vision-user");
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(1L, currentUser)
                .requestedSlot("reward_amount")
                .build();

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .sourceLanguage("en")
                .originalPrompt("It is free")
                .normalizedPrompt("It is free")
                .translationProvider("mock")
                .focusSlotId("reward_amount")
                .focusSlotConfidence(1.0)
                .translationApplied(false)
                .translationReliable(true)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .reward(VisionPromptUnderstandingRewardSlots.builder()
                                .freeQuest(true)
                                .freeQuestConfidence(1.0)
                                .amount("0")
                                .amountConfidence(1.0)
                                .build())
                        .build())
                .build();

        Map<String, String> merged = visionSlotService.mergeCreateQuestSlots(conversation, "It is free", understanding);

        assertEquals("true", merged.get("free_quest"));
        assertEquals("0", merged.get("reward_amount"));
        assertFalse(merged.containsKey("quest_description"));
    }
}
