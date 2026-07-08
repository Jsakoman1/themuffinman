package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionAgentState;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionNextAction;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.model.VisionTurnSource;
import com.themuffinman.app.vision.repository.VisionConversationRepository;

import com.themuffinman.app.common.normalization.TextValueNormalizer;

final class VisionConversationLifecycleSupport {

    private final VisionConversationRepository visionConversationRepository;
    private final VisionSemanticOrchestrationContextService visionSemanticOrchestrationContextService;
    private final VisionConversationMutationSupport visionConversationMutationSupport;

    VisionConversationLifecycleSupport(
            VisionConversationRepository visionConversationRepository,
            VisionSemanticOrchestrationContextService visionSemanticOrchestrationContextService,
            VisionConversationMutationSupport visionConversationMutationSupport
    ) {
        this.visionConversationRepository = visionConversationRepository;
        this.visionSemanticOrchestrationContextService = visionSemanticOrchestrationContextService;
        this.visionConversationMutationSupport = visionConversationMutationSupport;
    }

    boolean isCancelCommand(String prompt) {
        if (prompt == null) {
            return false;
        }

        String normalizedPrompt = TextValueNormalizer.lowerTrimToEmpty(prompt)
                .replaceAll("[.!?]+$", "");
        return normalizedPrompt.equals("cancel")
                || normalizedPrompt.equals("cancel quest")
                || normalizedPrompt.equals("cancel conversation")
                || normalizedPrompt.equals("stop")
                || normalizedPrompt.equals("stop task")
                || normalizedPrompt.equals("odustani")
                || normalizedPrompt.equals("prekini")
                || normalizedPrompt.equals("ponisti")
                || normalizedPrompt.equals("poništi")
                || normalizedPrompt.equals("reset")
                || normalizedPrompt.equals("reset conversation");
    }

    VisionConversation loadExistingConversation(Long conversationId, AppUser currentUser) {
        return visionConversationRepository.findByIdAndOwner(conversationId, currentUser)
                .orElseThrow(() -> ServiceErrors.notFound("Vision conversation was not found"));
    }

    VisionSemanticUserMemoryContext userMemoryFor(VisionConversation conversation) {
        if (visionSemanticOrchestrationContextService == null || conversation == null) {
            return null;
        }
        VisionSemanticMemoryContext memoryContext = visionSemanticOrchestrationContextService.buildMemoryContext(conversation.getOwner(), conversation);
        return memoryContext == null ? null : memoryContext.getUserMemory();
    }

    VisionTurn snapshotTurn(VisionConversation conversation) {
        VisionTurn turn = new VisionTurn();
        turn.setId(0L);
        turn.setConversation(conversation);
        turn.setTurnIndex(0);
        turn.setSource(VisionTurnSource.TEXT);
        turn.setPrompt(conversation.getLastUserPrompt() == null ? "" : conversation.getLastUserPrompt());
        turn.setNormalizedPrompt(conversation.getLastNormalizedPrompt() == null ? "" : conversation.getLastNormalizedPrompt());
        turn.setDetectedIntent(conversation.getIntent());
        turn.setAgentState(switch (conversation.getStatus()) {
            case ACTIVE -> conversation.getIntent() == VisionIntent.DISCOVER_QUESTS
                    ? VisionAgentState.RECOMMENDING
                    : VisionAgentState.ASKING;
            case REVIEW_READY -> VisionAgentState.REVIEW_READY;
            case COMPLETED -> VisionAgentState.COMPLETE;
            case BLOCKED -> VisionAgentState.BLOCKED;
        });
        turn.setNextAction(switch (conversation.getStatus()) {
            case ACTIVE -> conversation.getIntent() == VisionIntent.DISCOVER_QUESTS
                    ? VisionNextAction.SHOW_RESULTS
                    : VisionNextAction.ASK_FOR_SLOT;
            case REVIEW_READY -> VisionNextAction.SHOW_REVIEW;
            case COMPLETED -> VisionNextAction.COMPLETE;
            case BLOCKED -> VisionNextAction.BLOCKED;
        });
        turn.setRequestedSlot(conversation.getRequestedSlot());
        turn.setTranslationApplied(false);
        turn.setTranslationReliable(conversation.isLastTranslationReliable());
        turn.setAssistantMessage(conversation.getLastAssistantMessage() == null ? "Conversation resumed." : conversation.getLastAssistantMessage());
        return turn;
    }

