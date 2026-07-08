package com.themuffinman.app.semantic;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class SemanticAliasRegistry {

    private static final Map<SemanticEntityFamily, Map<String, String>> FAMILY_ALIASES = buildFamilyAliases();

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

    private static Map<SemanticEntityFamily, Map<String, String>> buildFamilyAliases() {
        Map<SemanticEntityFamily, Map<String, String>> aliases = new EnumMap<>(SemanticEntityFamily.class);
        aliases.put(SemanticEntityFamily.QUEST, aliasMap(
                "suitcase", "luggage",
                "suitcases", "luggage",
                "luggage", "luggage",
                "baggage", "luggage",
                "kofere", "luggage",
                "kofer", "luggage",
                "bags", "luggage",
                "task", "quest",
                "tasks", "quest",
                "job", "quest",
                "jobs", "quest",
                "gig", "quest",
                "gigs", "quest"
        ));
        aliases.put(SemanticEntityFamily.NOTIFICATIONS, aliasMap(
                "notification", "notification",
                "notifications", "notification",
                "alerts", "notification",
                "alert", "notification"
        ));
        aliases.put(SemanticEntityFamily.CIRCLE, aliasMap(
                "group", "circle",
                "groups", "circle",
                "team", "circle",
                "teams", "circle",
                "community", "circle",
                "communities", "circle",
                "crew", "circle",
                "crews", "circle"
        ));
        aliases.put(SemanticEntityFamily.APPLICATION, aliasMap(
                "application", "application",
                "applications", "application",
                "request", "application",
                "requests", "application",
                "submission", "application",
                "submissions", "application",
                "apply", "application"
        ));
        aliases.put(SemanticEntityFamily.USER, aliasMap(
                "profile", "user",
                "person", "user",
                "contact", "user",
                "contacts", "user",
                "member", "user",
                "members", "user",
                "account", "user",
                "accounts", "user"
        ));
        aliases.put(SemanticEntityFamily.BUSINESS, aliasMap(
                "business", "business",
                "biz", "business",
                "company", "business",
                "companys", "business",
                "shop", "business",
                "store", "business",
                "service", "business",
                "services", "business"
        ));
        return Collections.unmodifiableMap(aliases);
    }

    private static Map<String, String> aliasMap(String... pairs) {
        if (pairs == null || pairs.length == 0) {
            return Map.of();
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Alias pairs must contain an even number of entries.");
        }
        Map<String, String> aliases = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            aliases.put(pairs[i], pairs[i + 1]);
        }
        return Collections.unmodifiableMap(aliases);
    }
}
