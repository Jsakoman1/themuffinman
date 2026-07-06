package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.vision.dto.VisionConversationTurnRequestDTO;
import com.themuffinman.app.vision.model.VisionAgentState;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionConversationAction;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionNextAction;
import com.themuffinman.app.vision.model.VisionReviewTarget;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.repository.VisionConversationRepository;

final class VisionQuestReviewSupport {

    private final VisionExecutionService visionExecutionService;
    private final VisionSurfacePolicy visionSurfacePolicy;
    private final VisionSlotService visionSlotService;
    private final VisionClarificationService visionClarificationService;
    private final VisionSemanticMapper visionSemanticMapper;
    private final VisionConversationRepository visionConversationRepository;
    private final VisionConversationMutationSupport visionConversationMutationSupport;

    VisionQuestReviewSupport(
            VisionExecutionService visionExecutionService,
            VisionSurfacePolicy visionSurfacePolicy,
            VisionSlotService visionSlotService,
            VisionClarificationService visionClarificationService,
            VisionSemanticMapper visionSemanticMapper,
            VisionConversationRepository visionConversationRepository,
            VisionConversationMutationSupport visionConversationMutationSupport
    ) {
        this.visionExecutionService = visionExecutionService;
        this.visionSurfacePolicy = visionSurfacePolicy;
        this.visionSlotService = visionSlotService;
        this.visionClarificationService = visionClarificationService;
        this.visionSemanticMapper = visionSemanticMapper;
        this.visionConversationRepository = visionConversationRepository;
        this.visionConversationMutationSupport = visionConversationMutationSupport;
    }

    VisionTurn handleConfirmReviewAction(
            VisionConversation conversation,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleCreateQuestReviewTurnInternal(conversation, "", "", understanding, source, VisionConversationAction.CONFIRM_REVIEW);
    }

    VisionTurn handleReviewEditAction(
            VisionConversation conversation,
            VisionPromptUnderstandingResult understanding,
            String source,
            VisionConversationTurnRequestDTO dto
    ) {
        if (conversation.getIntent() != VisionIntent.CREATE_QUEST) {
            throw ServiceErrors.conflict("Typed review editing is only supported for create quest conversations");
        }
        VisionReviewTarget reviewTarget = VisionReviewTarget.from(dto == null ? null : dto.getReviewTarget());
        return handleCreateQuestReviewEditAction(conversation, understanding, source, reviewTarget);
    }

    VisionTurn handleCreateQuestReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleCreateQuestReviewTurnInternal(conversation, prompt, normalizedPrompt, understanding, source, VisionConversationAction.SUBMIT_PROMPT);
    }

    private VisionTurn handleCreateQuestReviewTurnInternal(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source,
            VisionConversationAction action
    ) {
        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;

        if (action == VisionConversationAction.CONFIRM_REVIEW || isConfirmationPrompt(normalizedPrompt)) {
            VisionExecutionResult executionResult = visionExecutionService.execute(conversation);
            if (executionResult.isExecuted()) {
                conversation.getSlotData().put("created_quest_id", executionResult.getCreatedQuest().getId().toString());
                status = VisionConversationStatus.COMPLETED;
                nextAction = VisionNextAction.COMPLETE;
                agentState = VisionAgentState.COMPLETE;
                message = "Quest created successfully. The first real `/vision` executor completed this task.";
            } else {
                status = VisionConversationStatus.REVIEW_READY;
                nextAction = VisionNextAction.SHOW_REVIEW;
                agentState = VisionAgentState.REVIEW_READY;
                message = executionResult.getBlockingReason();
            }
        } else {
            status = VisionConversationStatus.REVIEW_READY;
            nextAction = VisionNextAction.SHOW_REVIEW;
            agentState = VisionAgentState.REVIEW_READY;
            message = visionSurfacePolicy.canExecuteCapability("create_quest")
                    ? "The review is ready. Confirm to create the quest, or use the explicit review controls to revise one field."
                    : "The review is ready, but execution is still disabled. Use the explicit review controls if you want to revise one field.";
        }

        if (nextAction != VisionNextAction.ASK_FOR_SLOT) {
            conversation.setRequestedSlot(null);
        }
        conversation.setStatus(status);
        visionConversationMutationSupport.updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);

        return visionConversationMutationSupport.createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                VisionIntent.CREATE_QUEST,
                agentState,
                nextAction,
                conversation.getRequestedSlot(),
                understanding.isTranslationApplied(),
                understanding.isTranslationReliable(),
                message
        );
    }

    private VisionTurn handleCreateQuestReviewEditAction(
            VisionConversation conversation,
            VisionPromptUnderstandingResult understanding,
            String source,
            VisionReviewTarget reviewTarget
    ) {
        if (conversation.getStatus() != VisionConversationStatus.REVIEW_READY) {
            throw ServiceErrors.conflict("Review editing requires a review-ready vision conversation");
        }

        if (reviewTarget == null) {
            throw ServiceErrors.badRequest("Review target is required for typed review editing");
        }

        String editSlot = visionSemanticMapper.reviewTargetSlotId(reviewTarget);
        visionSlotService.prepareForReviewEdit(conversation.getSlotData(), reviewTarget);
        conversation.setRequestedSlot(editSlot);
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        String message = visionClarificationService.buildQuestion(editSlot);
        visionConversationMutationSupport.updateConversationMetadata(conversation, "", "", message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);

        return visionConversationMutationSupport.createTurn(
                conversation,
                source,
                "",
                "",
                VisionIntent.CREATE_QUEST,
                VisionAgentState.ASKING,
                VisionNextAction.ASK_FOR_SLOT,
                editSlot,
                false,
                understanding.isTranslationReliable(),
                message
        );
    }

    private boolean isConfirmationPrompt(String prompt) {
        String lower = prompt.toLowerCase(java.util.Locale.ROOT).trim();
        return lower.equals("confirm")
                || lower.equals("yes")
                || lower.equals("yes confirm")
                || lower.equals("go ahead")
                || lower.equals("create it")
                || lower.equals("create the quest")
                || lower.equals("submit");
    }
}
