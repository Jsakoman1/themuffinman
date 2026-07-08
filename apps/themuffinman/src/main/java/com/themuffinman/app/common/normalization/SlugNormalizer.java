package com.themuffinman.app.common.normalization;

import com.themuffinman.app.common.errors.ServiceErrors;

import java.util.Locale;

public final class SlugNormalizer {

    private SlugNormalizer() {
    }

    public static String normalizeSlug(String value, String requiredMessage, String invalidMessage) {
        String normalized = TextValueNormalizer.requireTrimmed(value, requiredMessage).toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")) {
            throw ServiceErrors.badRequest(invalidMessage);
        }
        return normalized;
    }

    public static String slugify(String value, String failureMessage) {
        String slug = TextValueNormalizer.requireTrimmed(value, failureMessage)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (slug.isBlank()) {
            throw ServiceErrors.badRequest(failureMessage);
        }
        return slug;
    }
}
