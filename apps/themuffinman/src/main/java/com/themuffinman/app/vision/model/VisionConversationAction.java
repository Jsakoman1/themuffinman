package com.themuffinman.app.vision.model;

public enum VisionConversationAction {
    SUBMIT_PROMPT,
    CONFIRM_REVIEW,
    REQUEST_REVIEW_EDIT;

    public static VisionConversationAction from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return SUBMIT_PROMPT;
        }

        try {
            return VisionConversationAction.valueOf(rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return SUBMIT_PROMPT;
        }
    }
}
