package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.semantic.SemanticEnvelope;
import com.themuffinman.app.semantic.SemanticReplayRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
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
    private String understandingProvider;
    private String understandingStatus;
    private String semanticContractVersion;
    @Builder.Default
    private SemanticEnvelope semanticEnvelope = SemanticEnvelope.builder().build();
    private String focusSlotId;
    private Double focusSlotConfidence;
    private boolean clarificationRequired;
    @Builder.Default
    private List<String> requiredSlotIds = List.of();
    @Builder.Default
    private List<String> missingRequiredSlotIds = List.of();
    @Builder.Default
    private VisionSemanticPlan semanticPlan = VisionSemanticPlan.empty();
    private boolean repairAttempted;
    private String repairSlotId;
    private String repairNote;
    private boolean translationApplied;
    private boolean translationReliable;
    @Builder.Default
    private VisionPromptUnderstandingSlots slots = new VisionPromptUnderstandingSlots();

    public static VisionPromptUnderstandingResult empty(String prompt) {
        return empty(prompt, Instant.now().toString());
    }

    public static VisionPromptUnderstandingResult empty(String prompt, String capturedAt) {
        String effectiveCapturedAt = capturedAt == null || capturedAt.isBlank() ? Instant.now().toString() : capturedAt;
        SemanticReplayRecord replayRecord = SemanticReplayRecord.builder()
                .contractVersion("vision-semantic-orchestration-v1")
                .provider("none")
                .model("none")
                .sourceLanguage("unknown")
                .rawUserText(prompt)
                .normalizedEnglishText(prompt)
                .intent("UNSUPPORTED")
                .entityFamily(SemanticEntityFamily.UNKNOWN)
                .requiredSlotIds(List.of())
                .missingRequiredSlotIds(List.of())
                .clarificationRequired(false)
                .confidence(0.0d)
                .ambiguityReason("No semantic understanding was produced.")
                .capturedAt(effectiveCapturedAt)
                .build();
        return VisionPromptUnderstandingResult.builder()
                .sourceLanguage("unknown")
                .originalPrompt(prompt)
                .normalizedPrompt(prompt)
                .translationProvider("none")
                .understandingProvider("none")
                .understandingStatus("not_applicable")
                .semanticContractVersion("vision-semantic-orchestration-v1")
                .semanticEnvelope(SemanticEnvelope.builder()
                        .rawUserText(prompt)
                        .normalizedEnglishText(prompt)
                        .localizedDisplayText(prompt)
                        .sourceLanguage("unknown")
                        .contractVersion("vision-semantic-orchestration-v1")
                        .translationProvider("none")
                        .translationApplied(false)
                        .translationReliable(true)
                        .clarificationRequired(false)
                        .requiredSlotIds(List.of())
                        .missingRequiredSlotIds(List.of())
                        .replayRecord(replayRecord)
                        .build())
                .focusSlotId(null)
                .focusSlotConfidence(null)
                .clarificationRequired(false)
                .requiredSlotIds(List.of())
                .missingRequiredSlotIds(List.of())
                .semanticPlan(VisionSemanticPlan.empty())
                .repairAttempted(false)
                .repairSlotId(null)
                .repairNote(null)
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
        putIfExplicit(flattened, "target_application_query", slots.getApplicationTargetQuery(), slots.getApplicationTargetQueryConfidence());
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

    public String slotValue(String slotId) {
        if (slotId == null || slotId.isBlank() || slots == null) {
            return null;
        }

        return switch (slotId) {
            case "quest_title" -> slots.getQuestTitle();
            case "quest_description" -> slots.getQuestDescription();
            case "circle_name" -> slots.getCircleName();
            case "target_circle_query" -> slots.getTargetCircleQuery();
            case "target_application_query" -> slots.getApplicationTargetQuery();
            case "target_quest_query" -> slots.getApplicationQuestQuery();
            case "application_message" -> slots.getApplicationMessage();
            case "application_proposed_price" -> slots.getApplicationProposedPrice();
            case "profile_username" -> slots.getProfileUsername();
            case "profile_description" -> slots.getProfileDescription();
            case "profile_location_mode" -> slots.getProfileLocationMode();
            case "profile_location_label" -> slots.getProfileLocationLabel();
            case "visibility" -> slots.getVisibility();
            case "free_quest" -> slots.getReward() == null || slots.getReward().getFreeQuest() == null
                    ? null
                    : slots.getReward().getFreeQuest().toString();
            case "reward_amount" -> slots.getReward() == null ? null : slots.getReward().getAmount();
            case "schedule_mode" -> slots.getSchedule() == null ? null : slots.getSchedule().getMode();
            case "scheduled_date" -> slots.getSchedule() == null ? null : slots.getSchedule().getScheduledDate();
            case "scheduled_time" -> slots.getSchedule() == null ? null : slots.getSchedule().getScheduledTime();
            case "location_mode" -> slots.getLocation() == null ? null : slots.getLocation().getMode();
            case "location_label" -> slots.getLocation() == null ? null : slots.getLocation().getLabel();
            case "location_candidate_confirmation" -> slots.getLocation() == null ? null : slots.getLocation().getCandidateConfirmation();
            default -> null;
        };
    }

    public Double slotConfidence(String slotId) {
        if (slotId == null || slotId.isBlank() || slots == null) {
            return null;
        }

        return switch (slotId) {
            case "quest_title" -> slots.getQuestTitleConfidence();
            case "quest_description" -> slots.getQuestDescriptionConfidence();
            case "circle_name" -> slots.getCircleNameConfidence();
            case "target_circle_query" -> slots.getTargetCircleQueryConfidence();
            case "target_application_query" -> slots.getApplicationTargetQueryConfidence();
            case "target_quest_query" -> slots.getApplicationQuestQueryConfidence();
            case "application_message" -> slots.getApplicationMessageConfidence();
            case "application_proposed_price" -> slots.getApplicationProposedPriceConfidence();
            case "profile_username" -> slots.getProfileUsernameConfidence();
            case "profile_description" -> slots.getProfileDescriptionConfidence();
            case "profile_location_mode" -> slots.getProfileLocationModeConfidence();
            case "profile_location_label" -> slots.getProfileLocationLabelConfidence();
            case "visibility" -> slots.getVisibilityConfidence();
            case "free_quest" -> slots.getReward() == null ? null : slots.getReward().getFreeQuestConfidence();
            case "reward_amount" -> slots.getReward() == null ? null : slots.getReward().getAmountConfidence();
            case "schedule_mode" -> slots.getSchedule() == null ? null : slots.getSchedule().getModeConfidence();
            case "scheduled_date" -> slots.getSchedule() == null ? null : slots.getSchedule().getScheduledDateConfidence();
            case "scheduled_time" -> slots.getSchedule() == null ? null : slots.getSchedule().getScheduledTimeConfidence();
            case "location_mode" -> slots.getLocation() == null ? null : slots.getLocation().getModeConfidence();
            case "location_label" -> slots.getLocation() == null ? null : slots.getLocation().getLabelConfidence();
            case "location_candidate_confirmation" -> slots.getLocation() == null ? null : slots.getLocation().getCandidateConfirmationConfidence();
            default -> null;
        };
    }

    public boolean hasConfidentSlot(String slotId, double minConfidence) {
        Double confidence = slotConfidence(slotId);
        return confidence != null && confidence >= minConfidence && slotValue(slotId) != null && !slotValue(slotId).isBlank();
    }

    public void copySlotValueFrom(VisionPromptUnderstandingResult source, String slotId) {
        if (source == null || slotId == null || slotId.isBlank()) {
            return;
        }
        ensureSlots();
        switch (slotId) {
            case "quest_title" -> {
                slots.setQuestTitle(source.slotValue(slotId));
                slots.setQuestTitleConfidence(source.slotConfidence(slotId));
            }
            case "quest_description" -> {
                slots.setQuestDescription(source.slotValue(slotId));
                slots.setQuestDescriptionConfidence(source.slotConfidence(slotId));
            }
            case "circle_name" -> {
                slots.setCircleName(source.slotValue(slotId));
                slots.setCircleNameConfidence(source.slotConfidence(slotId));
            }
            case "target_circle_query" -> {
                slots.setTargetCircleQuery(source.slotValue(slotId));
                slots.setTargetCircleQueryConfidence(source.slotConfidence(slotId));
            }
            case "target_application_query" -> {
                slots.setApplicationTargetQuery(source.slotValue(slotId));
                slots.setApplicationTargetQueryConfidence(source.slotConfidence(slotId));
            }
            case "target_quest_query" -> {
                slots.setApplicationQuestQuery(source.slotValue(slotId));
                slots.setApplicationQuestQueryConfidence(source.slotConfidence(slotId));
            }
            case "application_message" -> {
                slots.setApplicationMessage(source.slotValue(slotId));
                slots.setApplicationMessageConfidence(source.slotConfidence(slotId));
            }
            case "application_proposed_price" -> {
                slots.setApplicationProposedPrice(source.slotValue(slotId));
                slots.setApplicationProposedPriceConfidence(source.slotConfidence(slotId));
            }
            case "profile_username" -> {
                slots.setProfileUsername(source.slotValue(slotId));
                slots.setProfileUsernameConfidence(source.slotConfidence(slotId));
            }
            case "profile_description" -> {
                slots.setProfileDescription(source.slotValue(slotId));
                slots.setProfileDescriptionConfidence(source.slotConfidence(slotId));
            }
            case "profile_location_mode" -> {
                slots.setProfileLocationMode(source.slotValue(slotId));
                slots.setProfileLocationModeConfidence(source.slotConfidence(slotId));
            }
            case "profile_location_label" -> {
                slots.setProfileLocationLabel(source.slotValue(slotId));
                slots.setProfileLocationLabelConfidence(source.slotConfidence(slotId));
            }
            case "visibility" -> {
                slots.setVisibility(source.slotValue(slotId));
                slots.setVisibilityConfidence(source.slotConfidence(slotId));
            }
            case "free_quest" -> {
                if (source.getSlots() != null && source.getSlots().getReward() != null) {
                    slots.getReward().setFreeQuest(source.getSlots().getReward().getFreeQuest());
                    slots.getReward().setFreeQuestConfidence(source.getSlots().getReward().getFreeQuestConfidence());
                }
            }
            case "reward_amount" -> {
                if (source.getSlots() != null && source.getSlots().getReward() != null) {
                    slots.getReward().setAmount(source.getSlots().getReward().getAmount());
                    slots.getReward().setAmountConfidence(source.getSlots().getReward().getAmountConfidence());
                }
            }
            case "schedule_mode" -> {
                if (source.getSlots() != null && source.getSlots().getSchedule() != null) {
                    slots.getSchedule().setMode(source.getSlots().getSchedule().getMode());
                    slots.getSchedule().setModeConfidence(source.getSlots().getSchedule().getModeConfidence());
                }
            }
            case "scheduled_date" -> {
                if (source.getSlots() != null && source.getSlots().getSchedule() != null) {
                    slots.getSchedule().setScheduledDate(source.getSlots().getSchedule().getScheduledDate());
                    slots.getSchedule().setScheduledDateConfidence(source.getSlots().getSchedule().getScheduledDateConfidence());
                }
            }
            case "scheduled_time" -> {
                if (source.getSlots() != null && source.getSlots().getSchedule() != null) {
                    slots.getSchedule().setScheduledTime(source.getSlots().getSchedule().getScheduledTime());
                    slots.getSchedule().setScheduledTimeConfidence(source.getSlots().getSchedule().getScheduledTimeConfidence());
                }
            }
            case "location_mode" -> {
                if (source.getSlots() != null && source.getSlots().getLocation() != null) {
                    slots.getLocation().setMode(source.getSlots().getLocation().getMode());
                    slots.getLocation().setModeConfidence(source.getSlots().getLocation().getModeConfidence());
                }
            }
            case "location_label" -> {
                if (source.getSlots() != null && source.getSlots().getLocation() != null) {
                    slots.getLocation().setLabel(source.getSlots().getLocation().getLabel());
                    slots.getLocation().setLabelConfidence(source.getSlots().getLocation().getLabelConfidence());
                }
            }
            case "location_candidate_confirmation" -> {
                if (source.getSlots() != null && source.getSlots().getLocation() != null) {
                    slots.getLocation().setCandidateConfirmation(source.getSlots().getLocation().getCandidateConfirmation());
                    slots.getLocation().setCandidateConfirmationConfidence(source.getSlots().getLocation().getCandidateConfirmationConfidence());
                }
            }
            default -> {
            }
        }
    }

    private void ensureSlots() {
        if (slots == null) {
            slots = new VisionPromptUnderstandingSlots();
        }
        if (slots.getReward() == null) {
            slots.setReward(new VisionPromptUnderstandingRewardSlots());
        }
        if (slots.getSchedule() == null) {
            slots.setSchedule(new VisionPromptUnderstandingScheduleSlots());
        }
        if (slots.getLocation() == null) {
            slots.setLocation(new VisionPromptUnderstandingLocationSlots());
        }
    }

    public VisionSemanticPlan semanticPlanOrEmpty() {
        return semanticPlan == null ? VisionSemanticPlan.empty() : semanticPlan;
    }

    public SemanticEnvelope semanticEnvelopeOrEmpty() {
        return semanticEnvelope == null ? SemanticEnvelope.builder().build() : semanticEnvelope;
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
    private String applicationTargetQuery;
    private Double applicationTargetQueryConfidence;
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
