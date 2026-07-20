package com.themuffinman.app.vision.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VisionClarificationService {

    /** Shared slot clarification remains the source for Vision; Web guided intake uses the same backend-owned step model. */

    private static final double MIN_CONFIDENT_PREFERENCE = 0.30d;

    public String nextMissingCreateQuestSlot(Map<String, String> slotData) {
        return VisionClarificationCatalog.nextMissingCreateQuestSlot(slotData);
    }

    public String nextMissingCreateApplicationSlot(Map<String, String> slotData) {
        return VisionClarificationCatalog.nextMissingCreateApplicationSlot(slotData);
    }

    public String nextMissingUpdateApplicationSlot(Map<String, String> slotData) {
        return VisionClarificationCatalog.nextMissingUpdateApplicationSlot(slotData);
    }

    public String nextMissingApproveDeclineApplicationSlot(Map<String, String> slotData) {
        return VisionClarificationCatalog.nextMissingApproveDeclineApplicationSlot(slotData);
    }

    public String nextMissingUpdateCircleSlot(Map<String, String> slotData) {
        return VisionClarificationCatalog.nextMissingUpdateCircleSlot(slotData);
    }

    public String nextMissingViewApplicationDetailSlot(Map<String, String> slotData) {
        return VisionClarificationCatalog.nextMissingViewApplicationDetailSlot(slotData);
    }

    public String buildQuestion(String slotId) {
        return VisionClarificationCatalog.questionFor(slotId);
    }

    public String buildRetryQuestion(String slotId) {
        return VisionClarificationCatalog.retryQuestionFor(slotId);
    }

    public String buildCreateQuestConfidenceQuestion() {
        return "I can draft the quest, but I need a clearer title or task before I can review it.";
    }

    public String buildCreateQuestConfidenceRetryQuestion() {
        return "I still need a clearer quest title or task before I can review this draft.";
    }

    public String buildCreateQuestConfidenceQuestion(VisionSemanticUserMemoryContext userMemory) {
        if (prefersVoice(userMemory)) {
            return "Say the quest title or task in one short sentence.";
        }
        if (prefersText(userMemory)) {
            return "Type the quest title or task in one short sentence.";
        }
        return buildCreateQuestConfidenceQuestion();
    }

    public String buildCreateQuestConfidenceRetryQuestion(VisionSemanticUserMemoryContext userMemory) {
        if (prefersVoice(userMemory)) {
            return "I still need a clearer quest title or task. Say it in one short sentence.";
        }
        if (prefersText(userMemory)) {
            return "I still need a clearer quest title or task. Type it in one short sentence.";
        }
        return buildCreateQuestConfidenceRetryQuestion();
    }

    public String buildCreateQuestGuidanceQuestion(String slotId, VisionSemanticUserMemoryContext userMemory) {
        if (slotId == null || slotId.isBlank()) {
            return buildCreateQuestConfidenceQuestion(userMemory);
        }
        return switch (slotId) {
            case VisionClarificationCatalog.SLOT_QUEST_TITLE -> buildQuestion(slotId);
            case VisionClarificationCatalog.SLOT_QUEST_DESCRIPTION -> "What should people know to complete this quest?";
            case VisionClarificationCatalog.SLOT_REWARD_AMOUNT -> "Should this quest be free, or what reward amount should I use?";
            case VisionClarificationCatalog.SLOT_VISIBILITY -> "Should this quest be public or visible only to your circles?";
            case VisionClarificationCatalog.SLOT_SCHEDULE_MODE -> "Should this quest happen at a fixed time or be arranged by agreement?";
            case VisionClarificationCatalog.SLOT_SCHEDULED_DATE -> "What day should I use for the quest?";
            case VisionClarificationCatalog.SLOT_SCHEDULED_TIME -> "What time should I use for the quest?";
            case VisionClarificationCatalog.SLOT_LOCATION_MODE -> "Should I use your profile location, hide the location, or use a custom place?";
            case VisionClarificationCatalog.SLOT_LOCATION_LABEL -> "What custom place or address should I use for this quest?";
            case VisionClarificationCatalog.SLOT_LOCATION_CANDIDATE_CONFIRMATION -> "I found a more precise place match. Should I use the resolved place or keep the location exactly as you typed it?";
            default -> buildQuestion(slotId);
        };
    }

    public String buildCreateQuestRetryGuidanceQuestion(String slotId, VisionSemanticUserMemoryContext userMemory) {
        if (slotId == null || slotId.isBlank()) {
            return buildCreateQuestConfidenceRetryQuestion(userMemory);
        }
        return switch (slotId) {
            case VisionClarificationCatalog.SLOT_QUEST_TITLE -> buildRetryQuestion(slotId);
            case VisionClarificationCatalog.SLOT_QUEST_DESCRIPTION -> "I still need a short description of the task.";
            case VisionClarificationCatalog.SLOT_REWARD_AMOUNT -> "I still need the reward amount, or you can say this quest should be free.";
            case VisionClarificationCatalog.SLOT_VISIBILITY -> "I still need the visibility. Say public or circles only.";
            case VisionClarificationCatalog.SLOT_SCHEDULE_MODE -> "I still need the schedule type. Say fixed time or by agreement.";
            case VisionClarificationCatalog.SLOT_SCHEDULED_DATE -> "I still need the day for the quest.";
            case VisionClarificationCatalog.SLOT_SCHEDULED_TIME -> "I still need the time. Use a format like 14:30, 2 pm, noon, or this evening.";
            case VisionClarificationCatalog.SLOT_LOCATION_MODE -> "I still need the location type. Say use profile, hide location, or custom place.";
            case VisionClarificationCatalog.SLOT_LOCATION_LABEL -> "I still need a real custom place or address, not just 'custom place'.";
            case VisionClarificationCatalog.SLOT_LOCATION_CANDIDATE_CONFIRMATION -> "Choose one: use resolved place, or keep typed location.";
            default -> buildRetryQuestion(slotId);
        };
    }

    private boolean prefersVoice(VisionSemanticUserMemoryContext userMemory) {
        if (userMemory == null || userMemory.getPreferredInputType() == null) {
            return false;
        }
        if (userMemory.getPreferredInputTypeConfidence() == null
                || userMemory.getPreferredInputTypeConfidence() < MIN_CONFIDENT_PREFERENCE) {
            return false;
        }
        return "voice".equalsIgnoreCase(userMemory.getPreferredInputType());
    }

    private boolean prefersText(VisionSemanticUserMemoryContext userMemory) {
        if (userMemory == null || userMemory.getPreferredInputType() == null) {
            return false;
        }
        if (userMemory.getPreferredInputTypeConfidence() == null
                || userMemory.getPreferredInputTypeConfidence() < MIN_CONFIDENT_PREFERENCE) {
            return false;
        }
        return "text".equalsIgnoreCase(userMemory.getPreferredInputType());
    }
}
