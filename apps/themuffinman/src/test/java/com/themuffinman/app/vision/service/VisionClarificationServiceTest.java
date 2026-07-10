package com.themuffinman.app.vision.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisionClarificationServiceTest {

    private final VisionClarificationService service = new VisionClarificationService();

    @Test
    void personalizesCreateQuestConfidenceQuestionForVoicePreference() {
        VisionSemanticUserMemoryContext userMemory = VisionSemanticUserMemoryContext.builder()
                .preferredInputType("voice")
                .preferredInputTypeConfidence(0.82d)
                .build();

        assertEquals(
                "Say the quest title or task in one short sentence.",
                service.buildCreateQuestConfidenceQuestion(userMemory)
        );
    }

    @Test
    void fallsBackToDefaultQuestionWhenPreferenceIsWeak() {
        VisionSemanticUserMemoryContext userMemory = VisionSemanticUserMemoryContext.builder()
                .preferredInputType("voice")
                .preferredInputTypeConfidence(0.10d)
                .build();

        assertEquals(
                "I can draft the quest, but I need a clearer title or task before I can review it.",
                service.buildCreateQuestConfidenceQuestion(userMemory)
        );
    }

    @Test
    void buildsQuestGuidanceQuestionForRewardAmount() {
        assertEquals(
                "Should this quest be free, or what reward amount should I use?",
                service.buildCreateQuestGuidanceQuestion("reward_amount", null)
        );
    }

    @Test
    void buildsQuestRetryGuidanceQuestionForDescription() {
        assertEquals(
                "I still need a short description of the task.",
                service.buildCreateQuestRetryGuidanceQuestion("quest_description", null)
        );
    }
}
