package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionNextAction;

final class VisionSurfaceModeSupport {

    private VisionSurfaceModeSupport() {
    }

    static String canvasModeFor(VisionNextAction nextAction) {
        return switch (nextAction) {
            case ASK_FOR_SLOT -> "clarification";
            case SHOW_REVIEW -> "review";
            case SHOW_RESULTS -> "results";
            case COMPLETE -> "complete";
            case BLOCKED -> "blocked";
        };
    }

    static String stageLabelFor(VisionConversation conversation) {
        return switch (conversation.getStatus()) {
            case ACTIVE -> {
                if (conversation.getIntent() == VisionIntent.DISCOVER_QUESTS) {
                    yield "Browsing";
                }
                if (conversation.getIntent() == VisionIntent.OPEN_CHAT) {
                    yield conversation.getRequestedSlot() == null ? "Chatting" : "Needs input";
                }
                yield conversation.getRequestedSlot() == null ? "In progress" : "Needs input";
            }
            case REVIEW_READY -> "Review ready";
            case COMPLETED -> "Complete";
            case BLOCKED -> "Blocked";
        };
    }

    static String progressLabelFor(VisionConversation conversation, VisionClarificationService clarificationService) {
        return switch (conversation.getStatus()) {
            case ACTIVE -> conversation.getIntent() == VisionIntent.DISCOVER_QUESTS
                    ? "Quest discovery is ready."
                    : conversation.getIntent() == VisionIntent.OPEN_CHAT
                    ? conversation.getRequestedSlot() == null
                    ? "Chat conversation is active."
                    : "Next step: " + clarificationService.buildQuestion(conversation.getRequestedSlot())
                    : conversation.getRequestedSlot() == null
                    ? "Conversation is active."
                    : "Next step: " + clarificationService.buildQuestion(conversation.getRequestedSlot());
            case REVIEW_READY -> "Ready for review and confirmation.";
            case COMPLETED -> "Task finished.";
            case BLOCKED -> "Conversation stopped until the user starts a supported task.";
        };
    }

    static String groupKeyFor(VisionConversationStatus status) {
        return switch (status) {
            case ACTIVE -> "active";
            case REVIEW_READY -> "review_ready";
            case BLOCKED -> "blocked";
            case COMPLETED -> "completed";
        };
    }
}
