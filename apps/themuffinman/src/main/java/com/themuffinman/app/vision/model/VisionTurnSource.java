package com.themuffinman.app.vision.model;

import com.themuffinman.app.common.normalization.TextValueNormalizer;

public enum VisionTurnSource {
    TEXT,
    VOICE;

    public static VisionTurnSource from(String value) {
        if (value == null || value.isBlank()) {
            return TEXT;
        }
        try {
            return VisionTurnSource.valueOf(TextValueNormalizer.upperTrimToEmpty(value));
        } catch (IllegalArgumentException exception) {
            return TEXT;
        }
    }
}
