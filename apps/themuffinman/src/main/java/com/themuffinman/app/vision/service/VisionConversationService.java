package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionConversationTurnRequestDTO;
import com.themuffinman.app.vision.dto.VisionConversationListResponseDTO;
import com.themuffinman.app.vision.dto.VisionConversationSummaryDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.vision.dto.VisionQuestDiscoveryDTO;
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
    private final VisionExecutionPlanner visionExecutionPlanner;
    private final VisionQuestDiscoveryService visionQuestDiscoveryService;
    private final VisionExecutionService visionExecutionService;
    private final VisionChatExecutionService visionChatExecutionService;
    private final VisionPromptUnderstandingService visionPromptUnderstandingService;
    private final VisionSemanticMapper visionSemanticMapper;
    private final VisionSurfacePolicy visionSurfacePolicy;
    private final VisionProperties visionProperties;

    public VisionConversationService(
            VisionConversationRepository visionConversationRepository,
            VisionTurnRepository visionTurnRepository,
            VisionIntentRouter visionIntentRouter,
            VisionSlotService visionSlotService,
            VisionClarificationService visionClarificationService,
            VisionCanvasAssembler visionCanvasAssembler,
            VisionExecutionPlanner visionExecutionPlanner,
            VisionQuestDiscoveryService visionQuestDiscoveryService,
            VisionExecutionService visionExecutionService,
            VisionChatExecutionService visionChatExecutionService,
            VisionPromptUnderstandingService visionPromptUnderstandingService,
            VisionSemanticMapper visionSemanticMapper,
            VisionSurfacePolicy visionSurfacePolicy,
            VisionProperties visionProperties
    ) {
        this.visionConversationRepository = visionConversationRepository;
        this.visionTurnRepository = visionTurnRepository;
        this.visionIntentRouter = visionIntentRouter;
        this.visionSlotService = visionSlotService;
        this.visionClarificationService = visionClarificationService;
        this.visionCanvasAssembler = visionCanvasAssembler;
        this.visionExecutionPlanner = visionExecutionPlanner;
        this.visionQuestDiscoveryService = visionQuestDiscoveryService;
        this.visionExecutionService = visionExecutionService;
        this.visionChatExecutionService = visionChatExecutionService;
        this.visionPromptUnderstandingService = visionPromptUnderstandingService;
        this.visionSemanticMapper = visionSemanticMapper;
        this.visionSurfacePolicy = visionSurfacePolicy;
        this.visionProperties = visionProperties;
    }

    @Transactional
    public VisionConversationTurnResponseDTO processTurn(VisionConversationTurnRequestDTO dto, AppUser currentUser) {
        validateAccess(currentUser);
        if (!visionProperties.isEnabled()) {
            throw ServiceErrors.forbidden("Vision conversations are disabled");
        }

        VisionConversationAction action = VisionConversationAction.from(dto == null ? null : dto.getAction());
        String rawPrompt = dto == null ? null : dto.getPrompt();
        if (action == VisionConversationAction.SUBMIT_PROMPT && isCancelCommand(rawPrompt)) {
            Long conversationId = dto == null ? null : dto.getConversationId();
            if (conversationId == null) {
                throw ServiceErrors.badRequest("Conversation id is required to cancel an active vision task");
            }
            return cancelConversation(conversationId, currentUser);
        }
        VisionConversation existingConversation = dto != null && dto.getConversationId() != null
                ? loadExistingConversation(dto.getConversationId(), currentUser)
                : null;
        String prompt = action == VisionConversationAction.SUBMIT_PROMPT ? requirePrompt(dto) : "";
        VisionPromptUnderstandingResult understanding = action == VisionConversationAction.SUBMIT_PROMPT
                ? visionPromptUnderstandingService.understandPrompt(prompt, existingConversation)
                : VisionPromptUnderstandingResult.empty("");
        String normalizedPrompt = action == VisionConversationAction.SUBMIT_PROMPT
                ? normalizePrompt(prompt, understanding)
                : "";

        VisionConversation conversation = loadOrCreateConversation(
                dto == null ? null : dto.getConversationId(),
                currentUser,
                normalizedPrompt,
                action,
                understanding,
                existingConversation
        );
        ensureTurnCapacity(conversation);

        VisionTurn turn = switch (action) {
            case CONFIRM_REVIEW -> handleConfirmReviewAction(conversation, understanding, dto == null ? null : dto.getEffectiveSource());
            case REQUEST_REVIEW_EDIT -> handleReviewEditAction(conversation, understanding, dto == null ? null : dto.getEffectiveSource(), dto);
            case SUBMIT_PROMPT -> switch (conversation.getIntent()) {
                case CREATE_QUEST -> handleCreateQuestTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case DISCOVER_QUESTS -> handleDiscoverQuestsTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case OPEN_CHAT -> handleOpenChatTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case UNSUPPORTED -> handleUnsupportedTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
            };
        };

        return visionCanvasAssembler.assemble(
                conversation,
                turn,
                recentConversationSummaries(currentUser),
                visionExecutionPlanner.plan(conversation, understanding),
                visionQuestDiscoveryService.discover(conversation, understanding, currentUser)
        );
    }

    private boolean isCancelCommand(String prompt) {
        if (prompt == null) {
            return false;
        }

        String normalizedPrompt = prompt.trim().toLowerCase(java.util.Locale.ROOT)
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

    @Transactional
    public VisionConversationTurnResponseDTO resetConversation(Long conversationId, AppUser currentUser) {
        validateAccess(currentUser);
        VisionConversation conversation = loadExistingConversation(conversationId, currentUser);
        ensureTurnCapacity(conversation);
        VisionTurn turn = handleResetConversationAction(conversation, "system");
        return visionCanvasAssembler.assemble(
                conversation,
                turn,
                recentConversationSummaries(currentUser),
                visionExecutionPlanner.plan(conversation),
                visionQuestDiscoveryService.discover(conversation, VisionPromptUnderstandingResult.empty(""), currentUser)
        );
    }

    @Transactional
    public VisionConversationTurnResponseDTO cancelConversation(Long conversationId, AppUser currentUser) {
        validateAccess(currentUser);
        VisionConversation conversation = loadExistingConversation(conversationId, currentUser);
        ensureTurnCapacity(conversation);
        VisionTurn turn = handleCancelConversationAction(conversation, "system");
        return visionCanvasAssembler.assemble(
                conversation,
                turn,
                recentConversationSummaries(currentUser),
                visionExecutionPlanner.plan(conversation),
                visionQuestDiscoveryService.discover(conversation, VisionPromptUnderstandingResult.empty(""), currentUser)
        );
    }

    @Transactional(readOnly = true)
    public VisionConversationTurnResponseDTO loadConversation(Long conversationId, AppUser currentUser) {
        validateAccess(currentUser);
        VisionConversation conversation = loadExistingConversation(conversationId, currentUser);
        VisionTurn turn = visionTurnRepository.findTopByConversationOrderByTurnIndexDesc(conversation)
                .orElseGet(() -> snapshotTurn(conversation));
        return visionCanvasAssembler.assemble(
                conversation,
                turn,
                recentConversationSummaries(currentUser),
                visionExecutionPlanner.plan(conversation),
                visionQuestDiscoveryService.discover(conversation, VisionPromptUnderstandingResult.empty(""), currentUser)
        );
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
            VisionConversationAction action,
            VisionPromptUnderstandingResult understanding,
            VisionConversation existingConversation
    ) {
        VisionIntent detectedIntent = visionIntentRouter.detectIntent(normalizedPrompt, understanding);
        if (existingConversation != null) {
            if (action == VisionConversationAction.SUBMIT_PROMPT
                    && detectedIntent != VisionIntent.UNSUPPORTED
                    && detectedIntent != existingConversation.getIntent()) {
                VisionConversation switchedConversation = new VisionConversation();
                switchedConversation.setOwner(currentUser);
                switchedConversation.setIntent(detectedIntent);
                switchedConversation.setStatus(detectedIntent == VisionIntent.UNSUPPORTED
                        ? VisionConversationStatus.BLOCKED
                        : VisionConversationStatus.ACTIVE);
                switchedConversation.setSlotData(new LinkedHashMap<>());
                return visionConversationRepository.save(switchedConversation);
            }
            return existingConversation;
        }
        if (conversationId != null) {
            return loadExistingConversation(conversationId, currentUser);
        }
        if (action != VisionConversationAction.SUBMIT_PROMPT) {
            throw ServiceErrors.badRequest("Conversation action requires an existing conversation");
        }

        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(currentUser);
        conversation.setIntent(detectedIntent);
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
            title = conversation.getSlotData().get("search_query");
        }
        if ((title == null || title.isBlank()) && conversation.getIntent() == VisionIntent.OPEN_CHAT) {
            title = conversation.getSlotData().get("opened_chat_username");
        }
        if ((title == null || title.isBlank()) && conversation.getIntent() == VisionIntent.OPEN_CHAT) {
            title = conversation.getSlotData().get("target_user");
        }
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
        List<VisionSlotSummaryDTO> appliedSlotSummaries = visionTurnRepository.findTopByConversationOrderByTurnIndexDesc(conversation)
                .map(this::appliedSlotSummariesForTurn)
                .orElseGet(List::of);
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
                .appliedSlotSummaries(appliedSlotSummaries)
                .resumable(resumable)
                .completed(completed)
                .stale(stale)
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private String stageLabel(VisionConversation conversation) {
        return switch (conversation.getStatus()) {
            case ACTIVE -> {
                if (conversation.getIntent() == VisionIntent.DISCOVER_QUESTS) {
                    yield "Browsing";
                }
                if (conversation.getIntent() == VisionIntent.OPEN_CHAT) {
                    yield conversation.getRequestedSlot() == null ? "Chatting" : "Needs input";
                }
                yield conversation.getRequestedSlot() == null ? "In progress" : "Needs input";
            }
            case REVIEW_READY -> "Review ready";
            case COMPLETED -> "Complete";
            case BLOCKED -> "Blocked";
        };
    }

    private String progressLabel(VisionConversation conversation) {
        return switch (conversation.getStatus()) {
            case ACTIVE -> conversation.getIntent() == VisionIntent.DISCOVER_QUESTS
                    ? "Quest discovery is ready."
                    : conversation.getIntent() == VisionIntent.OPEN_CHAT
                    ? conversation.getRequestedSlot() == null
                    ? "Chat conversation is active."
                    : "Next step: " + visionClarificationService.buildQuestion(conversation.getRequestedSlot())
                    : conversation.getRequestedSlot() == null
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

    private List<VisionSlotSummaryDTO> appliedSlotSummariesForTurn(VisionTurn turn) {
        if (turn == null || turn.getAppliedSlotIds() == null || turn.getAppliedSlotIds().isEmpty()) {
            return List.of();
        }

        Map<String, String> slotData = turn.getConversation().getSlotData();
        return turn.getAppliedSlotIds().stream()
                .map(slotId -> VisionSlotSummaryDTO.builder()
                        .slotId(slotId)
                        .label(labelForSlot(slotId))
                        .value(valueForSlot(slotData, slotId))
                        .build())
                .filter(summary -> summary.getLabel() != null && summary.getValue() != null && !summary.getValue().isBlank())
                .toList();
    }

    private String labelForSlot(String slotId) {
        return switch (slotId) {
            case "quest_title" -> "Title";
            case "quest_description" -> "Description";
            case "reward_amount" -> "Reward";
            case "visibility" -> "Visibility";
            case "schedule_mode" -> "Schedule";
            case "scheduled_date" -> "Date";
            case "scheduled_time" -> "Time";
            case "location_mode" -> "Location";
            case "location_label" -> "Custom place";
            case "location_candidate_confirmation" -> "Location confirmation";
            default -> slotId;
        };
    }

    private String valueForSlot(Map<String, String> slotData, String slotId) {
        if (slotData == null) {
            return "";
        }

        return switch (slotId) {
            case "reward_amount" -> "true".equals(slotData.get("free_quest")) ? "Free" : slotData.get("reward_amount");
            case "schedule_mode" -> {
                String mode = slotData.get("schedule_mode");
                if ("fixed".equals(mode)) {
                    yield "Fixed time";
                }
                if ("agreement".equals(mode)) {
                    yield "By agreement";
                }
                yield mode;
            }
            case "scheduled_date" -> slotData.get("scheduled_date");
            case "scheduled_time" -> slotData.get("scheduled_time");
            case "location_mode" -> {
                String mode = slotData.get("location_mode");
                if ("profile".equals(mode)) {
                    yield "Use profile location";
                }
                if ("off".equals(mode)) {
                    yield "Hide location";
                }
                if ("custom".equals(mode)) {
                    String label = slotData.get("location_label");
                    yield label == null || label.isBlank() ? "Custom place" : label;
                }
                yield mode;
            }
            default -> slotData.get(slotId);
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

    private VisionTurn handleConfirmReviewAction(
            VisionConversation conversation,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getIntent() != VisionIntent.CREATE_QUEST) {
            throw ServiceErrors.conflict("Review confirmation is only supported for create quest conversations");
        }
        return handleCreateQuestReviewTurnInternal(conversation, "", "", understanding, source, VisionConversationAction.CONFIRM_REVIEW);
    }

    private VisionTurn handleReviewEditAction(
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

    private VisionTurn handleResetConversationAction(VisionConversation conversation, String source) {
        VisionIntent intent = conversation.getIntent() == VisionIntent.DISCOVER_QUESTS
                ? VisionIntent.DISCOVER_QUESTS
                : conversation.getIntent() == VisionIntent.OPEN_CHAT
                ? VisionIntent.OPEN_CHAT
                : VisionIntent.CREATE_QUEST;
        conversation.setIntent(intent);
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        conversation.setRequestedSlot(intent == VisionIntent.DISCOVER_QUESTS
                ? null
                : intent == VisionIntent.OPEN_CHAT ? "target_user" : "quest_title");
        conversation.setSlotData(new LinkedHashMap<>());
        String message = intent == VisionIntent.DISCOVER_QUESTS
                ? "The current quest discovery was reset. What would you like to browse next?"
                : intent == VisionIntent.OPEN_CHAT
                ? "The current chat task was reset. Who should I open chat with?"
                : "The current vision task was reset. What should the new quest be called?";
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(
                conversation,
                source,
                "",
                "",
                intent,
                intent == VisionIntent.DISCOVER_QUESTS ? VisionAgentState.RECOMMENDING : VisionAgentState.ASKING,
                intent == VisionIntent.DISCOVER_QUESTS ? VisionNextAction.SHOW_RESULTS : VisionNextAction.ASK_FOR_SLOT,
                intent == VisionIntent.DISCOVER_QUESTS ? null : intent == VisionIntent.OPEN_CHAT ? "target_user" : "quest_title",
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
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleCreateQuestReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
        }

        Map<String, String> beforeSlots = new LinkedHashMap<>(conversation.getSlotData());
        Map<String, String> mergedSlots = visionSlotService.mergeCreateQuestSlots(conversation, normalizedPrompt, understanding);
        List<String> appliedSlotIds = appliedSlotIds(beforeSlots, mergedSlots);
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
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
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
        turn.setTranslationApplied(understanding.isTranslationApplied());
        turn.setTranslationReliable(understanding.isTranslationReliable());
        turn.setAssistantMessage(message);
        turn.setAppliedSlotIds(appliedSlotIds);
        return visionTurnRepository.save(turn);
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
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
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
                understanding.isTranslationApplied(),
                understanding.isTranslationReliable(),
                message
        );
    }

    private VisionTurn handleCreateQuestReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleCreateQuestReviewTurnInternal(conversation, prompt, normalizedPrompt, understanding, source, VisionConversationAction.SUBMIT_PROMPT);
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
        updateConversationMetadata(conversation, "", "", message, understanding.isTranslationReliable());
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
                understanding.isTranslationReliable(),
                message
        );
    }

    private VisionTurn handleUnsupportedTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String message = "This vision backend only supports stepwise create_quest planning and read-only quest discovery for now.";
        conversation.setStatus(VisionConversationStatus.BLOCKED);
        conversation.setRequestedSlot(null);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
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
        turn.setTranslationApplied(understanding.isTranslationApplied());
        turn.setTranslationReliable(understanding.isTranslationReliable());
        turn.setAssistantMessage(message);
        return visionTurnRepository.save(turn);
    }

    private VisionTurn handleDiscoverQuestsTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        VisionQuestDiscoveryDTO discovery = visionQuestDiscoveryService.discover(conversation, understanding, conversation.getOwner());
        if (discovery == null) {
            throw ServiceErrors.conflict("Discovery is not available for this vision conversation");
        }

        conversation.setStatus(VisionConversationStatus.ACTIVE);
        conversation.setRequestedSlot(null);
        conversation.getSlotData().put("search_query", discovery.getQuery() == null ? "" : discovery.getQuery());
        String message = discovery.getSummary();
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);

        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setTurnIndex((int) visionTurnRepository.countByConversation(conversation) + 1);
        turn.setSource(VisionTurnSource.from(source));
        turn.setPrompt(prompt);
        turn.setNormalizedPrompt(normalizedPrompt);
        turn.setDetectedIntent(VisionIntent.DISCOVER_QUESTS);
        turn.setAgentState(VisionAgentState.RECOMMENDING);
        turn.setNextAction(VisionNextAction.SHOW_RESULTS);
        turn.setRequestedSlot(null);
        turn.setTranslationApplied(understanding.isTranslationApplied());
        turn.setTranslationReliable(understanding.isTranslationReliable());
        turn.setAssistantMessage(message);
        return visionTurnRepository.save(turn);
    }

    private VisionTurn handleOpenChatTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        VisionChatExecutionResult result = visionChatExecutionService.openChat(
                conversation.getOwner(),
                normalizedPrompt,
                understanding.semanticPlanOrEmpty()
        );

        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;

        if (result.isExecuted()) {
            status = VisionConversationStatus.COMPLETED;
            nextAction = VisionNextAction.COMPLETE;
            agentState = VisionAgentState.COMPLETE;
            conversation.getSlotData().put("conversation_outcome", "opened_chat");
            conversation.getSlotData().put("opened_chat_conversation_id", result.getConversation().getConversationId().toString());
            conversation.getSlotData().put("opened_chat_username", result.getConversation().getOtherUsername());
            conversation.getSlotData().put("opened_chat_user_id", result.getConversation().getOtherUserId().toString());
            message = "Chat opened with " + result.getConversation().getOtherUsername() + ".";
            conversation.setRequestedSlot(null);
        } else {
            status = VisionConversationStatus.ACTIVE;
            nextAction = VisionNextAction.ASK_FOR_SLOT;
            agentState = VisionAgentState.ASKING;
            conversation.setRequestedSlot("target_user");
            message = result.getBlockingReason();
            if (message == null || message.isBlank()) {
                message = "Who should I open chat with?";
            }
        }

        conversation.setStatus(status);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);

        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setTurnIndex((int) visionTurnRepository.countByConversation(conversation) + 1);
        turn.setSource(VisionTurnSource.from(source));
        turn.setPrompt(prompt);
        turn.setNormalizedPrompt(normalizedPrompt);
        turn.setDetectedIntent(VisionIntent.OPEN_CHAT);
        turn.setAgentState(agentState);
        turn.setNextAction(nextAction);
        turn.setRequestedSlot(conversation.getRequestedSlot());
        turn.setTranslationApplied(understanding.isTranslationApplied());
        turn.setTranslationReliable(understanding.isTranslationReliable());
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

    private List<String> appliedSlotIds(Map<String, String> beforeSlots, Map<String, String> afterSlots) {
        List<String> applied = new java.util.ArrayList<>();
        if (afterSlots == null || afterSlots.isEmpty()) {
            return applied;
        }

        for (Map.Entry<String, String> entry : afterSlots.entrySet()) {
            String key = entry.getKey();
            String afterValue = entry.getValue();
            String beforeValue = beforeSlots == null ? null : beforeSlots.get(key);
            if (afterValue == null || afterValue.isBlank()) {
                continue;
            }
            if (!java.util.Objects.equals(beforeValue, afterValue)) {
                applied.add(key);
            }
        }

        return applied;
    }

    private void ensureTurnCapacity(VisionConversation conversation) {
        long turnCount = visionTurnRepository.countByConversation(conversation);
        if (turnCount >= visionProperties.getMaxTurnsPerConversation()) {
            throw ServiceErrors.conflict("Vision conversation reached the maximum number of turns");
        }
    }

    private String normalizePrompt(String prompt, VisionPromptUnderstandingResult understanding) {
        if (understanding.getNormalizedPrompt() != null && !understanding.getNormalizedPrompt().isBlank()) {
            return understanding.getNormalizedPrompt().trim();
        }
        return prompt;
    }

    private String requirePrompt(VisionConversationTurnRequestDTO dto) {
        if (dto == null || dto.getEffectivePrompt().isBlank()) {
            throw ServiceErrors.badRequest("Prompt is required");
        }
        String prompt = dto.getEffectivePrompt();
        if (prompt.length() > visionProperties.getMaxPromptLength()) {
            throw ServiceErrors.badRequest("Prompt is too long");
        }
        return prompt;
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
