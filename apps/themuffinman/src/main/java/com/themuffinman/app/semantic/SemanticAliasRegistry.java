package com.themuffinman.app.semantic;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class SemanticAliasRegistry {

    private static final Map<SemanticEntityFamily, Map<String, String>> FAMILY_ALIASES = Map.of(
            SemanticEntityFamily.QUEST, Map.of(
                    "suitcase", "luggage",
                    "suitcases", "luggage",
                    "luggage", "luggage",
                    "baggage", "luggage",
                    "kofere", "luggage",
                    "kofer", "luggage",
                    "bags", "luggage"
            ),
            SemanticEntityFamily.CIRCLE, Map.of(
                    "group", "circle",
                    "groups", "circle",
                    "team", "circle",
                    "teams", "circle"
            ),
            SemanticEntityFamily.APPLICATION, Map.of(
                    "application", "application",
                    "applications", "application",
                    "request", "application",
                    "requests", "application"
            ),
            SemanticEntityFamily.USER, Map.of(
                    "profile", "user",
                    "person", "user",
                    "contact", "user",
                    "contacts", "user"
            )
    );

    public String normalizeQuery(SemanticEntityFamily family, String query) {
        if (query == null || query.isBlank() || family == null) {
            return query == null ? "" : query.trim();
        }

        Map<String, String> aliases = FAMILY_ALIASES.get(family);
        if (aliases == null || aliases.isEmpty()) {
            return query.trim();
        }

        String normalized = query.trim();
        for (Map.Entry<String, String> alias : aliases.entrySet()) {
            normalized = normalized.replaceAll(
                    "(?i)\\b" + java.util.regex.Pattern.quote(alias.getKey()) + "\\b",
                    alias.getValue()
            );
        }
        return normalized.replaceAll("\\s+", " ").trim();
    }

    public Map<String, String> aliasesFor(SemanticEntityFamily family) {
        Map<String, String> aliases = FAMILY_ALIASES.get(family);
        if (aliases == null) {
            return Map.of();
        }
        return new LinkedHashMap<>(aliases);
    }

    public boolean hasAliases(SemanticEntityFamily family) {
        Map<String, String> aliases = FAMILY_ALIASES.get(family);
        return aliases != null && !aliases.isEmpty();
    }
}
