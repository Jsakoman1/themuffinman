package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionReviewTarget;
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
    private final VisionScheduleParserService visionScheduleParserService;
    private final VisionLocationResolutionService visionLocationResolutionService;
    private final VisionSemanticMapper visionSemanticMapper;

    public VisionSlotService(
            VisionScheduleParserService visionScheduleParserService,
            VisionLocationResolutionService visionLocationResolutionService,
            VisionSemanticMapper visionSemanticMapper
    ) {
        this.visionScheduleParserService = visionScheduleParserService;
        this.visionLocationResolutionService = visionLocationResolutionService;
        this.visionSemanticMapper = visionSemanticMapper;
    }

    public Map<String, String> mergeCreateQuestSlots(VisionConversation conversation, String normalizedPrompt) {
        return mergeCreateQuestSlots(conversation, normalizedPrompt, VisionPromptUnderstandingResult.empty(normalizedPrompt));
    }

    public Map<String, String> mergeCreateQuestSlots(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        Map<String, String> merged = new LinkedHashMap<>(conversation.getSlotData());
        String requestedSlot = conversation.getRequestedSlot();
        String focusSlotId = visionSemanticMapper.focusSlotId(understanding);
        applySemanticSlots(merged, understanding, normalizedPrompt, actorKeyForConversation(conversation));

        if ("quest_title".equals(requestedSlot) && !merged.containsKey("quest_title")) {
            merged.put("quest_title", normalizeTitleAnswer(normalizedPrompt));
        } else if ("quest_description".equals(requestedSlot) && !merged.containsKey("quest_description")) {
            merged.put("quest_description", normalizedPrompt.trim());
        } else if ("reward_amount".equals(requestedSlot) && !merged.containsKey("reward_amount") && !merged.containsKey("free_quest")) {
            applyRewardAnswer(merged, normalizedPrompt);
        } else if ("visibility".equals(requestedSlot) && !merged.containsKey("visibility")) {
            String visibility = extractVisibility(normalizedPrompt);
            if (visibility != null) {
                merged.put("visibility", visibility);
            }
        } else if ("schedule_mode".equals(requestedSlot)) {
            if (!merged.containsKey("schedule_mode")) {
                applyScheduleModeAnswer(merged, normalizedPrompt);
            }
            if ("fixed".equals(merged.get("schedule_mode")) && !merged.containsKey("scheduled_at")) {
                applyScheduledAtAnswer(merged, normalizedPrompt);
            }
        } else if ("scheduled_at".equals(requestedSlot) && !merged.containsKey("scheduled_at")) {
            applyScheduledAtAnswer(merged, normalizedPrompt);
        } else if ("location_mode".equals(requestedSlot)) {
            if (!merged.containsKey("location_mode")) {
                applyLocationModeAnswer(merged, normalizedPrompt, actorKeyForConversation(conversation));
            }
            if ("custom".equals(merged.get("location_mode")) && !merged.containsKey("location_label")) {
                applyLocationLabelAnswer(merged, normalizedPrompt, actorKeyForConversation(conversation));
            }
        } else if ("location_label".equals(requestedSlot) && !merged.containsKey("location_label")) {
            applyLocationLabelAnswer(merged, normalizedPrompt, actorKeyForConversation(conversation));
        } else if ("location_candidate_confirmation".equals(requestedSlot) && !merged.containsKey("location_candidate_confirmation")) {
            applyLocationCandidateConfirmation(merged, normalizedPrompt);
        } else {
            autoFillCreateQuestSlots(merged, normalizedPrompt, actorKeyForConversation(conversation), requestedSlot, focusSlotId);
        }

        cleanupEmptyValues(merged);
        return merged;
    }

    public void prepareForReviewEdit(Map<String, String> slotData, VisionReviewTarget reviewTarget) {
        if (slotData == null || reviewTarget == null) {
            return;
        }

        String slotId = reviewTarget.getSlotId();

        switch (slotId) {
            case "reward_amount" -> {
                slotData.remove("reward_amount");
                slotData.remove("free_quest");
            }
            case "schedule_mode" -> {
                slotData.remove("schedule_mode");
                slotData.remove("scheduled_at");
            }
            case "location_mode" -> {
                slotData.remove("location_mode");
                slotData.remove("location_label");
                slotData.remove("location_resolution_status");
                slotData.remove("location_resolution_provider");
                clearPendingLocationCandidate(slotData);
                slotData.remove("location_candidate_confirmation");
            }
            default -> slotData.remove(slotId);
        }
    }

    private void autoFillCreateQuestSlots(
            Map<String, String> merged,
            String normalizedPrompt,
            String actorKey,
            String requestedSlot,
            String focusSlotId
    ) {
        String prompt = normalizedPrompt.trim();
        if (isGenericCreateQuestCommand(prompt)) {
            return;
        }
        if (!merged.containsKey("quest_description") && shouldAutoFillDescription(prompt, requestedSlot, focusSlotId)) {
            merged.put("quest_description", prompt);
        }

        if (!merged.containsKey("quest_title")) {
            String title = deriveTitleFromPrompt(prompt);
            if (title != null) {
                merged.put("quest_title", title);
            }
        }

        if (!merged.containsKey("reward_amount") && !merged.containsKey("free_quest") && shouldAutoFillReward(prompt, requestedSlot, focusSlotId)) {
            applyRewardAnswer(merged, prompt);
        }

        if (!merged.containsKey("visibility")) {
            String visibility = extractVisibility(prompt);
            if (visibility != null) {
                merged.put("visibility", visibility);
            }
        }

        if (!merged.containsKey("schedule_mode")) {
            applyScheduleModeAnswer(merged, prompt);
        }

        if ("fixed".equals(merged.get("schedule_mode")) && !merged.containsKey("scheduled_at")) {
            applyScheduledAtAnswer(merged, prompt);
        }

        if (!merged.containsKey("location_mode") && shouldAutoFillLocation(prompt, focusSlotId)) {
            applyLocationModeAnswer(merged, prompt, actorKey);
        }

        if ("custom".equals(merged.get("location_mode")) && !merged.containsKey("location_label")) {
            applyLocationLabelAnswer(merged, prompt, actorKey);
        }
    }

    private void applySemanticSlots(
            Map<String, String> merged,
            VisionPromptUnderstandingResult understanding,
            String normalizedPrompt,
            String actorKey
    ) {
        if (understanding == null) {
            return;
        }

        Map<String, String> extractedSlots = visionSemanticMapper.extractedSlots(understanding);
        String focusSlotId = visionSemanticMapper.focusSlotId(understanding);
        applySemanticTitle(merged, extractedSlots.get("quest_title"));
        applySemanticDescription(merged, extractedSlots.get("quest_description"), normalizedPrompt, focusSlotId);
        applySemanticReward(merged, extractedSlots, normalizedPrompt, focusSlotId);
        applySemanticVisibility(merged, extractedSlots.get("visibility"), normalizedPrompt, focusSlotId);
        applySemanticSchedule(merged, extractedSlots, normalizedPrompt, focusSlotId);
        applySemanticLocation(merged, extractedSlots, normalizedPrompt, focusSlotId, actorKey);
    }

    private void applySemanticTitle(Map<String, String> merged, String title) {
        if (title == null || title.isBlank()) {
            return;
        }
        merged.put("quest_title", normalizeTitleAnswer(title));
    }

    private void applySemanticDescription(Map<String, String> merged, String description, String normalizedPrompt, String focusSlotId) {
        if (description == null || description.isBlank()) {
            return;
        }
        merged.put("quest_description", description.trim());
    }

    private void applySemanticReward(Map<String, String> merged, Map<String, String> extractedSlots, String normalizedPrompt, String focusSlotId) {
        String freeQuest = extractedSlots.get("free_quest");
        if (freeQuest != null && Boolean.parseBoolean(freeQuest)) {
            merged.put("free_quest", "true");
            merged.put("reward_amount", "0");
            return;
        }

        String rewardAmount = extractedSlots.get("reward_amount");
        if (rewardAmount == null || rewardAmount.isBlank()) {
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(rewardAmount.trim().replace(',', '.'));
            merged.put("free_quest", "false");
            merged.put("reward_amount", amount.stripTrailingZeros().toPlainString());
        } catch (NumberFormatException ignored) {
            // Keep deterministic fallback if the extracted value is malformed.
        }
    }

    private void applySemanticVisibility(Map<String, String> merged, String visibility, String normalizedPrompt, String focusSlotId) {
        if (visibility == null || visibility.isBlank()) {
            return;
        }
        String normalized = visibility.trim().toUpperCase(Locale.ROOT);
        if ("PUBLIC".equals(normalized) || "CIRCLES".equals(normalized)) {
            merged.put("visibility", normalized);
        }
    }

    private void applySemanticSchedule(Map<String, String> merged, Map<String, String> extractedSlots, String normalizedPrompt, String focusSlotId) {
        String scheduleMode = extractedSlots.get("schedule_mode");
        if (scheduleMode != null && !scheduleMode.isBlank()) {
            String normalized = scheduleMode.trim().toLowerCase(Locale.ROOT);
            if ("agreement".equals(normalized) || "fixed".equals(normalized)) {
                merged.put("schedule_mode", normalized);
            }
        }

        String scheduledAt = extractedSlots.get("scheduled_at");
        if (scheduledAt != null && !scheduledAt.isBlank()) {
            merged.put("scheduled_at", scheduledAt.trim());
        }
    }

    private void applySemanticLocation(
            Map<String, String> merged,
            Map<String, String> extractedSlots,
            String normalizedPrompt,
            String focusSlotId,
            String actorKey
    ) {
        String locationMode = extractedSlots.get("location_mode");
        if (locationMode != null && !locationMode.isBlank()) {
            String normalized = locationMode.trim().toLowerCase(Locale.ROOT);
            if ("off".equals(normalized) || "profile".equals(normalized) || "custom".equals(normalized)) {
                merged.put("location_mode", normalized);
                if (!"custom".equals(normalized)) {
                    merged.remove("location_label");
                    merged.remove("location_resolution_status");
                    merged.remove("location_resolution_provider");
                    merged.remove("location_candidate_confirmation");
                    clearPendingLocationCandidate(merged);
                }
            }
        }

        String locationLabel = extractedSlots.get("location_label");
        if (locationLabel != null && !locationLabel.isBlank()) {
            String mode = merged.get("location_mode");
            if (!"off".equals(mode) && !"profile".equals(mode)) {
                applyLocationLabelAnswer(merged, locationLabel, actorKey);
            }
        }

        String locationCandidateConfirmation = extractedSlots.get("location_candidate_confirmation");
        if (locationCandidateConfirmation != null && !locationCandidateConfirmation.isBlank()) {
            merged.put("location_candidate_confirmation", locationCandidateConfirmation.trim().toLowerCase(Locale.ROOT));
        }
    }

    private boolean shouldAutoFillDescription(String normalizedPrompt, String requestedSlot, String focusSlotId) {
        if (focusSlotId != null && "quest_description".equals(focusSlotId)) {
            return true;
        }
        if ("quest_description".equals(requestedSlot)) {
            return true;
        }
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        return containsAny(lower, "need", "looking for", "help", "task", "description", "quest", "i want", "i need");
    }

    private boolean shouldAutoFillReward(String normalizedPrompt, String requestedSlot, String focusSlotId) {
        if (focusSlotId != null && "reward_amount".equals(focusSlotId)) {
            return true;
        }
        if ("reward_amount".equals(requestedSlot)) {
            return true;
        }
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        return containsAny(lower, "free", "no pay", "without pay", "unpaid", "reward", "euros", "euro", "eur", "kn", "pay", "compensation", "amount", "price");
    }

    private boolean shouldAutoFillVisibility(String normalizedPrompt, String focusSlotId) {
        if (focusSlotId != null && "visibility".equals(focusSlotId)) {
            return true;
        }
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        return containsAny(lower, "public", "circles", "friends only", "everyone", "private");
    }

    private boolean shouldAutoFillSchedule(String normalizedPrompt, String focusSlotId) {
        if (focusSlotId != null && ("schedule_mode".equals(focusSlotId) || "scheduled_at".equals(focusSlotId))) {
            return true;
        }
        return visionScheduleParserService.suggestsFixedSchedule(normalizedPrompt)
                || containsAny(normalizedPrompt.toLowerCase(Locale.ROOT), "agreement", "arrange", "flexible", "any time", "anytime", "by agreement");
    }

    private boolean shouldAutoFillLocation(String normalizedPrompt, String focusSlotId) {
        if (focusSlotId != null && (
                "location_mode".equals(focusSlotId)
                        || "location_label".equals(focusSlotId)
                        || "location_candidate_confirmation".equals(focusSlotId)
        )) {
            return true;
        }
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        return containsAny(lower, "location", "address", "current location", "my location", "profile", "hide location", "hide", "place", "street", "square", "near", "at ");
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

    private void applyScheduleModeAnswer(Map<String, String> merged, String normalizedPrompt) {
        String scheduleMode = extractScheduleMode(normalizedPrompt);
        if (scheduleMode == null) {
            return;
        }

        merged.put("schedule_mode", scheduleMode);
        if ("agreement".equals(scheduleMode)) {
            merged.remove("scheduled_at");
        } else {
            applyScheduledAtAnswer(merged, normalizedPrompt);
        }
    }

    private void applyScheduledAtAnswer(Map<String, String> merged, String normalizedPrompt) {
        String scheduledAt = extractScheduledAt(normalizedPrompt);
        if (scheduledAt != null) {
            merged.put("scheduled_at", scheduledAt);
        }
    }

    private void applyLocationModeAnswer(Map<String, String> merged, String normalizedPrompt, String actorKey) {
        String locationMode = extractLocationMode(normalizedPrompt);
        if (locationMode == null) {
            return;
        }

        merged.put("location_mode", locationMode);
        if (!"custom".equals(locationMode)) {
            merged.remove("location_label");
            merged.remove("location_resolution_status");
            merged.remove("location_resolution_provider");
            merged.remove("location_candidate_confirmation");
            clearPendingLocationCandidate(merged);
            return;
        }

        String locationLabel = deriveLocationLabel(normalizedPrompt);
        if (locationLabel != null) {
            applyLocationLabelAnswer(merged, locationLabel, actorKey);
        }
    }

    private void applyLocationLabelAnswer(Map<String, String> merged, String normalizedPrompt, String actorKey) {
        String locationLabel = deriveLocationLabel(normalizedPrompt);
        if (locationLabel == null) {
            return;
        }

        Map<String, String> parsedLocation = visionLocationResolutionService.resolveCustomLocation(locationLabel, actorKey);
        clearPendingLocationCandidate(merged);
        merged.remove("location_candidate_confirmation");
        merged.putAll(parsedLocation);
    }

    private void applyLocationCandidateConfirmation(Map<String, String> merged, String normalizedPrompt) {
        String decision = extractLocationCandidateDecision(normalizedPrompt);
        if (decision == null) {
            return;
        }

        if ("resolved".equals(decision)) {
            String candidateKey = extractLocationCandidateKey(merged, normalizedPrompt);
            if (candidateKey == null) {
                return;
            }
            visionLocationResolutionService.confirmPendingCandidate(merged, candidateKey);
        } else if ("typed".equals(decision)) {
            visionLocationResolutionService.keepTypedLocation(merged);
        }
        merged.put("location_candidate_confirmation", decision);
    }

    private String actorKeyForConversation(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null || conversation.getOwner().getId() == null) {
            return "system:vision";
        }
        return "vision:user:" + conversation.getOwner().getId();
    }

    private String extractScheduleMode(String normalizedPrompt) {
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "agreement", "arrange", "flexible", "any time", "anytime", "dogovor", "po dogovoru")) {
            return "agreement";
        }
        if (visionScheduleParserService.suggestsFixedSchedule(normalizedPrompt)) {
            return "fixed";
        }
        return null;
    }

    private String extractScheduledAt(String normalizedPrompt) {
        return visionScheduleParserService.extractScheduledAt(normalizedPrompt);
    }

    private String extractLocationMode(String normalizedPrompt) {
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "hide", "hidden", "off", "no location", "without location")) {
            return "off";
        }
        if (containsAny(lower, "profile", "my location", "use my location", "saved location")) {
            return "profile";
        }
        if (containsAny(lower, "custom", "different place", "other place", "address", "at ")) {
            return "custom";
        }
        return null;
    }

    private String deriveLocationLabel(String prompt) {
        String cleaned = prompt
                .replaceAll("(?i)^custom\\s+", "")
                .replaceAll("(?i)^custom place\\s+", "")
                .replaceAll("(?i)^custom address\\s+", "")
                .replaceAll("(?i)^address\\s+", "")
                .replaceAll("(?i)^at\\s+", "")
                .trim();
        String normalized = cleaned.toLowerCase(Locale.ROOT);
        if (cleaned.isBlank()
                || normalized.equals("custom")
                || normalized.equals("place")
                || normalized.equals("address")
                || normalized.equals("custom place")
                || normalized.equals("custom address")) {
            return null;
        }
        return cleaned;
    }

    private String extractLocationCandidateDecision(String normalizedPrompt) {
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        if (lower.matches(".*\\bcandidate\\s+[1-3]\\b.*") || lower.matches(".*\\boption\\s+[1-3]\\b.*")) {
            return "resolved";
        }
        if (containsAny(
                lower,
                "use resolved",
                "resolved place",
                "use matched",
                "use suggested",
                "use lookup",
                "yes",
                "yeah",
                "yep",
                "sure",
                "ok",
                "okay",
                "that one",
                "use that",
                "sounds good",
                "correct",
                "da"
        )) {
            return "resolved";
        }
        if (containsAny(
                lower,
                "keep typed",
                "keep my text",
                "keep typed location",
                "use typed",
                "keep original",
                "keep mine",
                "my location",
                "my text",
                "no",
                "nope",
                "ne",
                "zadrzi",
                "zadrži"
        )) {
            return "typed";
        }
        return null;
    }

    private String extractLocationCandidateKey(Map<String, String> slotData, String normalizedPrompt) {
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        Matcher matcher = Pattern.compile("\\b(?:candidate|option)\\s+([1-3])\\b").matcher(lower);
        if (matcher.find()) {
            return "pending_location_candidate_" + matcher.group(1);
        }
        if ("1".equals(slotData.get("pending_location_candidate_count"))) {
            return "pending_location_candidate_1";
        }
        if (containsAny(
                lower,
                "use resolved",
                "resolved place",
                "use matched",
                "use suggested",
                "use lookup",
                "yes",
                "yeah",
                "yep",
                "sure",
                "ok",
                "okay",
                "that one",
                "use that",
                "sounds good",
                "correct",
                "da"
        )) {
            return "pending_location_candidate_1";
        }
        return null;
    }

    private void clearPendingLocationCandidate(Map<String, String> slotData) {
        slotData.remove("pending_location_label");
        slotData.remove("pending_location_country_code");
        slotData.remove("pending_location_country");
        slotData.remove("pending_location_locality");
        slotData.remove("pending_location_postal_code");
        slotData.remove("pending_location_street");
        slotData.remove("pending_location_house_number");
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
