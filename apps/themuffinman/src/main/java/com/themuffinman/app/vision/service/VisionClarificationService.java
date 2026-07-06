package com.themuffinman.app.vision.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VisionClarificationService {

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
