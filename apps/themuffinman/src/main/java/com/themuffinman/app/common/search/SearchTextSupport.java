package com.themuffinman.app.common.search;

import com.themuffinman.app.common.normalization.SearchQueryNormalizer;
import com.themuffinman.app.common.normalization.TextValueNormalizer;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class SearchTextSupport {

    private SearchTextSupport() {
    }

    public static String normalizeQuery(String value) {
        return TextValueNormalizer.lowerTrimToEmpty(SearchQueryNormalizer.normalize(value));
    }

    public static boolean containsNormalized(String value, String normalizedQuery) {
        String normalizedValue = TextValueNormalizer.lowerTrimToEmpty(value);
        return !normalizedValue.isEmpty() && normalizedValue.contains(normalizedQuery);
    }

    public static boolean containsAnyNormalized(String normalizedQuery, String... values) {
        return normalizedHaystack(values).contains(normalizedQuery);
    }

    public static String normalizedHaystack(String... values) {
        return Arrays.stream(values)
                .map(TextValueNormalizer::lowerTrimToEmpty)
                .collect(Collectors.joining(" "));
    }
}