    VisionTurn resetConversationAction(VisionConversation conversation, String source) {
        VisionIntent intent = resetIntent(conversation);
        conversation.setIntent(intent);
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        conversation.setRequestedSlot(requestedSlotForIntent(intent));
        conversation.setSlotData(new java.util.LinkedHashMap<>());
        String message = intent == VisionIntent.DISCOVER_QUESTS
                ? "The current quest discovery was reset. What would you like to browse next?"
                : intent == VisionIntent.SEARCH
                ? "The current search was reset. What would you like to look for next?"
                : intent == VisionIntent.OPEN_CHAT
                ? "The current chat task was reset. Who should I open chat with?"
                : intent == VisionIntent.CREATE_CIRCLE
                ? "The current circle draft was reset. What should the circle be called?"
                : intent == VisionIntent.CREATE_CIRCLE_REQUEST
                ? "The current circle request draft was reset. Who should receive the circle request?"
                : intent == VisionIntent.ACCEPT_CIRCLE_REQUEST
                ? "The current circle request acceptance draft was reset. Whose request should I accept?"
                : intent == VisionIntent.DELETE_CIRCLE_REQUEST
                ? "The current circle request draft was reset. Whose pending request should I decline or cancel?"
                : intent == VisionIntent.UPDATE_CIRCLE
                ? "The current circle rename draft was reset. What circle should I rename?"
                : intent == VisionIntent.DELETE_CIRCLE
                ? "The current circle deletion draft was reset. What circle should I delete?"
                : intent == VisionIntent.CREATE_APPLICATION
                ? "The current application draft was reset. What quest should I apply to?"
                : intent == VisionIntent.UPDATE_APPLICATION
                ? "The current application update draft was reset. What application should I update?"
                : intent == VisionIntent.WITHDRAW_APPLICATION
                ? "The current application withdrawal draft was reset. What application should I withdraw?"
                : intent == VisionIntent.APPROVE_APPLICATION
                ? "The current application approval draft was reset. What quest should I use?"
                : intent == VisionIntent.DECLINE_APPLICATION
                ? "The current application decline draft was reset. What quest should I use?"
                : intent == VisionIntent.UPDATE_PROFILE
                ? "The current profile draft was reset. What username should I use?"
                : intent == VisionIntent.UPDATE_PROFILE_LOCATION
                ? "The current profile location draft was reset. Should I turn your profile location off, keep it approximate, or keep it exact?"
                : intent == VisionIntent.VIEW_CHAT_WORKSPACE
                ? VisionConversationSnapshotSupport.resetReadOnlySnapshotMessage(VisionIntent.VIEW_CHAT_WORKSPACE)
                : intent == VisionIntent.VIEW_PROFILE
                ? VisionConversationSnapshotSupport.resetReadOnlySnapshotMessage(VisionIntent.VIEW_PROFILE)
                : intent == VisionIntent.VIEW_SETTINGS
                ? VisionConversationSnapshotSupport.resetReadOnlySnapshotMessage(VisionIntent.VIEW_SETTINGS)
                : intent == VisionIntent.VIEW_BUSINESS
                ? VisionConversationSnapshotSupport.resetReadOnlySnapshotMessage(VisionIntent.VIEW_BUSINESS)
                : intent == VisionIntent.VIEW_BUSINESS_AVAILABILITY
                ? VisionConversationSnapshotSupport.resetReadOnlySnapshotMessage(VisionIntent.VIEW_BUSINESS_AVAILABILITY)
                : intent == VisionIntent.VIEW_USER_PROFILE
                ? "The current user profile view was reset. What profile should I open?"
                : intent == VisionIntent.VIEW_CIRCLES
                ? VisionConversationSnapshotSupport.resetReadOnlySnapshotMessage(VisionIntent.VIEW_CIRCLES)
                : intent == VisionIntent.VIEW_CIRCLE_DETAIL
                ? "The current circle detail view was reset. What circle should I open?"
                : intent == VisionIntent.VIEW_QUEST_DETAIL
                ? "The current quest detail view was reset. What quest should I open?"
                : intent == VisionIntent.VIEW_NOTIFICATIONS
                ? VisionConversationSnapshotSupport.resetReadOnlySnapshotMessage(VisionIntent.VIEW_NOTIFICATIONS)
                : intent == VisionIntent.VIEW_APPLICATIONS
                ? VisionConversationSnapshotSupport.resetReadOnlySnapshotMessage(VisionIntent.VIEW_APPLICATIONS)
                : intent == VisionIntent.VIEW_APPLICATION_DETAIL
                ? "The current application detail view was reset. What application should I open?"
                : intent == VisionIntent.VIEW_THINGS
                ? VisionConversationSnapshotSupport.resetReadOnlySnapshotMessage(VisionIntent.VIEW_THINGS)
                : "The current vision task was reset. What should the new quest be called?";
        visionConversationMutationSupport.updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        boolean showResultsIntent = intent == VisionIntent.DISCOVER_QUESTS
                || intent == VisionIntent.VIEW_CHAT_WORKSPACE
                || intent == VisionIntent.VIEW_PROFILE
                || intent == VisionIntent.VIEW_SETTINGS
                || intent == VisionIntent.VIEW_BUSINESS
                || intent == VisionIntent.VIEW_BUSINESS_AVAILABILITY
                || intent == VisionIntent.VIEW_CIRCLES
                || intent == VisionIntent.VIEW_NOTIFICATIONS
                || intent == VisionIntent.VIEW_APPLICATIONS;
        boolean needsClarificationIntent = intent == VisionIntent.OPEN_CHAT
                || intent == VisionIntent.CREATE_QUEST
                || intent == VisionIntent.CREATE_CIRCLE
                || intent == VisionIntent.CREATE_CIRCLE_REQUEST
                || intent == VisionIntent.ACCEPT_CIRCLE_REQUEST
                || intent == VisionIntent.DELETE_CIRCLE_REQUEST
                || intent == VisionIntent.UPDATE_CIRCLE
                || intent == VisionIntent.DELETE_CIRCLE
                || intent == VisionIntent.CREATE_APPLICATION
                || intent == VisionIntent.UPDATE_APPLICATION
                || intent == VisionIntent.WITHDRAW_APPLICATION
                || intent == VisionIntent.APPROVE_APPLICATION
                || intent == VisionIntent.DECLINE_APPLICATION
                || intent == VisionIntent.UPDATE_PROFILE
                || intent == VisionIntent.UPDATE_PROFILE_LOCATION
                || intent == VisionIntent.VIEW_USER_PROFILE
                || intent == VisionIntent.VIEW_CIRCLE_DETAIL
                || intent == VisionIntent.VIEW_QUEST_DETAIL
                || intent == VisionIntent.VIEW_APPLICATION_DETAIL;
        return visionConversationMutationSupport.createTurn(
                conversation,
                source,
                "",
                "",
                intent,
                showResultsIntent ? VisionAgentState.RECOMMENDING : VisionAgentState.ASKING,
                showResultsIntent ? VisionNextAction.SHOW_RESULTS : VisionNextAction.ASK_FOR_SLOT,
                showResultsIntent ? null : conversation.getRequestedSlot(),
                false,
                true,
                message
        );
    }

