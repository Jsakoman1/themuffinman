package com.themuffinman.app.vision.model;

public enum VisionReviewTarget {
    TITLE("quest_title"),
    DESCRIPTION("quest_description"),
    REWARD("reward_amount"),
    VISIBILITY("visibility"),
    SCHEDULE("schedule_mode"),
    LOCATION("location_mode"),
    CIRCLE_NAME("circle_name"),
    TARGET_USER("target_user"),
    TARGET_QUEST("target_quest_query"),
    TARGET_CIRCLE("target_circle_query"),
    APPLICATION_MESSAGE("application_message"),
    APPLICATION_PRICE("application_proposed_price"),
    PROFILE_USERNAME("profile_username"),
    PROFILE_DESCRIPTION("profile_description"),
    PROFILE_LOCATION_MODE("profile_location_mode"),
    PROFILE_LOCATION("profile_location_label");

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

        String normalized = rawValue.trim();
        try {
            return VisionReviewTarget.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException exception) {
            for (VisionReviewTarget reviewTarget : values()) {
                if (reviewTarget.slotId.equalsIgnoreCase(normalized)) {
                    return reviewTarget;
                }
            }
            return null;
        }
    }
}
