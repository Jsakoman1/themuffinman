package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VisionSlotService {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?<!\\d)(\\d{1,5}(?:[.,]\\d{1,2})?)(?!\\d)");

    public Map<String, String> mergeCreateQuestSlots(VisionConversation conversation, String normalizedPrompt) {
        Map<String, String> merged = new LinkedHashMap<>(conversation.getSlotData());
        String requestedSlot = conversation.getRequestedSlot();

        if ("quest_title".equals(requestedSlot)) {
            merged.put("quest_title", normalizeTitleAnswer(normalizedPrompt));
        } else if ("quest_description".equals(requestedSlot)) {
            merged.put("quest_description", normalizedPrompt.trim());
        } else if ("reward_amount".equals(requestedSlot)) {
            applyRewardAnswer(merged, normalizedPrompt);
        } else if ("visibility".equals(requestedSlot)) {
            String visibility = extractVisibility(normalizedPrompt);
            if (visibility != null) {
                merged.put("visibility", visibility);
            }
        } else {
            autoFillCreateQuestSlots(merged, normalizedPrompt);
        }

        cleanupEmptyValues(merged);
        return merged;
    }

    private void autoFillCreateQuestSlots(Map<String, String> merged, String normalizedPrompt) {
        String prompt = normalizedPrompt.trim();
        if (isGenericCreateQuestCommand(prompt)) {
            return;
        }
        if (!prompt.isBlank() && !merged.containsKey("quest_description")) {
            merged.put("quest_description", prompt);
        }

        if (!merged.containsKey("quest_title")) {
            String title = deriveTitleFromPrompt(prompt);
            if (title != null) {
                merged.put("quest_title", title);
            }
        }

        if (!merged.containsKey("reward_amount") && !merged.containsKey("free_quest")) {
            applyRewardAnswer(merged, prompt);
        }

        if (!merged.containsKey("visibility")) {
            String visibility = extractVisibility(prompt);
            if (visibility != null) {
                merged.put("visibility", visibility);
            }
        }
    }

    private void applyRewardAnswer(Map<String, String> merged, String normalizedPrompt) {
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "free", "no pay", "without pay", "volunteer", "unpaid")) {
            merged.put("free_quest", "true");
            merged.put("reward_amount", "0");
            return;
        }

        Matcher matcher = AMOUNT_PATTERN.matcher(lower);
        if (matcher.find()) {
            BigDecimal amount = new BigDecimal(matcher.group(1).replace(',', '.'));
            merged.put("free_quest", "false");
            merged.put("reward_amount", amount.stripTrailingZeros().toPlainString());
        }
    }

    private String extractVisibility(String normalizedPrompt) {
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "everyone", "public", "open to all", "anyone")) {
            return "PUBLIC";
        }
        if (containsAny(lower, "circle", "circles", "friends only", "private")) {
            return "CIRCLES";
        }
        return null;
    }

    private String deriveTitleFromPrompt(String prompt) {
        if (prompt.isBlank()) {
            return null;
        }

        String cleaned = prompt
                .replaceAll("(?i)^please\\s+", "")
                .replaceAll("(?i)^create (a )?quest( for)?\\s+", "")
                .replaceAll("(?i)^post (a )?quest( for)?\\s+", "")
                .replaceAll("(?i)^i need help with\\s+", "")
                .replaceAll("(?i)^i need someone to\\s+", "")
                .replaceAll("(?i)^need someone to\\s+", "")
                .replaceAll("(?i)^looking for someone to\\s+", "")
                .replaceAll("(?i)^can someone\\s+", "")
                .replaceAll("(?i)^help me with\\s+", "")
                .trim();

        if (cleaned.isBlank()) {
            return null;
        }

        String firstSentence = cleaned.split("[.!?\\n]")[0].trim();
        int rewardMarker = indexOfAny(firstSentence.toLowerCase(Locale.ROOT), " for ", " with ", " paying ", " pay ");
        if (rewardMarker > 10) {
            firstSentence = firstSentence.substring(0, rewardMarker).trim();
        }

        if (firstSentence.length() > 80) {
            firstSentence = firstSentence.substring(0, 80).trim();
        }
        if (firstSentence.length() < 4) {
            return null;
        }
        return capitalize(firstSentence);
    }

    private String normalizeTitleAnswer(String prompt) {
        String title = prompt.trim();
        if (title.length() > 255) {
            title = title.substring(0, 255).trim();
        }
        return capitalize(title);
    }

    private boolean isGenericCreateQuestCommand(String prompt) {
        String lower = prompt.toLowerCase(Locale.ROOT).trim();
        return lower.equals("create a quest")
                || lower.equals("create quest")
                || lower.equals("new quest")
                || lower.equals("post a quest")
                || lower.equals("post quest");
    }

    private void cleanupEmptyValues(Map<String, String> merged) {
        merged.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().isBlank());
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private int indexOfAny(String value, String... tokens) {
        int best = -1;
        for (String token : tokens) {
            int index = value.indexOf(token);
            if (index >= 0 && (best < 0 || index < best)) {
                best = index;
            }
        }
        return best;
    }

    private String capitalize(String input) {
        if (input.isBlank()) {
            return input;
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFKC);
        return normalized.substring(0, 1).toUpperCase(Locale.ROOT) + normalized.substring(1);
    }
}
