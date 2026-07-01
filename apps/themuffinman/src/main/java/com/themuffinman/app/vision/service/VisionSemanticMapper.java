package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionReviewTarget;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VisionSemanticMapper {

    public VisionPromptUnderstandingResult applyFallbackFocus(
            VisionPromptUnderstandingResult understanding,
            VisionConversation conversation
    ) {
        if (understanding == null) {
            return null;
        }
        if (understanding.getFocusSlotId() != null && !understanding.getFocusSlotId().isBlank()) {
            return understanding;
        }
        if (conversation == null || conversation.getRequestedSlot() == null || conversation.getRequestedSlot().isBlank()) {
            return understanding;
        }

        understanding.setFocusSlotId(conversation.getRequestedSlot());
        understanding.setFocusSlotConfidence(0.85d);
        return understanding;
    }

    public Map<String, String> extractedSlots(VisionPromptUnderstandingResult understanding) {
        if (understanding == null) {
            return Map.of();
        }
        return understanding.toExtractedSlotMap();
    }

    public String focusSlotId(VisionPromptUnderstandingResult understanding) {
        if (understanding == null) {
            return null;
        }
        return understanding.getFocusSlotId();
    }

    public String reviewTargetSlotId(VisionReviewTarget reviewTarget) {
        return reviewTarget == null ? null : reviewTarget.getSlotId();
    }
}
