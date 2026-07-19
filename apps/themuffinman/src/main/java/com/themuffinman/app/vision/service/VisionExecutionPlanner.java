package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.dto.VisionExecutionCandidateDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VisionExecutionPlanner {

    private final VisionClarificationService visionClarificationService;
    private final VisionSemanticRouteCatalogService visionSemanticRouteCatalogService;
    private final VisionProperties visionProperties;

    public VisionExecutionPlanner(
            VisionClarificationService visionClarificationService,
            VisionSemanticRouteCatalogService visionSemanticRouteCatalogService,
            VisionProperties visionProperties
    ) {
        this.visionClarificationService = visionClarificationService;
        this.visionSemanticRouteCatalogService = visionSemanticRouteCatalogService;
        this.visionProperties = visionProperties;
    }

    public VisionExecutionCandidateDTO plan(VisionConversation conversation) {
        return plan(conversation, null);
    }

    public VisionExecutionCandidateDTO plan(VisionConversation conversation, VisionPromptUnderstandingResult understanding) {
        VisionSemanticPlan semanticPlan = understanding == null ? VisionSemanticPlan.empty() : understanding.semanticPlanOrEmpty();
        VisionIntent candidateIntent = conversation == null
                ? semanticPlan.candidateIntentOrUnsupported()
                : conversation.getIntent();
        if (conversation == null) {
            return null;
        }
        VisionSemanticRouteDescriptor routeDescriptor = visionSemanticRouteCatalogService.routeForIntent(candidateIntent.name());
        if (routeDescriptor == null || !routeDescriptor.isMutating()) return null;
        String capabilityId = routeDescriptor.getCapabilityId() == null || routeDescriptor.getCapabilityId().isBlank()
                ? TextValueNormalizer.lowerToEmpty(candidateIntent.name())
                : semanticPlan.getCapabilityId();
        if (semanticPlan.getCapabilityId() == null || semanticPlan.getCapabilityId().isBlank() || !routeDescriptor.getCapabilityId().equals(semanticPlan.getCapabilityId())) {
            capabilityId = routeDescriptor.getCapabilityId();
        }
        double confidence = semanticPlan.getCandidateIntentConfidence() == null
                ? 0.0d
                : semanticPlan.getCandidateIntentConfidence();
        String planningNote = semanticPlan.getPlanningNote() == null ? "" : semanticPlan.getPlanningNote();

        if (conversation.getStatus() == VisionConversationStatus.COMPLETED) {
            return VisionExecutionCandidateDTO.builder()
                    .candidateIntent(candidateIntent.name())
                    .capabilityId(capabilityId)
                    .confidence(confidence)
                    .reviewReady(false)
                    .executionReady(false)
                    .confirmationRequired(false)
                .nextRequiredSlot(null)
                .blockingReason("Conversation is already complete.")
                    .failureCode("STATE")
                    .retryable(false)
                    .planningNote(planningNote)
                    .summary(candidateIntent == VisionIntent.CREATE_QUEST
                            ? "Quest has already been executed."
                            : "Ride action has already been executed.")
                    .build();
        }

        if (conversation.getStatus() == VisionConversationStatus.BLOCKED) {
            return VisionExecutionCandidateDTO.builder()
                    .candidateIntent(candidateIntent.name())
                    .capabilityId(capabilityId)
                    .confidence(confidence)
                    .reviewReady(false)
                    .executionReady(false)
                    .confirmationRequired(false)
                    .nextRequiredSlot(null)
                    .blockingReason("Conversation is blocked.")
                    .failureCode("STATE")
                    .retryable(false)
                    .planningNote(planningNote)
                    .summary("Conversation is blocked and cannot proceed.")
                    .build();
        }

        Map<String, String> slotData = conversation.getSlotData();
        String nextRequiredSlot = conversation.getStatus() == VisionConversationStatus.REVIEW_READY ? null : nextRequiredSlot(conversation, understanding, candidateIntent);
        boolean reviewReady = conversation.getStatus() == VisionConversationStatus.REVIEW_READY;
        boolean executionReady = reviewReady && visionProperties.isExecutionEnabled();
        boolean confirmationRequired = reviewReady;
        String blockingReason;
        if (!reviewReady && candidateIntent == VisionIntent.CREATE_QUEST && "quest_title".equals(nextRequiredSlot)
                && isLowConfidenceCreateQuestUnderstanding(understanding)) {
            blockingReason = "Need a clearer quest title or task before review.";
        } else if (!reviewReady) {
            blockingReason = nextRequiredSlot == null
                    ? "Continue collecting the required ride details."
                    : "Missing required field: " + nextRequiredSlot + ".";
        } else if (!visionProperties.isExecutionEnabled()) {
            blockingReason = "Execution is disabled by configuration.";
        } else {
            blockingReason = "";
        }

        return VisionExecutionCandidateDTO.builder()
                .candidateIntent(candidateIntent.name())
                .capabilityId(capabilityId)
                .confidence(confidence)
                .reviewReady(reviewReady)
                .executionReady(executionReady)
                .confirmationRequired(confirmationRequired)
                .nextRequiredSlot(nextRequiredSlot)
                .blockingReason(blockingReason)
                .failureCode(classifyFailure(blockingReason))
                .retryable(isRetryable(blockingReason))
                .planningNote(planningNote)
                .summary(buildSummary(reviewReady, executionReady, nextRequiredSlot, candidateIntent))
                .build();
    }

    private String classifyFailure(String reason) {
        if (reason == null || reason.isBlank()) return "NONE";
        String value = reason.toLowerCase(java.util.Locale.ROOT);
        if (value.contains("disabled") || value.contains("configuration")) return "CONFIGURATION";
        if (value.contains("required") || value.contains("invalid") || value.contains("missing")) return "VALIDATION";
        return "STATE";
    }

    private boolean isRetryable(String reason) {
        return false;
    }

    private String nextRequiredSlot(VisionConversation conversation, VisionPromptUnderstandingResult understanding, VisionIntent intent) {
        if (intent == VisionIntent.CREATE_QUEST && isLowConfidenceCreateQuestUnderstanding(understanding)
                && conversation.getSlotData().containsKey("schedule_mode")
                && conversation.getSlotData().containsKey("location_mode")) return "quest_title";
        if (intent == VisionIntent.CREATE_QUEST) {
            if (!conversation.getSlotData().containsKey("schedule_mode")) return "schedule_mode";
            if (!conversation.getSlotData().containsKey("location_mode")) return "location_mode";
        }
        return null;
    }

    private String buildSummary(boolean reviewReady, boolean executionReady, String nextRequiredSlot, VisionIntent intent) {
        if (reviewReady && executionReady) {
            return intent == VisionIntent.CREATE_QUEST ? "Quest review is ready and can be confirmed." : "Ride review is ready and can be confirmed.";
        }
        if (reviewReady) {
            return "Review is ready, but execution is still disabled.";
        }
        if (nextRequiredSlot != null) {
            return "Collect " + nextRequiredSlot + " next.";
        }
        return "Continue collecting the required details.";
    }

    private boolean isLowConfidenceCreateQuestUnderstanding(VisionPromptUnderstandingResult understanding) {
        if (understanding == null || understanding.semanticPlanOrEmpty() == null) {
            return false;
        }
        if (understanding.semanticPlanOrEmpty().candidateIntentOrUnsupported() != VisionIntent.CREATE_QUEST) {
            return false;
        }
        Double confidence = understanding.semanticPlanOrEmpty().getCandidateIntentConfidence();
        if (confidence == null) {
            return false;
        }
        return confidence < visionSemanticRouteCatalogService.minimumConfidenceForIntent(VisionIntent.CREATE_QUEST);
    }
}
