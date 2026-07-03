package com.themuffinman.app.semantic;

import java.util.List;
import java.util.Locale;

public final class SemanticEntityResolutionSupport {

    private static final double EXACT_MATCH_CONFIDENCE = 0.97d;
    private static final double PARTIAL_MATCH_CONFIDENCE = 0.88d;
    private static final double RESOLVED_FALLBACK_CONFIDENCE = 0.82d;
    private static final double AMBIGUOUS_CONFIDENCE = 0.35d;
    private static final double NOT_FOUND_CONFIDENCE = 0.10d;
    private static final double UNKNOWN_CONFIDENCE = 0.0d;

    private SemanticEntityResolutionSupport() {
    }

    public static Double confidenceForResolution(
            SemanticEntityResolutionStatus status,
            String targetEntityQuery,
            String canonicalLabel,
            String ambiguityReason,
            List<String> aliasMatches
    ) {
        if (status == SemanticEntityResolutionStatus.RESOLVED) {
            return confidenceForResolved(targetEntityQuery, canonicalLabel, aliasMatches);
        }
        if (status == SemanticEntityResolutionStatus.AMBIGUOUS) {
            return AMBIGUOUS_CONFIDENCE;
        }
        if (status == SemanticEntityResolutionStatus.NOT_FOUND) {
            return confidenceForNotFound(targetEntityQuery, ambiguityReason);
        }
        return UNKNOWN_CONFIDENCE;
    }

    public static Double confidenceForResolved(String targetEntityQuery, String canonicalLabel, List<String> aliasMatches) {
        String normalizedQuery = normalize(targetEntityQuery);
        String normalizedLabel = normalize(canonicalLabel);
        if (normalizedQuery == null || normalizedLabel == null) {
            return RESOLVED_FALLBACK_CONFIDENCE;
        }
        if (normalizedQuery.equals(normalizedLabel)) {
            return EXACT_MATCH_CONFIDENCE;
        }
        if (containsEitherWay(normalizedQuery, normalizedLabel) || aliasMatched(normalizedQuery, aliasMatches)) {
            return PARTIAL_MATCH_CONFIDENCE;
        }
        return RESOLVED_FALLBACK_CONFIDENCE;
    }

    public static Double confidenceForNotFound(String targetEntityQuery, String ambiguityReason) {
        if (hasAmbiguousLanguage(ambiguityReason)) {
            return AMBIGUOUS_CONFIDENCE;
        }
        if (normalize(targetEntityQuery) == null) {
            return UNKNOWN_CONFIDENCE;
        }
        return NOT_FOUND_CONFIDENCE;
    }

    private static boolean aliasMatched(String normalizedQuery, List<String> aliasMatches) {
        if (aliasMatches == null || aliasMatches.isEmpty() || normalizedQuery == null) {
            return false;
        }
        for (String aliasMatch : aliasMatches) {
            String normalizedAlias = normalize(aliasMatch);
            if (normalizedAlias != null && normalizedAlias.equals(normalizedQuery)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsEitherWay(String left, String right) {
        return left.contains(right) || right.contains(left);
    }

    private static boolean hasAmbiguousLanguage(String ambiguityReason) {
        if (ambiguityReason == null || ambiguityReason.isBlank()) {
            return false;
        }
        String normalized = ambiguityReason.toLowerCase(Locale.ROOT);
        return normalized.contains("several")
                || normalized.contains("multiple")
                || normalized.contains("ambiguous");
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
