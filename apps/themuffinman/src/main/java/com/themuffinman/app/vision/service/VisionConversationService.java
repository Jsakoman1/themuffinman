package com.themuffinman.app.vision.service;

import com.themuffinman.app.agent.dto.AdminAgentPlaygroundResponseDTO;
import com.themuffinman.app.agent.service.AdminAgentPlaygroundService;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionConversationTurnRequestDTO;
import com.themuffinman.app.vision.dto.VisionConversationListResponseDTO;
import com.themuffinman.app.vision.dto.VisionConversationSummaryDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.model.VisionAgentState;
import com.themuffinman.app.vision.model.VisionConversationAction;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionNextAction;
import com.themuffinman.app.vision.model.VisionReviewTarget;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.model.VisionTurnSource;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.vision.repository.VisionTurnRepository;
import com.themuffinman.app.workmarket.model.Quest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class VisionConversationService {

    private final VisionConversationRepository visionConversationRepository;
    private final VisionTurnRepository visionTurnRepository;
    private final VisionIntentRouter visionIntentRouter;
    private final VisionSlotService visionSlotService;
    private final VisionClarificationService visionClarificationService;
    private final VisionCanvasAssembler visionCanvasAssembler;
    private final VisionCreateQuestExecutionAdapter visionCreateQuestExecutionAdapter;
    private final AdminAgentPlaygroundService adminAgentPlaygroundService;
    private final VisionProperties visionProperties;

    public VisionConversationService(
            VisionConversationRepository visionConversationRepository,
            VisionTurnRepository visionTurnRepository,
            VisionIntentRouter visionIntentRouter,
            VisionSlotService visionSlotService,
            VisionClarificationService visionClarificationService,
            VisionCanvasAssembler visionCanvasAssembler,
            VisionCreateQuestExecutionAdapter visionCreateQuestExecutionAdapter,
            AdminAgentPlaygroundService adminAgentPlaygroundService,
            VisionProperties visionProperties
    ) {
        this.visionConversationRepository = visionConversationRepository;
        this.visionTurnRepository = visionTurnRepository;
        this.visionIntentRouter = visionIntentRouter;
        this.visionSlotService = visionSlotService;
        this.visionClarificationService = visionClarificationService;
        this.visionCanvasAssembler = visionCanvasAssembler;
        this.visionCreateQuestExecutionAdapter = visionCreateQuestExecutionAdapter;
        this.adminAgentPlaygroundService = adminAgentPlaygroundService;
        this.visionProperties = visionProperties;
    }

    @Transactional
    public VisionConversationTurnResponseDTO processTurn(VisionConversationTurnRequestDTO dto, AppUser currentUser) {
        validateAccess(currentUser);
        if (!visionProperties.isEnabled()) {
            throw ServiceErrors.forbidden("Vision conversations are disabled");
        }

        VisionConversationAction action = VisionConversationAction.from(dto == null ? null : dto.getAction());
        String prompt = action == VisionConversationAction.SUBMIT_PROMPT ? requirePrompt(dto) : "";
        AdminAgentPlaygroundResponseDTO analysis = action == VisionConversationAction.SUBMIT_PROMPT
                ? adminAgentPlaygroundService.analyzePrompt(prompt)
                : emptyAnalysis();
        String normalizedPrompt = action == VisionConversationAction.SUBMIT_PROMPT
                ? normalizePrompt(prompt, analysis)
                : "";

        VisionConversation conversation = loadOrCreateConversation(dto == null ? null : dto.getConversationId(), currentUser, normalizedPrompt, action);
        ensureTurnCapacity(conversation);

        VisionTurn turn = switch (action) {
            case CONFIRM_REVIEW -> handleConfirmReviewAction(conversation, analysis, dto == null ? null : dto.getSource());
            case REQUEST_REVIEW_EDIT -> handleReviewEditAction(conversation, analysis, dto == null ? null : dto.getSource(), dto);
            case SUBMIT_PROMPT -> switch (conversation.getIntent()) {
                case CREATE_QUEST -> handleCreateQuestTurn(conversation, prompt, normalizedPrompt, analysis, dto.getSource());
                case UNSUPPORTED -> handleUnsupportedTurn(conversation, prompt, normalizedPrompt, analysis, dto.getSource());
            };
        };

        return visionCanvasAssembler.assemble(conversation, turn, recentConversationSummaries(currentUser));
    }

    @Transactional
    public VisionConversationTurnResponseDTO resetConversation(Long conversationId, AppUser currentUser) {
        validateAccess(currentUser);
        VisionConversation conversation = loadExistingConversation(conversationId, currentUser);
        ensureTurnCapacity(conversation);
        VisionTurn turn = handleResetConversationAction(conversation, "system");
        return visionCanvasAssembler.assemble(conversation, turn, recentConversationSummaries(currentUser));
    }

    @Transactional
    public VisionConversationTurnResponseDTO cancelConversation(Long conversationId, AppUser currentUser) {
        validateAccess(currentUser);
        VisionConversation conversation = loadExistingConversation(conversationId, currentUser);
        ensureTurnCapacity(conversation);
        VisionTurn turn = handleCancelConversationAction(conversation, "system");
        return visionCanvasAssembler.assemble(conversation, turn, recentConversationSummaries(currentUser));
    }

    @Transactional(readOnly = true)
    public VisionConversationTurnResponseDTO loadConversation(Long conversationId, AppUser currentUser) {
        validateAccess(currentUser);
        VisionConversation conversation = loadExistingConversation(conversationId, currentUser);
        VisionTurn turn = visionTurnRepository.findTopByConversationOrderByTurnIndexDesc(conversation)
                .orElseGet(() -> snapshotTurn(conversation));
        return visionCanvasAssembler.assemble(conversation, turn, recentConversationSummaries(currentUser));
    }

    @Transactional(readOnly = true)
    public VisionConversationListResponseDTO listRecentConversations(AppUser currentUser) {
        validateAccess(currentUser);
        return VisionConversationListResponseDTO.builder()
                .items(recentConversationSummaries(currentUser))
                .build();
    }

    private VisionConversation loadOrCreateConversation(
            Long conversationId,
            AppUser currentUser,
            String normalizedPrompt,
            VisionConversationAction action
    ) {
        if (conversationId != null) {
            return loadExistingConversation(conversationId, currentUser);
        }
        if (action != VisionConversationAction.SUBMIT_PROMPT) {
            throw ServiceErrors.badRequest("Conversation action requires an existing conversation");
        }

        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(currentUser);
        conversation.setIntent(visionIntentRouter.detectIntent(normalizedPrompt));
        conversation.setStatus(conversation.getIntent() == VisionIntent.UNSUPPORTED
                ? VisionConversationStatus.BLOCKED
                : VisionConversationStatus.ACTIVE);
        conversation.setSlotData(new LinkedHashMap<>());
        return visionConversationRepository.save(conversation);
    }

    private VisionConversation loadExistingConversation(Long conversationId, AppUser currentUser) {
        return visionConversationRepository.findByIdAndOwner(conversationId, currentUser)
                .orElseThrow(() -> ServiceErrors.notFound("Vision conversation was not found"));
    }

    private List<VisionConversationSummaryDTO> recentConversationSummaries(AppUser currentUser) {
        return visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(currentUser).stream()
                .map(this::toConversationSummary)
                .toList();
    }

    private VisionConversationSummaryDTO toConversationSummary(VisionConversation conversation) {
        String title = conversation.getSlotData().get("quest_title");
        if (title == null || title.isBlank()) {
            title = conversation.getIntent().name();
        }
        String subtitle = conversation.getLastAssistantMessage();
        if (subtitle == null || subtitle.isBlank()) {
            subtitle = conversation.getRequestedSlot() == null
                    ? "No pending prompt"
                    : "Waiting for " + conversation.getRequestedSlot();
        }
        boolean completed = conversation.getStatus() == VisionConversationStatus.COMPLETED;
        boolean resumable = conversation.getStatus() != VisionConversationStatus.COMPLETED;
        boolean stale = isStale(conversation);
        return VisionConversationSummaryDTO.builder()
                .conversationId(conversation.getId())
                .intent(conversation.getIntent().name())
                .status(conversation.getStatus().name())
                .title(title)
                .subtitle(subtitle)
                .stageLabel(stageLabel(conversation))
                .progressLabel(progressLabel(conversation))
                .groupKey(groupKey(conversation))
                .requestedSlot(conversation.getRequestedSlot())
                .resumable(resumable)
                .completed(completed)
                .stale(stale)
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private String stageLabel(VisionConversation conversation) {
        return switch (conversation.getStatus()) {
            case ACTIVE -> conversation.getRequestedSlot() == null ? "In progress" : "Needs input";
            case REVIEW_READY -> "Review ready";
            case COMPLETED -> "Complete";
            case BLOCKED -> "Blocked";
        };
    }

    private String progressLabel(VisionConversation conversation) {
        return switch (conversation.getStatus()) {
            case ACTIVE -> conversation.getRequestedSlot() == null
                    ? "Conversation is active."
                    : "Next step: " + visionClarificationService.buildQuestion(conversation.getRequestedSlot());
            case REVIEW_READY -> "Ready for review and confirmation.";
            case COMPLETED -> "Task finished.";
            case BLOCKED -> "Conversation stopped until the user starts a supported task.";
        };
    }

    private String groupKey(VisionConversation conversation) {
        return switch (conversation.getStatus()) {
            case ACTIVE -> "active";
            case REVIEW_READY -> "review_ready";
            case BLOCKED -> "blocked";
            case COMPLETED -> "completed";
        };
    }

    private boolean isStale(VisionConversation conversation) {
        Instant updatedAt = conversation.getUpdatedAt();
        if (updatedAt == null) {
            return false;
        }
        return Duration.between(updatedAt, Instant.now()).toHours() >= 24;
    }

    private VisionTurn snapshotTurn(VisionConversation conversation) {
        VisionTurn turn = new VisionTurn();
        turn.setId(0L);
        turn.setConversation(conversation);
        turn.setTurnIndex(0);
        turn.setSource(VisionTurnSource.TEXT);
        turn.setPrompt(conversation.getLastUserPrompt() == null ? "" : conversation.getLastUserPrompt());
        turn.setNormalizedPrompt(conversation.getLastNormalizedPrompt() == null ? "" : conversation.getLastNormalizedPrompt());
        turn.setDetectedIntent(conversation.getIntent());
        turn.setAgentState(switch (conversation.getStatus()) {
            case ACTIVE -> VisionAgentState.ASKING;
            case REVIEW_READY -> VisionAgentState.REVIEW_READY;
            case COMPLETED -> VisionAgentState.COMPLETE;
            case BLOCKED -> VisionAgentState.BLOCKED;
        });
        turn.setNextAction(switch (conversation.getStatus()) {
            case ACTIVE -> VisionNextAction.ASK_FOR_SLOT;
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

    private VisionTurn handleConfirmReviewAction(
            VisionConversation conversation,
            AdminAgentPlaygroundResponseDTO analysis,
            String source
    ) {
        if (conversation.getIntent() != VisionIntent.CREATE_QUEST) {
            throw ServiceErrors.conflict("Review confirmation is only supported for create quest conversations");
        }
        return handleCreateQuestReviewTurnInternal(conversation, "", "", analysis, source, VisionConversationAction.CONFIRM_REVIEW);
    }

    private VisionTurn handleReviewEditAction(
            VisionConversation conversation,
            AdminAgentPlaygroundResponseDTO analysis,
            String source,
            VisionConversationTurnRequestDTO dto
    ) {
        if (conversation.getIntent() != VisionIntent.CREATE_QUEST) {
            throw ServiceErrors.conflict("Typed review editing is only supported for create quest conversations");
        }
        VisionReviewTarget reviewTarget = VisionReviewTarget.from(dto == null ? null : dto.getReviewTarget());
        return handleCreateQuestReviewEditAction(conversation, analysis, source, reviewTarget);
    }

    private VisionTurn handleResetConversationAction(VisionConversation conversation, String source) {
        conversation.setIntent(VisionIntent.CREATE_QUEST);
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        conversation.setRequestedSlot("quest_title");
        conversation.setSlotData(new LinkedHashMap<>());
        String message = "The current vision task was reset. What should the new quest be called?";
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(
                conversation,
                source,
                "",
                "",
                VisionIntent.CREATE_QUEST,
                VisionAgentState.ASKING,
                VisionNextAction.ASK_FOR_SLOT,
                "quest_title",
                false,
                true,
                message
        );
    }

    private VisionTurn handleCancelConversationAction(VisionConversation conversation, String source) {
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.COMPLETED);
        conversation.getSlotData().put("conversation_outcome", "cancelled");
        String message = "The current vision task was cancelled. Start a new task when you want to continue.";
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(
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

    private VisionTurn handleCreateQuestTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            AdminAgentPlaygroundResponseDTO analysis,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleCreateQuestReviewTurn(conversation, prompt, normalizedPrompt, analysis, source);
        }

        Map<String, String> mergedSlots = visionSlotService.mergeCreateQuestSlots(conversation, normalizedPrompt);
        String missingSlot = visionClarificationService.nextMissingCreateQuestSlot(mergedSlots);

        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;
        if (missingSlot == null) {
            status = VisionConversationStatus.REVIEW_READY;
            nextAction = VisionNextAction.SHOW_REVIEW;
            agentState = VisionAgentState.REVIEW_READY;
            message = visionProperties.isExecutionEnabled()
                    ? "I have enough to prepare a quest review. Confirm when you want me to create it."
                    : "I have enough for a quest review. Execution is still disabled, so this phase stops at review.";
        } else {
            status = VisionConversationStatus.ACTIVE;
            nextAction = VisionNextAction.ASK_FOR_SLOT;
            agentState = VisionAgentState.ASKING;
            message = missingSlot.equals(conversation.getRequestedSlot())
                    ? visionClarificationService.buildRetryQuestion(missingSlot)
                    : visionClarificationService.buildQuestion(missingSlot);
        }

        conversation.setSlotData(mergedSlots);
        conversation.setRequestedSlot(missingSlot);
        conversation.setStatus(status);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, analysis.isPromptTranslationReliable());
        visionConversationRepository.save(conversation);

        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setTurnIndex((int) visionTurnRepository.countByConversation(conversation) + 1);
        turn.setSource(VisionTurnSource.from(source));
        turn.setPrompt(prompt);
        turn.setNormalizedPrompt(normalizedPrompt);
        turn.setDetectedIntent(VisionIntent.CREATE_QUEST);
        turn.setAgentState(agentState);
        turn.setNextAction(nextAction);
        turn.setRequestedSlot(missingSlot);
        turn.setTranslationApplied(analysis.isPromptTranslationApplied());
        turn.setTranslationReliable(analysis.isPromptTranslationReliable());
        turn.setAssistantMessage(message);
        return visionTurnRepository.save(turn);
    }

    private VisionTurn handleCreateQuestReviewTurnInternal(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            AdminAgentPlaygroundResponseDTO analysis,
            String source,
            VisionConversationAction action
    ) {
        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;

        if (action == VisionConversationAction.CONFIRM_REVIEW || isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                status = VisionConversationStatus.REVIEW_READY;
                nextAction = VisionNextAction.SHOW_REVIEW;
                agentState = VisionAgentState.REVIEW_READY;
                message = "Execution is still disabled. The quest review stays ready until that flag is enabled.";
            } else {
                Quest createdQuest = visionCreateQuestExecutionAdapter.execute(conversation.getSlotData(), conversation.getOwner());
                conversation.getSlotData().put("created_quest_id", createdQuest.getId().toString());
                status = VisionConversationStatus.COMPLETED;
                nextAction = VisionNextAction.COMPLETE;
                agentState = VisionAgentState.COMPLETE;
                message = "Quest created successfully. The first real `/vision` executor completed this task.";
            }
        } else {
            status = VisionConversationStatus.REVIEW_READY;
            nextAction = VisionNextAction.SHOW_REVIEW;
            agentState = VisionAgentState.REVIEW_READY;
            message = visionProperties.isExecutionEnabled()
                    ? "The review is ready. Confirm to create the quest, or use the explicit review controls to revise one field."
                    : "The review is ready, but execution is still disabled. Use the explicit review controls if you want to revise one field.";
        }

        if (nextAction != VisionNextAction.ASK_FOR_SLOT) {
            conversation.setRequestedSlot(null);
        }
        conversation.setStatus(status);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, analysis.isPromptTranslationReliable());
        visionConversationRepository.save(conversation);

        return createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                VisionIntent.CREATE_QUEST,
                agentState,
                nextAction,
                conversation.getRequestedSlot(),
                analysis.isPromptTranslationApplied(),
                analysis.isPromptTranslationReliable(),
                message
        );
    }

    private VisionTurn handleCreateQuestReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            AdminAgentPlaygroundResponseDTO analysis,
            String source
    ) {
        return handleCreateQuestReviewTurnInternal(conversation, prompt, normalizedPrompt, analysis, source, VisionConversationAction.SUBMIT_PROMPT);
    }

    private VisionTurn handleCreateQuestReviewEditAction(
            VisionConversation conversation,
            AdminAgentPlaygroundResponseDTO analysis,
            String source,
            VisionReviewTarget reviewTarget
    ) {
        if (conversation.getStatus() != VisionConversationStatus.REVIEW_READY) {
            throw ServiceErrors.conflict("Review editing requires a review-ready vision conversation");
        }

        if (reviewTarget == null) {
            throw ServiceErrors.badRequest("Review target is required for typed review editing");
        }

        String editSlot = reviewTarget.getSlotId();
        visionSlotService.prepareForReviewEdit(conversation.getSlotData(), reviewTarget);
        conversation.setRequestedSlot(editSlot);
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        String message = visionClarificationService.buildQuestion(editSlot);
        updateConversationMetadata(conversation, "", "", message, analysis.isPromptTranslationReliable());
        visionConversationRepository.save(conversation);

        return createTurn(
                conversation,
                source,
                "",
                "",
                VisionIntent.CREATE_QUEST,
                VisionAgentState.ASKING,
                VisionNextAction.ASK_FOR_SLOT,
                editSlot,
                false,
                analysis.isPromptTranslationReliable(),
                message
        );
    }

    private VisionTurn handleUnsupportedTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            AdminAgentPlaygroundResponseDTO analysis,
            String source
    ) {
        String message = "This Phase 1 vision backend only supports stepwise create_quest planning for now.";
        conversation.setStatus(VisionConversationStatus.BLOCKED);
        conversation.setRequestedSlot(null);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, analysis.isPromptTranslationReliable());
        visionConversationRepository.save(conversation);

        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setTurnIndex((int) visionTurnRepository.countByConversation(conversation) + 1);
        turn.setSource(VisionTurnSource.from(source));
        turn.setPrompt(prompt);
        turn.setNormalizedPrompt(normalizedPrompt);
        turn.setDetectedIntent(VisionIntent.UNSUPPORTED);
        turn.setAgentState(VisionAgentState.BLOCKED);
        turn.setNextAction(VisionNextAction.BLOCKED);
        turn.setRequestedSlot(null);
        turn.setTranslationApplied(analysis.isPromptTranslationApplied());
        turn.setTranslationReliable(analysis.isPromptTranslationReliable());
        turn.setAssistantMessage(message);
        return visionTurnRepository.save(turn);
    }

    private VisionTurn createTurn(
            VisionConversation conversation,
            String source,
            String prompt,
            String normalizedPrompt,
            VisionIntent intent,
            VisionAgentState agentState,
            VisionNextAction nextAction,
            String requestedSlot,
            boolean translationApplied,
            boolean translationReliable,
            String assistantMessage
    ) {
        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setTurnIndex((int) visionTurnRepository.countByConversation(conversation) + 1);
        turn.setSource(VisionTurnSource.from(source));
        turn.setPrompt(prompt);
        turn.setNormalizedPrompt(normalizedPrompt);
        turn.setDetectedIntent(intent);
        turn.setAgentState(agentState);
        turn.setNextAction(nextAction);
        turn.setRequestedSlot(requestedSlot);
        turn.setTranslationApplied(translationApplied);
        turn.setTranslationReliable(translationReliable);
        turn.setAssistantMessage(assistantMessage);
        return visionTurnRepository.save(turn);
    }

    private void updateConversationMetadata(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            String assistantMessage,
            boolean translationReliable
    ) {
        Instant now = Instant.now();
        conversation.setLastUserPrompt(prompt);
        conversation.setLastNormalizedPrompt(normalizedPrompt);
        conversation.setLastAssistantMessage(assistantMessage);
        conversation.setLastTranslationReliable(translationReliable);
        conversation.setUpdatedAt(now);
        conversation.setLastTurnAt(now);
    }

    private void ensureTurnCapacity(VisionConversation conversation) {
        long turnCount = visionTurnRepository.countByConversation(conversation);
        if (turnCount >= visionProperties.getMaxTurnsPerConversation()) {
            throw ServiceErrors.conflict("Vision conversation reached the maximum number of turns");
        }
    }

    private String normalizePrompt(String prompt, AdminAgentPlaygroundResponseDTO analysis) {
        if (analysis.getTranslatedPrompt() != null && !analysis.getTranslatedPrompt().isBlank()) {
            return analysis.getTranslatedPrompt().trim();
        }
        return prompt;
    }

    private String requirePrompt(VisionConversationTurnRequestDTO dto) {
        if (dto == null || dto.getPrompt() == null || dto.getPrompt().isBlank()) {
            throw ServiceErrors.badRequest("Prompt is required");
        }
        String prompt = dto.getPrompt().trim();
        if (prompt.length() > visionProperties.getMaxPromptLength()) {
            throw ServiceErrors.badRequest("Prompt is too long");
        }
        return prompt;
    }

    private AdminAgentPlaygroundResponseDTO emptyAnalysis() {
        return AdminAgentPlaygroundResponseDTO.builder()
                .translatedPrompt(null)
                .promptTranslationApplied(false)
                .promptTranslationReliable(true)
                .build();
    }

    private void validateAccess(AppUser currentUser) {
        if (currentUser == null) {
            throw ServiceErrors.forbidden("Authentication is required");
        }
    }

    private boolean isConfirmationPrompt(String prompt) {
        String lower = prompt.toLowerCase(Locale.ROOT).trim();
        return lower.equals("confirm")
                || lower.equals("yes")
                || lower.equals("yes confirm")
                || lower.equals("go ahead")
                || lower.equals("create it")
                || lower.equals("create the quest")
                || lower.equals("submit");
    }
}
