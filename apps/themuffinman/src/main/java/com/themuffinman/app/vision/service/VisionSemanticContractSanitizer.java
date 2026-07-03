package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionIntent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class VisionSemanticContractSanitizer {

    public VisionPromptUnderstandingResult sanitize(
            VisionPromptUnderstandingResult understanding,
            List<VisionSemanticRouteDescriptor> allowedRoutes
    ) {
        if (understanding == null) {
            return null;
        }

        List<VisionSemanticRouteDescriptor> routes = allowedRoutes == null ? List.of() : allowedRoutes;
        VisionSemanticRouteDescriptor selectedRoute = selectRoute(understanding.semanticPlanOrEmpty(), routes);
        if (selectedRoute == null) {
            understanding.setSemanticPlan(VisionSemanticPlan.empty());
            understanding.setFocusSlotId(null);
            understanding.setFocusSlotConfidence(null);
            understanding.setSlots(new VisionPromptUnderstandingSlots());
            return understanding;
        }

        understanding.setSemanticPlan(sanitizePlan(understanding.semanticPlanOrEmpty(), selectedRoute));
        sanitizeFocusSlot(understanding, selectedRoute);
        sanitizeSlots(understanding, selectedRoute);
        return understanding;
    }

    private VisionSemanticRouteDescriptor selectRoute(
            VisionSemanticPlan semanticPlan,
            List<VisionSemanticRouteDescriptor> allowedRoutes
    ) {
        VisionIntent candidateIntent = semanticPlan == null
                ? VisionIntent.UNSUPPORTED
                : semanticPlan.candidateIntentOrUnsupported();
        String capabilityId = semanticPlan == null ? null : normalizeBlank(semanticPlan.getCapabilityId());
        if (candidateIntent == VisionIntent.UNSUPPORTED || capabilityId == null) {
            return null;
        }

        return allowedRoutes.stream()
                .filter(Objects::nonNull)
                .filter(route -> candidateIntent.name().equalsIgnoreCase(normalizeBlank(route.getIntent())))
                .filter(route -> capabilityId.equalsIgnoreCase(normalizeBlank(route.getCapabilityId())))
                .findFirst()
                .orElse(null);
    }

    private VisionSemanticPlan sanitizePlan(VisionSemanticPlan semanticPlan, VisionSemanticRouteDescriptor route) {
        Set<String> allowedSlotIds = route.getSlots() == null
                ? Set.of()
                : route.getSlots().stream()
                .map(VisionSemanticSlotDescriptor::getSlotId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        VisionSemanticPlan sanitized = VisionSemanticPlan.builder()
                .candidateIntent(route.getIntent())
                .candidateIntentConfidence(normalizeConfidence(semanticPlan.getCandidateIntentConfidence()))
                .capabilityId(route.getCapabilityId())
                .planningNote(trimToNull(semanticPlan.getPlanningNote()))
                .build();
        if (allowedSlotIds.contains("search_query")) {
            sanitized.setSearchQuery(trimToNull(semanticPlan.getSearchQuery()));
        }
        if (allowedSlotIds.contains("target_user")) {
            sanitized.setTargetUserQuery(trimToNull(semanticPlan.getTargetUserQuery()));
        }
        return sanitized;
    }

    private void sanitizeFocusSlot(VisionPromptUnderstandingResult understanding, VisionSemanticRouteDescriptor route) {
        Set<String> allowedSlotIds = route.getSlots() == null
                ? Set.of()
                : route.getSlots().stream()
                .map(VisionSemanticSlotDescriptor::getSlotId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        String focusSlotId = trimToNull(understanding.getFocusSlotId());
        if (focusSlotId == null || !allowedSlotIds.contains(focusSlotId)) {
            understanding.setFocusSlotId(null);
            understanding.setFocusSlotConfidence(null);
            return;
        }

        understanding.setFocusSlotId(focusSlotId);
        understanding.setFocusSlotConfidence(normalizeConfidence(understanding.getFocusSlotConfidence()));
    }

    private void sanitizeSlots(VisionPromptUnderstandingResult understanding, VisionSemanticRouteDescriptor route) {
        VisionPromptUnderstandingSlots source = understanding.getSlots() == null
                ? new VisionPromptUnderstandingSlots()
                : understanding.getSlots();
        Map<String, VisionSemanticSlotDescriptor> schema = route.getSlots() == null
                ? Map.of()
                : route.getSlots().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        VisionSemanticSlotDescriptor::getSlotId,
                        descriptor -> descriptor,
                        (left, right) -> left
                ));

        VisionPromptUnderstandingSlots sanitized = new VisionPromptUnderstandingSlots();
        copyTextSlot(schema.get("circle_name"), source.getCircleName(), source.getCircleNameConfidence(),
                sanitized::setCircleName, sanitized::setCircleNameConfidence);
        copyTextSlot(schema.get("target_circle_query"), source.getTargetCircleQuery(), source.getTargetCircleQueryConfidence(),
                sanitized::setTargetCircleQuery, sanitized::setTargetCircleQueryConfidence);
        copyTextSlot(schema.get("target_quest_query"), source.getApplicationQuestQuery(), source.getApplicationQuestQueryConfidence(),
                sanitized::setApplicationQuestQuery, sanitized::setApplicationQuestQueryConfidence);
        copyTextSlot(schema.get("application_message"), source.getApplicationMessage(), source.getApplicationMessageConfidence(),
                sanitized::setApplicationMessage, sanitized::setApplicationMessageConfidence);
        copyTextSlot(schema.get("application_proposed_price"), source.getApplicationProposedPrice(), source.getApplicationProposedPriceConfidence(),
                sanitized::setApplicationProposedPrice, sanitized::setApplicationProposedPriceConfidence);
        copyTextSlot(schema.get("profile_username"), source.getProfileUsername(), source.getProfileUsernameConfidence(),
                sanitized::setProfileUsername, sanitized::setProfileUsernameConfidence);
        copyTextSlot(schema.get("profile_description"), source.getProfileDescription(), source.getProfileDescriptionConfidence(),
                sanitized::setProfileDescription, sanitized::setProfileDescriptionConfidence);
        copyEnumSlot(schema.get("profile_location_mode"), source.getProfileLocationMode(), source.getProfileLocationModeConfidence(),
                sanitized::setProfileLocationMode, sanitized::setProfileLocationModeConfidence);
        copyLocationSlot(schema.get("profile_location_label"), source.getProfileLocationLabel(), source.getProfileLocationLabelConfidence(),
                sanitized::setProfileLocationLabel, sanitized::setProfileLocationLabelConfidence);
        copyTextSlot(schema.get("quest_title"), source.getQuestTitle(), source.getQuestTitleConfidence(),
                sanitized::setQuestTitle, sanitized::setQuestTitleConfidence);
        copyTextSlot(schema.get("quest_description"), source.getQuestDescription(), source.getQuestDescriptionConfidence(),
                sanitized::setQuestDescription, sanitized::setQuestDescriptionConfidence);
        copyEnumSlot(schema.get("visibility"), source.getVisibility(), source.getVisibilityConfidence(),
                sanitized::setVisibility, sanitized::setVisibilityConfidence);

        VisionPromptUnderstandingRewardSlots reward = new VisionPromptUnderstandingRewardSlots();
        if (schema.containsKey("reward_amount")) {
            reward.setFreeQuest(source.getReward() == null ? null : source.getReward().getFreeQuest());
            reward.setFreeQuestConfidence(normalizeConfidence(source.getReward() == null ? null : source.getReward().getFreeQuestConfidence()));
            reward.setAmount(trimToNull(source.getReward() == null ? null : source.getReward().getAmount()));
            reward.setAmountConfidence(normalizeConfidence(source.getReward() == null ? null : source.getReward().getAmountConfidence()));
        }
        sanitized.setReward(reward);

        VisionPromptUnderstandingScheduleSlots schedule = new VisionPromptUnderstandingScheduleSlots();
        copyEnumSlot(schema.get("schedule_mode"), source.getSchedule() == null ? null : source.getSchedule().getMode(),
                source.getSchedule() == null ? null : source.getSchedule().getModeConfidence(),
                schedule::setMode, schedule::setModeConfidence);
        copyTextSlot(schema.get("scheduled_date"), source.getSchedule() == null ? null : source.getSchedule().getScheduledDate(),
                source.getSchedule() == null ? null : source.getSchedule().getScheduledDateConfidence(),
                schedule::setScheduledDate, schedule::setScheduledDateConfidence);
        copyTextSlot(schema.get("scheduled_time"), source.getSchedule() == null ? null : source.getSchedule().getScheduledTime(),
                source.getSchedule() == null ? null : source.getSchedule().getScheduledTimeConfidence(),
                schedule::setScheduledTime, schedule::setScheduledTimeConfidence);
        sanitized.setSchedule(schedule);

        VisionPromptUnderstandingLocationSlots location = new VisionPromptUnderstandingLocationSlots();
        copyEnumSlot(schema.get("location_mode"), source.getLocation() == null ? null : source.getLocation().getMode(),
                source.getLocation() == null ? null : source.getLocation().getModeConfidence(),
                location::setMode, location::setModeConfidence);
        copyLocationSlot(schema.get("location_label"), source.getLocation() == null ? null : source.getLocation().getLabel(),
                source.getLocation() == null ? null : source.getLocation().getLabelConfidence(),
                location::setLabel, location::setLabelConfidence);
        copyEnumSlot(schema.get("location_candidate_confirmation"),
                source.getLocation() == null ? null : source.getLocation().getCandidateConfirmation(),
                source.getLocation() == null ? null : source.getLocation().getCandidateConfirmationConfidence(),
                location::setCandidateConfirmation, location::setCandidateConfirmationConfidence);
        sanitized.setLocation(location);

        understanding.setSlots(sanitized);
    }

    private void copyTextSlot(
            VisionSemanticSlotDescriptor descriptor,
            String value,
            Double confidence,
            Consumer<String> valueConsumer,
            Consumer<Double> confidenceConsumer
    ) {
        if (descriptor == null) {
            return;
        }
        String normalized = trimToNull(value);
        if (normalized == null) {
            return;
        }
        valueConsumer.accept(normalized);
        confidenceConsumer.accept(normalizeConfidence(confidence));
    }

    private void copyLocationSlot(
            VisionSemanticSlotDescriptor descriptor,
            String value,
            Double confidence,
            Consumer<String> valueConsumer,
            Consumer<Double> confidenceConsumer
    ) {
        if (descriptor == null) {
            return;
        }
        String normalized = trimToNull(value);
        if (normalized == null || !looksLikeLocationValue(normalized)) {
            return;
        }
        valueConsumer.accept(normalized);
        confidenceConsumer.accept(normalizeConfidence(confidence));
    }

    private void copyEnumSlot(
            VisionSemanticSlotDescriptor descriptor,
            String value,
            Double confidence,
            Consumer<String> valueConsumer,
            Consumer<Double> confidenceConsumer
    ) {
        if (descriptor == null) {
            return;
        }
        String normalized = trimToNull(value);
        if (normalized == null) {
            return;
        }
        Set<String> allowedValues = descriptor.getAllowedValues() == null
                ? Set.of()
                : descriptor.getAllowedValues().stream()
                .map(this::normalizeBlank)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!allowedValues.isEmpty() && !allowedValues.contains(normalized)) {
            return;
        }
        valueConsumer.accept(normalized);
        confidenceConsumer.accept(normalizeConfidence(confidence));
    }

    private Double normalizeConfidence(Double confidence) {
        if (confidence == null) {
            return null;
        }
        if (confidence < 0.0d) {
            return 0.0d;
        }
        if (confidence > 1.0d) {
            return 1.0d;
        }
        return confidence;
    }

    private String trimToNull(String value) {
        String normalized = normalizeBlank(value);
        return normalized == null ? null : normalized.trim();
    }

    private boolean looksLikeLocationValue(String value) {
        String normalized = value.trim();
        if (normalized.length() > 120) {
            return false;
        }

        String lower = normalized.toLowerCase();
        if (containsAny(lower, "create a quest", "create quest", "reward", "schedule", "tomorrow", "today", "tonight",
                "application", "profile", "circle", "chat", "quest", "move my sofa", "help me", "looking for")) {
            return false;
        }

        if (normalized.matches(".*\\d.*")) {
            return true;
        }
        if (normalized.contains(",")) {
            return true;
        }
        if (containsAny(lower, "street", "st.", "road", "rd.", "avenue", "ave.", "square", "plaza", "place", "ulica", "trg", "ul.") ) {
            return true;
        }

        String[] words = normalized.split("\\s+");
        return words.length <= 4 && normalized.length() <= 48;
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
