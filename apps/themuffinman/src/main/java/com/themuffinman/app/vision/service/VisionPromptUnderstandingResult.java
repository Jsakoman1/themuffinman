package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VisionPromptUnderstandingResult {
    static final double MIN_SLOT_CONFIDENCE = 0.75d;

    private String sourceLanguage;
    private String originalPrompt;
    private String normalizedPrompt;
    private String translationProvider;
    private String focusSlotId;
    private Double focusSlotConfidence;
    @Builder.Default
    private VisionSemanticPlan semanticPlan = VisionSemanticPlan.empty();
    private boolean translationApplied;
    private boolean translationReliable;
    @Builder.Default
    private VisionPromptUnderstandingSlots slots = new VisionPromptUnderstandingSlots();

    public static VisionPromptUnderstandingResult empty(String prompt) {
        return VisionPromptUnderstandingResult.builder()
                .sourceLanguage("unknown")
                .originalPrompt(prompt)
                .normalizedPrompt(prompt)
                .translationProvider("none")
                .focusSlotId(null)
                .focusSlotConfidence(null)
                .semanticPlan(VisionSemanticPlan.empty())
                .translationApplied(false)
                .translationReliable(true)
                .slots(new VisionPromptUnderstandingSlots())
                .build();
    }

    public Map<String, String> toExtractedSlotMap() {
        Map<String, String> flattened = new LinkedHashMap<>();
        if (slots == null) {
            return flattened;
        }

        putIfExplicit(flattened, "quest_title", slots.getQuestTitle(), slots.getQuestTitleConfidence());
        putIfExplicit(flattened, "quest_description", slots.getQuestDescription(), slots.getQuestDescriptionConfidence());
        putIfExplicit(flattened, "circle_name", slots.getCircleName(), slots.getCircleNameConfidence());
        putIfExplicit(flattened, "target_circle_query", slots.getTargetCircleQuery(), slots.getTargetCircleQueryConfidence());
        putIfExplicit(flattened, "target_quest_query", slots.getApplicationQuestQuery(), slots.getApplicationQuestQueryConfidence());
        putIfExplicit(flattened, "application_message", slots.getApplicationMessage(), slots.getApplicationMessageConfidence());
        putIfExplicit(flattened, "application_proposed_price", slots.getApplicationProposedPrice(), slots.getApplicationProposedPriceConfidence());
        putIfExplicit(flattened, "profile_username", slots.getProfileUsername(), slots.getProfileUsernameConfidence());
        putIfExplicit(flattened, "profile_description", slots.getProfileDescription(), slots.getProfileDescriptionConfidence());
        putIfExplicit(flattened, "profile_location_mode", slots.getProfileLocationMode(), slots.getProfileLocationModeConfidence());
        putIfExplicit(flattened, "profile_location_label", slots.getProfileLocationLabel(), slots.getProfileLocationLabelConfidence());
        putIfExplicit(flattened, "visibility", slots.getVisibility(), slots.getVisibilityConfidence());

        VisionPromptUnderstandingRewardSlots reward = slots.getReward();
        if (reward != null) {
            if (reward.getFreeQuest() != null && isExplicit(reward.getFreeQuestConfidence())) {
                flattened.put("free_quest", reward.getFreeQuest().toString());
            }
            putIfExplicit(flattened, "reward_amount", reward.getAmount(), reward.getAmountConfidence());
        }

        VisionPromptUnderstandingScheduleSlots schedule = slots.getSchedule();
        if (schedule != null) {
            putIfExplicit(flattened, "schedule_mode", schedule.getMode(), schedule.getModeConfidence());
            putIfExplicit(flattened, "scheduled_date", schedule.getScheduledDate(), schedule.getScheduledDateConfidence());
            putIfExplicit(flattened, "scheduled_time", schedule.getScheduledTime(), schedule.getScheduledTimeConfidence());
        }

        VisionPromptUnderstandingLocationSlots location = slots.getLocation();
        if (location != null) {
            putIfExplicit(flattened, "location_mode", location.getMode(), location.getModeConfidence());
            putIfExplicit(flattened, "location_label", location.getLabel(), location.getLabelConfidence());
            putIfExplicit(flattened, "location_candidate_confirmation", location.getCandidateConfirmation(), location.getCandidateConfirmationConfidence());
        }

        return flattened;
    }

    public VisionSemanticPlan semanticPlanOrEmpty() {
        return semanticPlan == null ? VisionSemanticPlan.empty() : semanticPlan;
    }

    private void putIfExplicit(Map<String, String> target, String key, String value, Double confidence) {
        if (value == null || value.isBlank() || !isExplicit(confidence)) {
            return;
        }
        target.put(key, value.trim());
    }

    private boolean isExplicit(Double confidence) {
        return confidence != null && confidence >= MIN_SLOT_CONFIDENCE;
    }

    public boolean isFocusedOn(String slotId) {
        return focusSlotId != null && focusSlotId.equals(slotId) && isExplicit(focusSlotConfidence);
    }
}

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class VisionPromptUnderstandingSlots {
    private String circleName;
    private Double circleNameConfidence;
    private String targetCircleQuery;
    private Double targetCircleQueryConfidence;
    private String applicationQuestQuery;
    private Double applicationQuestQueryConfidence;
    private String applicationMessage;
    private Double applicationMessageConfidence;
    private String applicationProposedPrice;
    private Double applicationProposedPriceConfidence;
    private String profileUsername;
    private Double profileUsernameConfidence;
    private String profileDescription;
    private Double profileDescriptionConfidence;
    private String profileLocationMode;
    private Double profileLocationModeConfidence;
    private String profileLocationLabel;
    private Double profileLocationLabelConfidence;
    private String questTitle;
    private Double questTitleConfidence;
    private String questDescription;
    private Double questDescriptionConfidence;
    private String visibility;
    private Double visibilityConfidence;
    @Builder.Default
    private VisionPromptUnderstandingRewardSlots reward = new VisionPromptUnderstandingRewardSlots();
    @Builder.Default
    private VisionPromptUnderstandingScheduleSlots schedule = new VisionPromptUnderstandingScheduleSlots();
    @Builder.Default
    private VisionPromptUnderstandingLocationSlots location = new VisionPromptUnderstandingLocationSlots();
}

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class VisionPromptUnderstandingRewardSlots {
    private Boolean freeQuest;
    private Double freeQuestConfidence;
    private String amount;
    private Double amountConfidence;
}

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class VisionPromptUnderstandingScheduleSlots {
    private String mode;
    private Double modeConfidence;
    private String scheduledDate;
    private Double scheduledDateConfidence;
    private String scheduledTime;
    private Double scheduledTimeConfidence;
}

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class VisionPromptUnderstandingLocationSlots {
    private String mode;
    private Double modeConfidence;
    private String label;
    private Double labelConfidence;
    private String candidateConfirmation;
    private Double candidateConfirmationConfidence;
}
