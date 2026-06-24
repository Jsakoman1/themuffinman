package com.themuffinman.app.common.normalization;

public final class SearchQueryNormalizer {

    private SearchQueryNormalizer() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().replaceFirst("^@+", "");
    }
}
