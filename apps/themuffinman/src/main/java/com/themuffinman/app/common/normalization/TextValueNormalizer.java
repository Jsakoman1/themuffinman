package com.themuffinman.app.common.normalization;

import com.themuffinman.app.common.errors.ServiceErrors;

import java.util.Locale;

public final class TextValueNormalizer {

    private TextValueNormalizer() {
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public static String requireTrimmed(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw ServiceErrors.badRequest(message);
        }
        return normalized;
    }

    public static String lowerTrimToEmpty(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static String lowerTrimToNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    public static String lowerToEmpty(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    public static String lowerToNull(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT);
    }

    public static String upperTrimToNull(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    public static String upperTrimToEmpty(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? "" : normalized.toUpperCase(Locale.ROOT);
    }

    public static String upperToEmpty(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }
}
