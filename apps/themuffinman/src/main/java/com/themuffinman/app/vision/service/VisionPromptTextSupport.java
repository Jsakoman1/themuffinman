package com.themuffinman.app.vision.service;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

final class VisionPromptTextSupport {

    private static final List<String> LEADING_MARKERS = List.of("-", ":", "=", "->", "—", "–", "•");

    private VisionPromptTextSupport() {
    }

    static String extractAfterAnyPrefix(String value, Collection<String> prefixes) {
        if (value == null || value.isBlank() || prefixes == null || prefixes.isEmpty()) {
            return null;
        }

        String normalized = normalizeWhitespace(value);
        String matchedPrefix = findLongestPrefix(normalized, prefixes);
        if (matchedPrefix == null) {
            return null;
        }

        String stripped = normalized.substring(matchedPrefix.length()).trim();
        stripped = stripLeadingMarkers(stripped);
        return stripped.isBlank() ? null : stripped;
    }

    static String extractAfterAnyPrefix(String value, Collection<String> prefixes, Collection<String> connectors) {
        String stripped = extractAfterAnyPrefix(value, prefixes);
        if (stripped == null || connectors == null || connectors.isEmpty()) {
            return stripped;
        }
        return truncateAtAnyConnector(stripped, connectors);
    }

    static String stripLeadingWords(String value, Collection<String> words) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (words == null || words.isEmpty()) {
            return normalizeWhitespace(value);
        }
        String current = normalizeWhitespace(value);
        boolean changed;
        do {
            changed = false;
            String lower = current.toLowerCase(Locale.ROOT);
            for (String word : words) {
                if (word == null || word.isBlank()) {
                    continue;
                }
                String normalizedWord = word.trim().toLowerCase(Locale.ROOT);
                if (lower.equals(normalizedWord)) {
                    return null;
                }
                if (lower.startsWith(normalizedWord + " ")) {
                    current = current.substring(normalizedWord.length()).trim();
                    changed = true;
                    break;
                }
            }
        } while (changed);

        current = stripLeadingMarkers(current);
        return current.isBlank() ? null : current;
    }

    static String normalizeWhitespace(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private static String truncateAtAnyConnector(String value, Collection<String> connectors) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = normalizeWhitespace(value);
        String lower = normalized.toLowerCase(Locale.ROOT);
        int cutIndex = -1;
        for (String connector : connectors) {
            if (connector == null || connector.isBlank()) {
                continue;
            }
            int index = lower.indexOf(connector.toLowerCase(Locale.ROOT));
            if (index > 0 && (cutIndex < 0 || index < cutIndex)) {
                cutIndex = index;
            }
        }

        if (cutIndex > 0) {
            normalized = normalized.substring(0, cutIndex).trim();
        }

        normalized = stripLeadingMarkers(normalized);
        return normalized.isBlank() ? null : normalized;
    }

    private static String stripLeadingMarkers(String value) {
        if (value == null || value.isBlank()) {
            return value == null ? null : value.trim();
        }
        String current = normalizeWhitespace(value);
        boolean changed;
        do {
            changed = false;
            for (String marker : LEADING_MARKERS) {
                if (current.startsWith(marker + " ")) {
                    current = current.substring(marker.length()).trim();
                    changed = true;
                    break;
                }
                if (current.startsWith(marker)) {
                    current = current.substring(marker.length()).trim();
                    changed = true;
                    break;
                }
            }
        } while (changed);
        return current.trim();
    }

    private static String findLongestPrefix(String value, Collection<String> prefixes) {
        String lower = value.toLowerCase(Locale.ROOT);
        String matchedPrefix = null;
        for (String prefix : prefixes) {
            if (prefix == null || prefix.isBlank()) {
                continue;
            }
            String normalizedPrefix = normalizeWhitespace(prefix).toLowerCase(Locale.ROOT);
            if (!lower.startsWith(normalizedPrefix)) {
                continue;
            }
            if (matchedPrefix == null || normalizedPrefix.length() > matchedPrefix.length()) {
                matchedPrefix = normalizedPrefix;
            }
        }
        return matchedPrefix;
    }
}
