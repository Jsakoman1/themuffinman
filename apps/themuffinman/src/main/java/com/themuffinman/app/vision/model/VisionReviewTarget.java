package com.themuffinman.app.vision.model;

public enum VisionReviewTarget {
    TITLE("quest_title"),
    DESCRIPTION("quest_description"),
    REWARD("reward_amount"),
    VISIBILITY("visibility"),
    SCHEDULE("schedule_mode"),
    LOCATION("location_mode");

    private final String slotId;

    VisionReviewTarget(String slotId) {
        this.slotId = slotId;
    }

    public String getSlotId() {
        return slotId;
    }

    public static VisionReviewTarget from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        try {
            return VisionReviewTarget.valueOf(rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
