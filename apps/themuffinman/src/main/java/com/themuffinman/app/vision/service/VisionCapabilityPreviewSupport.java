package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

final class VisionCapabilityPreviewSupport {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault());

    private VisionCapabilityPreviewSupport() {
    }

    static void addItem(List<VisionSlotSummaryDTO> items, String slotId, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        items.add(VisionSlotSummaryDTO.builder()
                .slotId(slotId)
                .label(label)
                .value(value)
                .build());
    }

    static long countFilledValues(List<VisionSlotSummaryDTO> items) {
        return items.stream()
                .map(VisionSlotSummaryDTO::getValue)
                .filter(VisionCapabilityPreviewSupport::hasText)
                .count();
    }

    static String draftSummary(long filledFieldCount, String emptySummary, String partialSummary, String fullSummary, int fullThreshold) {
        if (filledFieldCount == 0) {
            return emptySummary;
        }
        if (filledFieldCount < fullThreshold) {
            return partialSummary;
        }
        return fullSummary;
    }

    static String formatRewardLabel(QuestResponseDTO quest) {
        if (quest == null || quest.getAwardAmount() == null || quest.getAwardAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "Free";
        }
        return quest.getAwardAmount().stripTrailingZeros().toPlainString();
    }

    static String formatQuestDraftRewardLabel(Map<String, String> slotData) {
        if (slotData == null || "true".equals(slotData.get("free_quest"))) {
            return "Free";
        }
        String rewardAmount = slotData.get("reward_amount");
        return rewardAmount == null || rewardAmount.isBlank() ? null : rewardAmount;
    }

    static String formatQuestDraftScheduleMode(Map<String, String> slotData) {
        if (slotData == null) {
            return null;
        }

        String mode = slotData.get("schedule_mode");
        if (mode == null || mode.isBlank()) {
            return null;
        }
        if ("fixed".equals(mode)) {
            return "Fixed time";
        }
        if ("agreement".equals(mode)) {
            return "By agreement";
        }
        return mode;
    }

    static String formatQuestDraftLocationMode(Map<String, String> slotData) {
        if (slotData == null) {
            return null;
        }

        String mode = slotData.get("location_mode");
        if (mode == null || mode.isBlank()) {
            return null;
        }
        if ("profile".equals(mode)) {
            return "Use profile location";
        }
        if ("off".equals(mode)) {
            return "Hide location";
        }
        if ("custom".equals(mode)) {
            return "Custom place";
        }
        return mode;
    }

    static String formatDateTime(Instant value) {
        if (value == null) {
            return null;
        }
        return DATE_TIME_FORMAT.format(value);
    }

    static String applicationListValue(QuestApplicationResponseDTO application) {
        if (application == null) {
            return null;
        }

        String statusLabel = application.getPresentation() == null
                ? application.getStatus() == null ? null : application.getStatus().name()
                : application.getPresentation().getStatusLabel();
        String nextActionLabel = nextActionLabel(application);
        if (!hasText(statusLabel)) {
            return nextActionLabel;
        }
        if (!hasText(nextActionLabel)) {
            return statusLabel;
        }
        return statusLabel + " · " + nextActionLabel;
    }

    static String thingListingValue(ThingListingResponseDTO listing) {
        if (listing == null) {
            return null;
        }

        String detail = hasText(listing.getDescription())
                ? listing.getDescription().trim()
                : listing.getOwnerUsername();
        if (!hasText(detail)) {
            return listing.isAvailable() ? "Available" : "Unavailable";
        }
        return (listing.isAvailable() ? "Available" : "Unavailable") + " · " + detail.trim();
    }

    private static String nextActionLabel(QuestApplicationResponseDTO application) {
        if (application == null || application.getAllowedActions() == null || application.getAllowedActions().isEmpty()) {
            return null;
        }
        return application.getAllowedActions().get(0) == null ? null : application.getAllowedActions().get(0).name().toLowerCase();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
