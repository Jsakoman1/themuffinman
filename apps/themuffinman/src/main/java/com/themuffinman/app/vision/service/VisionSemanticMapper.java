package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionIntent;
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

        VisionIntent conversationIntent = conversation.getIntent();
        VisionIntent promptIntent = understanding.semanticPlanOrEmpty().candidateIntentOrUnsupported();
        if (conversationIntent != null
                && promptIntent != VisionIntent.UNSUPPORTED
                && !sameWorkspaceFamily(conversationIntent, promptIntent)) {
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

    private boolean sameWorkspaceFamily(VisionIntent left, VisionIntent right) {
        String leftFamily = workspaceFamily(left);
        String rightFamily = workspaceFamily(right);
        return leftFamily != null && leftFamily.equals(rightFamily);
    }

    private String workspaceFamily(VisionIntent intent) {
        if (intent == null) {
            return null;
        }
        return switch (intent) {
            case CREATE_QUEST, VIEW_QUEST_DETAIL, VIEW_QUEST_NEWS -> "quests";
            case VIEW_PROFILE, VIEW_SETTINGS, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION, VIEW_USER_PROFILE -> "profile";
            case VIEW_CIRCLES, VIEW_CIRCLE_DETAIL, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST,
                    DELETE_CIRCLE_REQUEST, UPDATE_CIRCLE, DELETE_CIRCLE -> "circles";
            case VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL, CREATE_APPLICATION, UPDATE_APPLICATION,
                    WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION -> "applications";
            case OPEN_CHAT, VIEW_CHAT_WORKSPACE -> "chat";
            default -> null;
        };
    }
}
