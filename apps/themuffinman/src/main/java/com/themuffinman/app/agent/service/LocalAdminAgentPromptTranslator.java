package com.themuffinman.app.agent.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class LocalAdminAgentPromptTranslator implements AdminAgentPromptTranslator {

    private static final Map<String, String> CROATIAN_TO_ENGLISH = new LinkedHashMap<>();

    static {
        CROATIAN_TO_ENGLISH.put("pošalji friend request", "send friend request");
        CROATIAN_TO_ENGLISH.put("posalji friend request", "send friend request");
        CROATIAN_TO_ENGLISH.put("dodaj prijatelja", "add friend");
        CROATIAN_TO_ENGLISH.put("napravi novi user", "create new user");
        CROATIAN_TO_ENGLISH.put("napravi user", "create user");
        CROATIAN_TO_ENGLISH.put("novi user", "new user");
        CROATIAN_TO_ENGLISH.put("napravi novi quest", "create new quest");
        CROATIAN_TO_ENGLISH.put("napravi quest", "create quest");
        CROATIAN_TO_ENGLISH.put("questa", "quests");
        CROATIAN_TO_ENGLISH.put("novi quest", "new quest");
        CROATIAN_TO_ENGLISH.put("ako ima vise", "if there are multiple");
        CROATIAN_TO_ENGLISH.put("ako ima više", "if there are multiple");
        CROATIAN_TO_ENGLISH.put("prijava", "application");
        CROATIAN_TO_ENGLISH.put("prijave", "applications");
        CROATIAN_TO_ENGLISH.put("applicirao prvi", "applied first");
        CROATIAN_TO_ENGLISH.put("prijavio prvi", "applied first");
        CROATIAN_TO_ENGLISH.put("approvaj", "approve");
        CROATIAN_TO_ENGLISH.put("approveaj", "approve");
        CROATIAN_TO_ENGLISH.put("odobri", "approve");
        CROATIAN_TO_ENGLISH.put("obrisi", "delete");
        CROATIAN_TO_ENGLISH.put("obriši", "delete");
        CROATIAN_TO_ENGLISH.put("moj quest", "my quest");
        CROATIAN_TO_ENGLISH.put("trenutnu lokaciju", "current location");
        CROATIAN_TO_ENGLISH.put("trenutna lokacija", "current location");
        CROATIAN_TO_ENGLISH.put("moju lokaciju", "my location");
        CROATIAN_TO_ENGLISH.put("pošalji poruku", "send message");
        CROATIAN_TO_ENGLISH.put("posalji poruku", "send message");
        CROATIAN_TO_ENGLISH.put("prikaži profil", "show profile");
        CROATIAN_TO_ENGLISH.put("prikazi profil", "show profile");
        CROATIAN_TO_ENGLISH.put("sutra", "tomorrow");
        CROATIAN_TO_ENGLISH.put("samo za prijatelje", "only for friends");
        CROATIAN_TO_ENGLISH.put("samo za krug", "circle only");
    }

    @Override
    public AdminAgentPromptTranslation translateForPlanning(String prompt) {
        String normalizedPrompt = prompt == null ? "" : prompt.trim().replaceAll("\\s+", " ");
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        String translated = lower;
        boolean translationApplied = false;

        for (Map.Entry<String, String> entry : CROATIAN_TO_ENGLISH.entrySet()) {
            if (translated.contains(entry.getKey())) {
                translated = translated.replace(entry.getKey(), entry.getValue());
                translationApplied = true;
            }
        }

        return AdminAgentPromptTranslation.builder()
                .sourceLanguage(detectSourceLanguage(lower))
                .originalPrompt(normalizedPrompt)
                .translatedPrompt(translationApplied ? translated : normalizedPrompt)
                .translationProvider("local")
                .translationApplied(translationApplied)
                .translationReliable(isLikelyEnglish(lower) || translationApplied)
                .build();
    }

    private String detectSourceLanguage(String normalizedPrompt) {
        if (containsCroatianMarkers(normalizedPrompt)) {
            return "hr";
        }
        if (isLikelyEnglish(normalizedPrompt)) {
            return "en";
        }
        return "unknown";
    }

    private boolean isLikelyEnglish(String normalizedPrompt) {
        return normalizedPrompt.chars().allMatch(codePoint -> codePoint < 128)
                && (normalizedPrompt.contains("quest")
                || normalizedPrompt.contains("create")
                || normalizedPrompt.contains("approve")
                || normalizedPrompt.contains("delete")
                || normalizedPrompt.contains("friend request"));
    }

    private boolean containsCroatianMarkers(String normalizedPrompt) {
        return normalizedPrompt.contains("napravi")
                || normalizedPrompt.contains("pošalji")
                || normalizedPrompt.contains("posalji")
                || normalizedPrompt.contains("obrisi")
                || normalizedPrompt.contains("obriši")
                || normalizedPrompt.contains("sutra")
                || normalizedPrompt.contains("prijav");
    }
}
