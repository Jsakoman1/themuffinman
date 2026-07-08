package com.themuffinman.app.common.normalization;

import com.themuffinman.app.common.errors.ServiceErrors;

public final class ProfileValueNormalizer {

    private static final int MAX_PROFILE_AVATAR_DATA_URL_LENGTH = 250_000;

    private ProfileValueNormalizer() {
    }

    public static String normalizeText(String value) {
        return TextValueNormalizer.trimToNull(value);
    }

    public static String normalizeAvatarDataUrl(String value) {
        String normalized = TextValueNormalizer.trimToNull(value);
        if (normalized == null) {
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
