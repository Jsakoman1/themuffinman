package com.themuffinman.app.vision.model;

public enum VisionTurnSource {
    TEXT,
    VOICE;

    public static VisionTurnSource from(String value) {
        if (value == null || value.isBlank()) {
            return TEXT;
        }
        try {
            return VisionTurnSource.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return TEXT;
        }
    }
}
