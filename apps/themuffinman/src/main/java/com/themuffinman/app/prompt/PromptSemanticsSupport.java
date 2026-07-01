package com.themuffinman.app.prompt;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class PromptSemanticsSupport {

    public PromptSemanticPlan inferPlan(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return PromptSemanticPlan.empty();
        }

        String normalizedPrompt = normalizePrompt(prompt);
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        if (containsDiscoverySignals(lower)) {
            return PromptSemanticPlan.discoverQuests(0.8d, "Local prompt signals match discover_quests.", inferDiscoveryQuery(normalizedPrompt));
        }
        if (containsAny(lower,
                "create quest",
                "create quests",
                "new quest",
                "new quests",
                "post a quest",
                "post quest",
                "post quests",
                "need someone",
                "looking for someone",
                "i need help",
                "can someone",
                "task for someone",
                "help me with",
                "quests")) {
            return PromptSemanticPlan.createQuest(0.8d, "Local prompt signals match create_quest.");
        }
        return PromptSemanticPlan.empty();
    }

    public String normalizePrompt(String prompt) {
        if (prompt == null) {
            return "";
        }
        return prompt.trim().replaceAll("\\s+", " ");
    }

    private String inferDiscoveryQuery(String prompt) {
        if (prompt == null) {
            return "";
        }

        String extracted = prompt.toLowerCase(Locale.ROOT)
                .replace("show me open quests for", " ")
                .replace("show open quests for", " ")
                .replace("show me quests for", " ")
                .replace("show quests for", " ")
                .replace("find quests for", " ")
                .replace("browse quests for", " ")
                .replace("search quests for", " ")
                .replace("looking for work", " ")
                .replace("looking for jobs", " ")
                .replace("jobs near", " ")
                .replace("work near", " ")
                .replace("help wanted", " ")
                .replaceAll("(?i)\\b(open|available|discover|recommend|quest|quests|job|jobs|work|task|tasks|near|around|me|please|for|the|a|an|to)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return extracted;
    }

    private boolean containsDiscoverySignals(String value) {
        return containsAny(value,
                "open quests",
                "available quests",
                "show open quests",
                "show quests",
                "find quests",
                "browse quests",
                "search quests",
                "looking for work",
                "looking for jobs",
                "what quests",
                "what can i do",
                "odd jobs",
                "jobs near",
                "work near",
                "help wanted",
                "recommend a quest",
                "recommend work");
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
