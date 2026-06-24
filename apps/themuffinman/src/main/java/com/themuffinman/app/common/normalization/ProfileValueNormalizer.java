package com.themuffinman.app.common.normalization;

import com.themuffinman.app.common.errors.ServiceErrors;

public final class ProfileValueNormalizer {

    private static final int MAX_PROFILE_AVATAR_DATA_URL_LENGTH = 250_000;

    private ProfileValueNormalizer() {
    }

    public static String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public static String normalizeAvatarDataUrl(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        if (!normalized.startsWith("data:image/")) {
            throw ServiceErrors.badRequest("Profile avatar must be an image data URL");
        }

        if (normalized.length() > MAX_PROFILE_AVATAR_DATA_URL_LENGTH) {
            throw ServiceErrors.badRequest("Profile avatar is too large");
        }

        return normalized;
    }
}
