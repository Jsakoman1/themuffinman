package com.themuffinman.app.vision.service;

import java.util.Locale;

final class VisionReviewInteractionSupport {

    private VisionReviewInteractionSupport() {
    }

    static boolean isConfirmationPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return false;
        }
        String lower = prompt.toLowerCase(Locale.ROOT).trim();
        return lower.equals("confirm")
                || lower.equals("yes")
                || lower.equals("yes confirm")
                || lower.equals("go ahead")
                || lower.equals("create it")
                || lower.equals("create the quest")
                || lower.equals("submit");
    }
}
