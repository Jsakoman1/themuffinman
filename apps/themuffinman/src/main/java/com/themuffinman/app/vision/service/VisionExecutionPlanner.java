package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.dto.VisionExecutionCandidateDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
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
        if (conversation == null || candidateIntent != VisionIntent.CREATE_QUEST) {
            return null;
        }
        String capabilityId = semanticPlan.getCapabilityId() == null || semanticPlan.getCapabilityId().isBlank()
                ? candidateIntent.name().toLowerCase(java.util.Locale.ROOT)
                : semanticPlan.getCapabilityId();
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
                    .planningNote(planningNote)
                    .summary("Quest has already been executed.")
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
                    .planningNote(planningNote)
                    .summary("Conversation is blocked and cannot proceed.")
                    .build();
        }

        Map<String, String> slotData = conversation.getSlotData();
        String nextRequiredSlot = conversation.getStatus() == VisionConversationStatus.REVIEW_READY
                ? null
                : visionClarificationService.nextMissingCreateQuestSlot(slotData);
        boolean lowConfidence = isLowConfidenceCreateQuestUnderstanding(understanding);
        boolean reviewReady = conversation.getStatus() == VisionConversationStatus.REVIEW_READY;
        boolean executionReady = reviewReady && visionProperties.isExecutionEnabled();
        boolean confirmationRequired = reviewReady;
        String blockingReason;
        if (nextRequiredSlot == null && lowConfidence && !reviewReady) {
            nextRequiredSlot = "quest_title";
            blockingReason = "Need a clearer quest title or task before review.";
        } else if (!reviewReady) {
            blockingReason = nextRequiredSlot == null
                    ? "Continue collecting quest details."
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
                .planningNote(planningNote)
                .summary(buildSummary(reviewReady, executionReady, nextRequiredSlot))
                .build();
    }

    private String buildSummary(boolean reviewReady, boolean executionReady, String nextRequiredSlot) {
        if (reviewReady && executionReady) {
            return "Quest review is ready and can be confirmed.";
        }
        if (reviewReady) {
            return "Quest review is ready, but execution is still disabled.";
        }
        if (nextRequiredSlot != null) {
            return "Collect " + nextRequiredSlot + " next.";
        }
        return "Continue collecting quest details.";
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