    VisionTurn cancelConversationAction(VisionConversation conversation, String source) {
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.COMPLETED);
        conversation.getSlotData().put("conversation_outcome", "cancelled");
        String message = "The current vision task was cancelled. Start a new task when you want to continue.";
        visionConversationMutationSupport.updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return visionConversationMutationSupport.createTurn(
                conversation,
                source,
                "",
                "",
                conversation.getIntent(),
                VisionAgentState.COMPLETE,
                VisionNextAction.COMPLETE,
                null,
                false,
                true,
                message
        );
    }

    private VisionIntent resetIntent(VisionConversation conversation) {
        if (conversation == null || conversation.getIntent() == null) {
            return VisionIntent.CREATE_QUEST;
        }
        return conversation.getIntent();
    }

    private String requestedSlotForIntent(VisionIntent intent) {
        return switch (intent) {
            case DISCOVER_QUESTS, SEARCH -> null;
            case OPEN_CHAT -> "target_user";
            case CREATE_CIRCLE -> "circle_name";
            case CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST -> "target_user";
            case UPDATE_CIRCLE, DELETE_CIRCLE -> "target_circle_query";
            case CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION -> "target_quest_query";
            case UPDATE_PROFILE -> "profile_username";
            case UPDATE_PROFILE_LOCATION -> "profile_location_mode";
            case VIEW_CHAT_WORKSPACE -> null;
            case VIEW_BUSINESS, VIEW_BUSINESS_AVAILABILITY -> null;
            case VIEW_USER_PROFILE -> "target_user";
            case VIEW_CIRCLES -> null;
            case VIEW_CIRCLE_DETAIL -> "target_circle_query";
            case VIEW_QUEST_DETAIL -> "target_quest_query";
            case VIEW_NOTIFICATIONS -> null;
            case VIEW_APPLICATIONS -> null;
            case VIEW_APPLICATION_DETAIL -> "target_application_query";
            case VIEW_QUEST_NEWS -> null;
            case VIEW_THINGS -> null;
            default -> "quest_title";
        };
    }
}
