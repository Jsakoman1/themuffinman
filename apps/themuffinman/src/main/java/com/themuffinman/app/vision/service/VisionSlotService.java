package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionReviewTarget;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
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
    private static final double MEDIUM_SLOT_CONFIDENCE = 0.45d;
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
            String description = deriveDescriptionFromPrompt(normalizedPrompt, requestedSlot, focusSlotId);
            merged.put("quest_description", description == null ? normalizedPrompt.trim() : description);
        } else if ("reward_amount".equals(requestedSlot) && !merged.containsKey("reward_amount") && !merged.containsKey("free_quest")) {
            applyRewardAnswer(merged, normalizedPrompt, requestedSlot, focusSlotId);
        } else if ("visibility".equals(requestedSlot) && !merged.containsKey("visibility")) {
            String visibility = extractVisibility(normalizedPrompt);
            if (visibility != null) {
                merged.put("visibility", visibility);
            }
        } else if ("schedule_mode".equals(requestedSlot)) {
            if (!merged.containsKey("schedule_mode")) {
                applyScheduleModeAnswer(merged, normalizedPrompt);
            }
            if ("fixed".equals(merged.get("schedule_mode"))) {
                if (!merged.containsKey("scheduled_date")) {
                    applyScheduledDateAnswer(merged, normalizedPrompt);
                }
                if (!merged.containsKey("scheduled_time")) {
                    applyScheduledTimeAnswer(merged, normalizedPrompt);
                }
            }
        } else if ("scheduled_date".equals(requestedSlot) && !merged.containsKey("scheduled_date")) {
            applyScheduledDateAnswer(merged, normalizedPrompt);
            if (!merged.containsKey("scheduled_time")) {
                applyScheduledTimeAnswer(merged, normalizedPrompt);
            }
        } else if ("scheduled_time".equals(requestedSlot) && !merged.containsKey("scheduled_time")) {
            applyScheduledTimeAnswer(merged, normalizedPrompt);
            if (!merged.containsKey("scheduled_date")) {
                applyScheduledDateAnswer(merged, normalizedPrompt);
            }
        } else if ("location_mode".equals(requestedSlot)) {
            if (!merged.containsKey("location_mode")) {
                applyLocationModeAnswer(merged, normalizedPrompt, actorKeyForConversation(conversation));
            }
            if ("custom".equals(merged.get("location_mode")) && !merged.containsKey("location_label")) {
                applyLocationLabelAnswer(merged, normalizedPrompt, actorKeyForConversation(conversation), false);
            }
        } else if ("location_label".equals(requestedSlot) && !merged.containsKey("location_label")) {
            applyLocationLabelAnswer(merged, normalizedPrompt, actorKeyForConversation(conversation), false);
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
                slotData.remove("scheduled_date");
                slotData.remove("scheduled_time");
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
        if ("reward_amount".equals(requestedSlot)
                || "reward_amount".equals(focusSlotId)
                || "free_quest".equals(requestedSlot)) {
            return;
        }
        if (!merged.containsKey("quest_description")) {
            String description = deriveDescriptionFromPrompt(prompt, requestedSlot, focusSlotId);
            if (description != null) {
                merged.put("quest_description", description);
            }
        }

        if (!merged.containsKey("quest_title")) {
            String title = deriveTitleFromPrompt(prompt);
            if (title != null) {
                merged.put("quest_title", title);
            }
        }

        if (!merged.containsKey("reward_amount") && !merged.containsKey("free_quest") && shouldAutoFillReward(prompt, requestedSlot, focusSlotId)) {
            applyRewardAnswer(merged, prompt, requestedSlot, focusSlotId);
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

        if ("fixed".equals(merged.get("schedule_mode"))) {
            if (!merged.containsKey("scheduled_date")) {
                applyScheduledDateAnswer(merged, prompt);
            }
            if (!merged.containsKey("scheduled_time")) {
                applyScheduledTimeAnswer(merged, prompt);
            }
        }

        if (!merged.containsKey("location_mode") && shouldAutoFillLocation(prompt, focusSlotId)) {
            applyLocationModeAnswer(merged, prompt, actorKey);
        }

        if ("custom".equals(merged.get("location_mode")) && !merged.containsKey("location_label")) {
            applyLocationLabelAnswer(merged, prompt, actorKey, false);
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

        applySemanticTitle(merged, understanding);
        applySemanticDescription(merged, understanding);
        applySemanticReward(merged, understanding);
        applySemanticVisibility(merged, understanding);
        applySemanticSchedule(merged, understanding);
        applySemanticLocation(merged, understanding, actorKey);
    }

    private void applySemanticTitle(Map<String, String> merged, VisionPromptUnderstandingResult understanding) {
        if (!shouldAcceptSemanticSlot(merged, understanding, "quest_title")) {
            return;
        }
        merged.put("quest_title", normalizeTitleAnswer(semanticSlotValue(understanding, "quest_title")));
    }

    private void applySemanticDescription(Map<String, String> merged, VisionPromptUnderstandingResult understanding) {
        if (!shouldAcceptSemanticSlot(merged, understanding, "quest_description")) {
            return;
        }
        merged.put("quest_description", normalizeSentenceSlotValue(semanticSlotValue(understanding, "quest_description")));
    }

    private void applySemanticReward(Map<String, String> merged, VisionPromptUnderstandingResult understanding) {
        if (shouldAcceptSemanticSlot(merged, understanding, "free_quest")) {
            String freeQuest = semanticSlotValue(understanding, "free_quest");
            if (freeQuest != null && Boolean.parseBoolean(freeQuest.trim())) {
                merged.put("free_quest", "true");
                merged.put("reward_amount", "0");
                return;
            }
        }

        if (!shouldAcceptSemanticSlot(merged, understanding, "reward_amount")) {
            return;
        }

        String rewardAmount = normalizeMoneySlotValue(semanticSlotValue(understanding, "reward_amount"));
        if (rewardAmount == null) {
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

    private void applySemanticVisibility(Map<String, String> merged, VisionPromptUnderstandingResult understanding) {
        if (!shouldAcceptSemanticSlot(merged, understanding, "visibility")) {
            return;
        }
        String visibility = semanticSlotValue(understanding, "visibility");
        if (visibility == null || visibility.isBlank()) {
            return;
        }
        String normalized = TextValueNormalizer.upperTrimToEmpty(visibility);
        if ("PUBLIC".equals(normalized) || "CIRCLES".equals(normalized)) {
            merged.put("visibility", normalized);
        }
    }

    private void applySemanticSchedule(Map<String, String> merged, VisionPromptUnderstandingResult understanding) {
        if (shouldAcceptSemanticSlot(merged, understanding, "schedule_mode")) {
            String scheduleMode = semanticSlotValue(understanding, "schedule_mode");
            if (scheduleMode != null && !scheduleMode.isBlank()) {
                String normalized = TextValueNormalizer.lowerTrimToEmpty(scheduleMode);
                if ("agreement".equals(normalized) || "fixed".equals(normalized)) {
                    merged.put("schedule_mode", normalized);
                }
            }
        }

        if (shouldAcceptSemanticSlot(merged, understanding, "scheduled_date")) {
            String scheduledDate = normalizeDateSlotValue(semanticSlotValue(understanding, "scheduled_date"));
            if (scheduledDate != null) {
                merged.put("scheduled_date", scheduledDate);
            }
        }

        if (shouldAcceptSemanticSlot(merged, understanding, "scheduled_time")) {
            String scheduledTime = normalizeTimeSlotValue(semanticSlotValue(understanding, "scheduled_time"));
            if (scheduledTime != null) {
                merged.put("scheduled_time", scheduledTime);
            }
        }
    }

    private void applySemanticLocation(
            Map<String, String> merged,
            VisionPromptUnderstandingResult understanding,
            String actorKey
    ) {
        if (shouldAcceptSemanticSlot(merged, understanding, "location_mode")) {
            String locationMode = semanticSlotValue(understanding, "location_mode");
            if (locationMode != null && !locationMode.isBlank()) {
                String normalized = TextValueNormalizer.lowerTrimToEmpty(locationMode);
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
        }

        if (shouldAcceptSemanticSlot(merged, understanding, "location_label")) {
            String locationLabel = semanticSlotValue(understanding, "location_label");
            if (locationLabel != null && !locationLabel.isBlank()) {
                String mode = merged.get("location_mode");
                if (!"off".equals(mode) && !"profile".equals(mode)) {
                    applyLocationLabelAnswer(merged, locationLabel, actorKey, true);
                }
            }
        }

        if (shouldAcceptSemanticSlot(merged, understanding, "location_candidate_confirmation")) {
            String locationCandidateConfirmation = semanticSlotValue(understanding, "location_candidate_confirmation");
            if (locationCandidateConfirmation != null && !locationCandidateConfirmation.isBlank()) {
                merged.put("location_candidate_confirmation", TextValueNormalizer.lowerTrimToEmpty(locationCandidateConfirmation));
            }
        }
    }

    private boolean shouldAutoFillReward(String normalizedPrompt, String requestedSlot, String focusSlotId) {
        if (focusSlotId != null && "reward_amount".equals(focusSlotId)) {
            return true;
        }
        if ("reward_amount".equals(requestedSlot)) {
            return true;
        }
        return containsRewardSignals(normalizedPrompt);
    }

    private boolean shouldAutoFillVisibility(String normalizedPrompt, String focusSlotId) {
        if (focusSlotId != null && "visibility".equals(focusSlotId)) {
            return true;
        }
        String lower = TextValueNormalizer.lowerToEmpty(normalizedPrompt);
        return containsAny(lower, "public", "circles", "friends only", "everyone", "private");
    }

    private boolean shouldAutoFillSchedule(String normalizedPrompt, String focusSlotId) {
        if (focusSlotId != null && (
                "schedule_mode".equals(focusSlotId)
                        || "scheduled_date".equals(focusSlotId)
                        || "scheduled_time".equals(focusSlotId)
        )) {
            return true;
        }
        return visionScheduleParserService.suggestsFixedSchedule(normalizedPrompt)
                || containsAny(TextValueNormalizer.lowerToEmpty(normalizedPrompt), "agreement", "arrange", "flexible", "any time", "anytime", "by agreement");
    }

    private boolean shouldAutoFillLocation(String normalizedPrompt, String focusSlotId) {
        if (focusSlotId != null && (
                "location_mode".equals(focusSlotId)
                        || "location_label".equals(focusSlotId)
                        || "location_candidate_confirmation".equals(focusSlotId)
        )) {
            return true;
        }
        return containsLocationSignals(normalizedPrompt);
    }

    private void applyRewardAnswer(Map<String, String> merged, String normalizedPrompt, String requestedSlot, String focusSlotId) {
        String lower = TextValueNormalizer.lowerToEmpty(normalizedPrompt);
        if (!containsRewardSignals(lower)
                && (focusSlotId == null || !"reward_amount".equals(focusSlotId))
                && (requestedSlot == null || !"reward_amount".equals(requestedSlot))) {
            return;
        }
        if (containsAny(lower, "free", "no pay", "without pay", "volunteer", "unpaid")) {
            merged.put("free_quest", "true");
            merged.put("reward_amount", "0");
            return;
        }

        String rewardAmount = extractRewardAmount(lower, requestedSlot, focusSlotId);
        if (rewardAmount != null) {
            BigDecimal amount = new BigDecimal(rewardAmount.replace(',', '.'));
            merged.put("free_quest", "false");
            merged.put("reward_amount", amount.stripTrailingZeros().toPlainString());
        }
    }

    private String extractRewardAmount(String lower, String requestedSlot, String focusSlotId) {
        Pattern[] prioritizedPatterns = new Pattern[] {
                Pattern.compile("(?i)(?:reward|price|pay|paid|amount|compensation|cost|for)\\D{0,12}(\\d{1,5}(?:[.,]\\d{1,2})?)\\s*(?:eur|euro|euros|kn)?"),
                Pattern.compile("(?i)(\\d{1,5}(?:[.,]\\d{1,2})?)\\s*(?:eur|euro|euros|kn)\\b")
        };

        for (Pattern pattern : prioritizedPatterns) {
            Matcher matcher = pattern.matcher(lower);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        if (focusSlotId != null && "reward_amount".equals(focusSlotId)) {
            Matcher matcher = AMOUNT_PATTERN.matcher(lower);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        if ("reward_amount".equals(requestedSlot)) {
            Matcher matcher = AMOUNT_PATTERN.matcher(lower);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private String extractVisibility(String normalizedPrompt) {
        String lower = TextValueNormalizer.lowerToEmpty(normalizedPrompt);
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
            merged.remove("scheduled_date");
            merged.remove("scheduled_time");
            return;
        }

        applyScheduledDateAnswer(merged, normalizedPrompt);
        applyScheduledTimeAnswer(merged, normalizedPrompt);
    }

    private void applyScheduledDateAnswer(Map<String, String> merged, String normalizedPrompt) {
        String scheduledDate = extractScheduledDate(normalizedPrompt);
        if (scheduledDate != null) {
            merged.put("scheduled_date", scheduledDate);
        }
    }

    private void applyScheduledTimeAnswer(Map<String, String> merged, String normalizedPrompt) {
        String scheduledTime = extractScheduledTime(normalizedPrompt);
        if (scheduledTime != null) {
            merged.put("scheduled_time", scheduledTime);
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
            applyLocationLabelAnswer(merged, locationLabel, actorKey, true);
        }
    }

    private void applyLocationLabelAnswer(Map<String, String> merged, String locationLabelInput, String actorKey, boolean semanticProvided) {
        String locationLabel = semanticProvided ? trimToNull(locationLabelInput) : deriveLocationLabel(locationLabelInput);
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

    private boolean shouldAcceptSemanticSlot(
            Map<String, String> merged,
            VisionPromptUnderstandingResult understanding,
            String slotId
    ) {
        String value = semanticSlotValue(understanding, slotId);
        Double confidence = semanticSlotConfidence(understanding, slotId);
        if (value == null || value.isBlank() || confidence == null) {
            return false;
        }
        if (confidence >= VisionPromptUnderstandingResult.MIN_SLOT_CONFIDENCE) {
            return true;
        }
        if (confidence >= MEDIUM_SLOT_CONFIDENCE) {
            return !hasText(merged.get(slotId));
        }
        return false;
    }

    private String semanticSlotValue(VisionPromptUnderstandingResult understanding, String slotId) {
        return understanding == null ? null : understanding.slotValue(slotId);
    }

    private Double semanticSlotConfidence(VisionPromptUnderstandingResult understanding, String slotId) {
        return understanding == null ? null : understanding.slotConfidence(slotId);
    }

    private String normalizeSentenceSlotValue(String value) {
        String cleaned = trimToNull(value);
        return cleaned == null ? null : capitalize(cleaned);
    }

    private String normalizeMoneySlotValue(String value) {
        String cleaned = trimToNull(value);
        if (cleaned == null) {
            return null;
        }

        Matcher matcher = AMOUNT_PATTERN.matcher(cleaned.replaceAll("[^\\d.,]", " "));
        if (!matcher.find()) {
            return cleaned;
        }

        try {
            return new BigDecimal(matcher.group(1).replace(',', '.')).stripTrailingZeros().toPlainString();
        } catch (NumberFormatException ignored) {
            return cleaned;
        }
    }

    private String normalizeDateSlotValue(String value) {
        String cleaned = trimToNull(value);
        if (cleaned == null) {
            return null;
        }
        String parsed = visionScheduleParserService.extractScheduledDate(cleaned);
        return parsed == null ? cleaned : parsed;
    }

    private String normalizeTimeSlotValue(String value) {
        String cleaned = trimToNull(value);
        if (cleaned == null) {
            return null;
        }
        String parsed = visionScheduleParserService.extractScheduledTime(cleaned);
        return parsed == null ? cleaned : parsed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String actorKeyForConversation(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null || conversation.getOwner().getId() == null) {
            return "system:vision";
        }
        return "vision:user:" + conversation.getOwner().getId();
    }

    private String extractScheduleMode(String normalizedPrompt) {
        String lower = TextValueNormalizer.lowerToEmpty(normalizedPrompt);
        if (containsAny(lower, "agreement", "arrange", "flexible", "any time", "anytime", "dogovor", "po dogovoru")) {
            return "agreement";
        }
        if (visionScheduleParserService.suggestsFixedSchedule(normalizedPrompt)) {
            return "fixed";
        }
        return null;
    }

    private String extractScheduledDate(String normalizedPrompt) {
        return visionScheduleParserService.extractScheduledDate(normalizedPrompt);
    }

    private String extractScheduledTime(String normalizedPrompt) {
        return visionScheduleParserService.extractScheduledTime(normalizedPrompt);
    }

    private String extractLocationMode(String normalizedPrompt) {
        String lower = TextValueNormalizer.lowerToEmpty(normalizedPrompt);
        if (containsAny(lower, "hide", "hidden", "off", "no location", "without location")) {
            return "off";
        }
        if (containsAny(lower, "profile", "my location", "use my location", "saved location")) {
            return "profile";
        }
        if (containsAny(lower, "custom", "different place", "other place", "address") || containsLocationAtSignal(lower)) {
            return "custom";
        }
        return null;
    }

    private String deriveLocationLabel(String prompt) {
        String source = prompt == null ? "" : prompt.trim();
        Matcher trailingLocation = Pattern.compile("(?i).*(?:\\b(?:at|in|near|address)\\s+)([\\p{L}][^.!?]*)$").matcher(source);
        String cleaned = trailingLocation.matches()
                ? trailingLocation.group(1).trim()
                : source
                .replaceAll("(?i)^custom\\s+", "")
                .replaceAll("(?i)^custom place\\s+", "")
                .replaceAll("(?i)^custom address\\s+", "")
                .replaceAll("(?i)^address\\s+", "")
                .replaceAll("(?i)^at\\s+", "")
                .trim();
        String normalized = TextValueNormalizer.lowerTrimToEmpty(cleaned);
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
        String lower = TextValueNormalizer.lowerToEmpty(normalizedPrompt);
        if (lower.matches(".*\\bcandidate\\s+[1-3]\\b.*") || lower.matches(".*\\boption\\s+[1-3]\\b.*")) {
            return "resolved";
        }
        if (containsAny(lower, "first", "1st", "second", "2nd", "third", "3rd", "prvi", "drugi", "treći", "treci")) {
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
        String lower = TextValueNormalizer.lowerToEmpty(normalizedPrompt);
        Matcher matcher = Pattern.compile("\\b(?:candidate|option|choice)\\s+([1-3])\\b").matcher(lower);
        if (matcher.find()) {
            return "pending_location_candidate_" + matcher.group(1);
        }
        Integer ordinal = extractOrdinalLocationCandidate(lower);
        if (ordinal != null) {
            return "pending_location_candidate_" + ordinal;
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

    private Integer extractOrdinalLocationCandidate(String lower) {
        if (containsAny(lower, "first", "1st", "prvi", "prva", "prvo")) {
            return 1;
        }
        if (containsAny(lower, "second", "2nd", "drugi", "druga", "drugo")) {
            return 2;
        }
        if (containsAny(lower, "third", "3rd", "treci", "treći", "treća", "trece", "treće")) {
            return 3;
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
        String core = deriveTaskCoreFromPrompt(prompt);
        if (core == null || isGenericCreateQuestCommand(core)) {
            return null;
        }
        if (core.length() > 80) {
            core = core.substring(0, 80).trim();
        }
        if (core.length() < 4) {
            return null;
        }
        return capitalize(core);
    }

    private String deriveDescriptionFromPrompt(String prompt, String requestedSlot, String focusSlotId) {
        String core = deriveTaskCoreFromPrompt(prompt);
        if (core == null) {
            return null;
        }
        if (focusSlotId != null && "quest_description".equals(focusSlotId)) {
            return capitalize(core);
        }
        if ("quest_description".equals(requestedSlot)) {
            return capitalize(core);
        }
        if (isGenericCreateQuestCommand(core)) {
            return null;
        }
        return capitalize(core);
    }

    private String deriveTaskCoreFromPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
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
        int rewardMarker = indexOfAny(TextValueNormalizer.lowerToEmpty(firstSentence), " for ", " with ", " paying ", " pay ");
        int scheduleMarker = indexOfAny(TextValueNormalizer.lowerToEmpty(firstSentence), " next ", " tomorrow", " today", " tonight");
        int locationMarker = indexOfAny(TextValueNormalizer.lowerToEmpty(firstSentence), " at ", " near ", " address ", " location ");
        int boundary = earliestPositiveIndex(rewardMarker, scheduleMarker, locationMarker);
        if (boundary > 10) {
            firstSentence = firstSentence.substring(0, boundary).trim();
        }

        if (firstSentence.length() < 4) {
            return null;
        }
        return firstSentence;
    }

    private boolean containsRewardSignals(String normalizedPrompt) {
        String lower = TextValueNormalizer.lowerToEmpty(normalizedPrompt);
        return containsAny(lower, "free", "no pay", "without pay", "unpaid", "reward", "euros", "euro", "eur", "kn", "pay", "compensation", "amount", "price");
    }

    private boolean containsLocationSignals(String normalizedPrompt) {
        String lower = TextValueNormalizer.lowerToEmpty(normalizedPrompt);
        return containsAny(lower, "location", "address", "current location", "my location", "hide location", "place", "street", "square", "near")
                || containsLocationAtSignal(lower);
    }

    private boolean containsLocationAtSignal(String lower) {
        return Pattern.compile("\\bat\\s+[\\p{L}]").matcher(lower).find();
    }

    private int earliestPositiveIndex(int... indexes) {
        int best = -1;
        for (int index : indexes) {
            if (index < 0) {
                continue;
            }
            if (best < 0 || index < best) {
                best = index;
            }
        }
        return best;
    }

    private String normalizeTitleAnswer(String prompt) {
        String title = prompt.trim();
        if (title.length() > 255) {
            title = title.substring(0, 255).trim();
        }
        return capitalize(title);
    }

    private boolean isGenericCreateQuestCommand(String prompt) {
        String lower = TextValueNormalizer.lowerTrimToEmpty(prompt);
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
        return TextValueNormalizer.upperToEmpty(normalized.substring(0, 1)) + normalized.substring(1);
    }
}
