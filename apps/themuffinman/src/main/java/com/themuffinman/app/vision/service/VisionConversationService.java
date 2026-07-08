package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionConversationTurnRequestDTO;
import com.themuffinman.app.vision.dto.VisionConversationListResponseDTO;
import com.themuffinman.app.vision.dto.VisionConversationSummaryDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.VisionExecutionCandidateDTO;
import com.themuffinman.app.vision.dto.VisionMemoryTrailDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.vision.dto.VisionQuestDiscoveryDTO;
import com.themuffinman.app.vision.dto.VisionSearchDiscoveryDTO;
import com.themuffinman.app.workmarket.dto.ApplicationAllowedActionDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final VisionSearchDiscoveryService visionSearchDiscoveryService;
    private final VisionExecutionService visionExecutionService;
    private final VisionChatExecutionService visionChatExecutionService;
    private final VisionCapabilityPreviewService visionCapabilityPreviewService;
    private final VisionPromptUnderstandingService visionPromptUnderstandingService;
    private final VisionSemanticOrchestrationContextService visionSemanticOrchestrationContextService;
    private final VisionConversationReadModelAssembler visionConversationReadModelAssembler;
    private final VisionReadOnlyConversationTurnHandler visionReadOnlyConversationTurnHandler;
    private final VisionDetailConversationTurnSupport visionDetailConversationTurnSupport;
    private final VisionConversationSlotResolutionSupport visionConversationSlotResolutionSupport;
    private final VisionIntentSignalSupport visionIntentSignalSupport;
    private final VisionConversationLifecycleSupport visionConversationLifecycleSupport;
    private final VisionConversationMutationSupport visionConversationMutationSupport;
    private final VisionQuestReviewSupport visionQuestReviewSupport;
    private final VisionSemanticMapper visionSemanticMapper;
    private final VisionSemanticRouteCatalogService visionSemanticRouteCatalogService;
    private final VisionSurfacePolicy visionSurfacePolicy;
    private final VisionProperties visionProperties;
    private final VisionLearningService visionLearningService;

    @Autowired
    public VisionConversationService(
            VisionConversationRepository visionConversationRepository,
            VisionTurnRepository visionTurnRepository,
            VisionIntentRouter visionIntentRouter,
            VisionSlotService visionSlotService,
            VisionClarificationService visionClarificationService,
            VisionCanvasAssembler visionCanvasAssembler,
            VisionExecutionPlanner visionExecutionPlanner,
            VisionQuestDiscoveryService visionQuestDiscoveryService,
            VisionSearchDiscoveryService visionSearchDiscoveryService,
            VisionExecutionService visionExecutionService,
            VisionChatExecutionService visionChatExecutionService,
            VisionCapabilityPreviewService visionCapabilityPreviewService,
            VisionPromptUnderstandingService visionPromptUnderstandingService,
            VisionSemanticOrchestrationContextService visionSemanticOrchestrationContextService,
            VisionConversationReadModelAssembler visionConversationReadModelAssembler,
            VisionSemanticMapper visionSemanticMapper,
            VisionSemanticRouteCatalogService visionSemanticRouteCatalogService,
            VisionSurfacePolicy visionSurfacePolicy,
            VisionProperties visionProperties,
            VisionLearningService visionLearningService
    ) {
        this.visionConversationRepository = visionConversationRepository;
        this.visionTurnRepository = visionTurnRepository;
        this.visionIntentRouter = visionIntentRouter;
        this.visionSlotService = visionSlotService;
        this.visionClarificationService = visionClarificationService;
        this.visionCanvasAssembler = visionCanvasAssembler;
        this.visionExecutionPlanner = visionExecutionPlanner;
        this.visionQuestDiscoveryService = visionQuestDiscoveryService;
        this.visionSearchDiscoveryService = visionSearchDiscoveryService;
        this.visionExecutionService = visionExecutionService;
        this.visionChatExecutionService = visionChatExecutionService;
        this.visionCapabilityPreviewService = visionCapabilityPreviewService;
        this.visionPromptUnderstandingService = visionPromptUnderstandingService;
        this.visionSemanticOrchestrationContextService = visionSemanticOrchestrationContextService;
        this.visionConversationReadModelAssembler = visionConversationReadModelAssembler;
        this.visionReadOnlyConversationTurnHandler = new VisionReadOnlyConversationTurnHandler();
        this.visionDetailConversationTurnSupport = new VisionDetailConversationTurnSupport();
        this.visionConversationSlotResolutionSupport = new VisionConversationSlotResolutionSupport(visionClarificationService);
        this.visionIntentSignalSupport = new VisionIntentSignalSupport();
        this.visionConversationMutationSupport = new VisionConversationMutationSupport(
                visionConversationRepository,
                visionTurnRepository,
                visionSemanticOrchestrationContextService,
                visionProperties
        );
        this.visionConversationLifecycleSupport = new VisionConversationLifecycleSupport(
                visionConversationRepository,
                visionSemanticOrchestrationContextService,
                visionConversationMutationSupport
        );
        this.visionQuestReviewSupport = new VisionQuestReviewSupport(
                visionExecutionService,
                visionSurfacePolicy,
                visionSlotService,
                visionClarificationService,
                visionSemanticMapper,
                visionConversationRepository,
                visionConversationMutationSupport
        );
        this.visionSemanticMapper = visionSemanticMapper;
        this.visionSemanticRouteCatalogService = visionSemanticRouteCatalogService;
        this.visionSurfacePolicy = visionSurfacePolicy;
        this.visionProperties = visionProperties;
        this.visionLearningService = visionLearningService;
    }

    public VisionConversationService(
            VisionConversationRepository visionConversationRepository,
            VisionTurnRepository visionTurnRepository,
            VisionIntentRouter visionIntentRouter,
            VisionSlotService visionSlotService,
            VisionClarificationService visionClarificationService,
            VisionCanvasAssembler visionCanvasAssembler,
            VisionExecutionPlanner visionExecutionPlanner,
            VisionQuestDiscoveryService visionQuestDiscoveryService,
            VisionSearchDiscoveryService visionSearchDiscoveryService,
            VisionExecutionService visionExecutionService,
            VisionChatExecutionService visionChatExecutionService,
            VisionCapabilityPreviewService visionCapabilityPreviewService,
            VisionPromptUnderstandingService visionPromptUnderstandingService,
            VisionSemanticOrchestrationContextService visionSemanticOrchestrationContextService,
            VisionConversationReadModelAssembler visionConversationReadModelAssembler,
            VisionSemanticMapper visionSemanticMapper,
            VisionSemanticRouteCatalogService visionSemanticRouteCatalogService,
            VisionSurfacePolicy visionSurfacePolicy,
            VisionProperties visionProperties
    ) {
        this(
                visionConversationRepository,
                visionTurnRepository,
                visionIntentRouter,
                visionSlotService,
                visionClarificationService,
                visionCanvasAssembler,
                visionExecutionPlanner,
                visionQuestDiscoveryService,
                visionSearchDiscoveryService,
                visionExecutionService,
                visionChatExecutionService,
                visionCapabilityPreviewService,
                visionPromptUnderstandingService,
                visionSemanticOrchestrationContextService,
                visionConversationReadModelAssembler,
                visionSemanticMapper,
                visionSemanticRouteCatalogService,
                visionSurfacePolicy,
                visionProperties,
                null
        );
    }

    @Transactional
    public VisionConversationTurnResponseDTO processTurn(VisionConversationTurnRequestDTO dto, AppUser currentUser) {
        validateAccess(currentUser);
        if (!visionProperties.isEnabled()) {
            throw ServiceErrors.forbidden("Vision conversations are disabled");
        }

        VisionConversationAction action = VisionConversationAction.from(dto == null ? null : dto.getAction());
        String rawPrompt = dto == null ? null : dto.getPrompt();
        if (action == VisionConversationAction.SUBMIT_PROMPT && visionConversationLifecycleSupport.isCancelCommand(rawPrompt)) {
            Long conversationId = dto == null ? null : dto.getConversationId();
            if (conversationId == null) {
                throw ServiceErrors.badRequest("Conversation id is required to cancel an active vision task");
            }
            return cancelConversation(conversationId, currentUser);
        }
        VisionConversation existingConversation = dto != null && dto.getConversationId() != null
                ? visionConversationLifecycleSupport.loadExistingConversation(dto.getConversationId(), currentUser)
                : null;
        String prompt = action == VisionConversationAction.SUBMIT_PROMPT ? requirePrompt(dto) : "";
        VisionSemanticRuntimeHints runtimeHints = runtimeHints(dto);
        VisionPromptUnderstandingResult understanding = action == VisionConversationAction.SUBMIT_PROMPT
                ? visionPromptUnderstandingService.understandPrompt(prompt, existingConversation, currentUser, runtimeHints)
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
        rememberRuntimeHints(conversation, runtimeHints);
        visionConversationMutationSupport.ensureTurnCapacity(conversation);

        String source = effectiveSource(dto);
        VisionTurn turn = handleConversationTurn(action, conversation, prompt, normalizedPrompt, understanding, source, dto);

        if (visionLearningService != null) {
            visionLearningService.recordTurnOutcome(
                    currentUser,
                    conversation,
                    turn,
                    understanding,
                    runtimeHints,
                    action
            );
        }

        return assembleConversationResponse(
                conversation,
                turn,
                currentUser,
                understanding,
                visionExecutionPlanner.plan(conversation, understanding)
        );
    }

    private VisionTurn handleConversationTurn(
            VisionConversationAction action,
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source,
            VisionConversationTurnRequestDTO dto
    ) {
        return switch (action) {
            case CONFIRM_REVIEW -> handleConfirmReviewAction(conversation, understanding, source);
            case REQUEST_REVIEW_EDIT -> handleReviewEditAction(conversation, understanding, source, dto);
            case SUBMIT_PROMPT -> handleSubmitPromptTurn(conversation, prompt, normalizedPrompt, understanding, source);
        };
    }

    private VisionTurn handleSubmitPromptTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return switch (conversation.getIntent()) {
            case CREATE_QUEST -> handleCreateQuestTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case CREATE_CIRCLE -> handleCreateCircleTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case CREATE_CIRCLE_REQUEST -> handleCreateCircleRequestTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case ACCEPT_CIRCLE_REQUEST -> handleAcceptCircleRequestTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case DELETE_CIRCLE_REQUEST -> handleDeleteCircleRequestTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case UPDATE_CIRCLE -> handleUpdateCircleTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case DELETE_CIRCLE -> handleDeleteCircleTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case CREATE_APPLICATION -> handleCreateApplicationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case UPDATE_APPLICATION -> handleUpdateApplicationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case WITHDRAW_APPLICATION -> handleWithdrawApplicationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case APPROVE_APPLICATION -> handleApproveApplicationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case DECLINE_APPLICATION -> handleDeclineApplicationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case UPDATE_PROFILE -> handleUpdateProfileTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case UPDATE_PROFILE_LOCATION -> handleUpdateProfileLocationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case DISCOVER_QUESTS -> handleDiscoverQuestsTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case SEARCH -> handleSearchTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case OPEN_CHAT -> handleOpenChatTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_CHAT_WORKSPACE -> visionReadOnlyConversationTurnHandler.handleViewChatWorkspaceTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_PROFILE -> visionReadOnlyConversationTurnHandler.handleViewProfileTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_SETTINGS -> visionReadOnlyConversationTurnHandler.handleViewSettingsTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_USER_PROFILE -> handleViewUserProfileTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_CIRCLES -> visionReadOnlyConversationTurnHandler.handleViewCirclesTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_CIRCLE_DETAIL -> handleViewCircleDetailTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_QUEST_DETAIL -> handleViewQuestDetailTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_NOTIFICATIONS -> visionReadOnlyConversationTurnHandler.handleViewNotificationsTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_QUEST_NEWS -> visionReadOnlyConversationTurnHandler.handleViewQuestNewsTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_APPLICATIONS -> visionReadOnlyConversationTurnHandler.handleViewApplicationsTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_APPLICATION_DETAIL -> handleViewApplicationDetailTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_THINGS -> visionReadOnlyConversationTurnHandler.handleViewThingsTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case UNSUPPORTED -> handleUnsupportedTurn(conversation, prompt, normalizedPrompt, understanding, source);
        };
    }

    @Transactional
    public VisionConversationTurnResponseDTO resetConversation(Long conversationId, AppUser currentUser) {
        validateAccess(currentUser);
        VisionConversation conversation = visionConversationLifecycleSupport.loadExistingConversation(conversationId, currentUser);
        visionConversationMutationSupport.ensureTurnCapacity(conversation);
        VisionTurn turn = visionConversationLifecycleSupport.resetConversationAction(conversation, "system");
        if (visionLearningService != null) {
            visionLearningService.recordTurnOutcome(
                    currentUser,
                    conversation,
                    turn,
                    emptyUnderstanding(),
                    null,
                    VisionConversationAction.SUBMIT_PROMPT
            );
        }
        return assembleConversationResponse(
                conversation,
                turn,
                currentUser,
                VisionPromptUnderstandingResult.empty(""),
                visionExecutionPlanner.plan(conversation)
        );
    }

    @Transactional
    public VisionConversationTurnResponseDTO cancelConversation(Long conversationId, AppUser currentUser) {
        validateAccess(currentUser);
        VisionConversation conversation = visionConversationLifecycleSupport.loadExistingConversation(conversationId, currentUser);
        visionConversationMutationSupport.ensureTurnCapacity(conversation);
        VisionTurn turn = visionConversationLifecycleSupport.cancelConversationAction(conversation, "system");
        if (visionLearningService != null) {
            visionLearningService.recordTurnOutcome(
                    currentUser,
                    conversation,
                    turn,
                    emptyUnderstanding(),
                    null,
                    VisionConversationAction.SUBMIT_PROMPT
            );
        }
        return assembleConversationResponse(
                conversation,
                turn,
                currentUser,
                VisionPromptUnderstandingResult.empty(""),
                visionExecutionPlanner.plan(conversation)
        );
    }

    @Transactional(readOnly = true)
    public VisionConversationTurnResponseDTO loadConversation(Long conversationId, AppUser currentUser) {
        validateAccess(currentUser);
        VisionConversation conversation = visionConversationLifecycleSupport.loadExistingConversation(conversationId, currentUser);
        VisionTurn turn = visionTurnRepository.findTopByConversationOrderByTurnIndexDesc(conversation)
                .orElseGet(() -> visionConversationLifecycleSupport.snapshotTurn(conversation));
        return assembleConversationResponse(
                conversation,
                turn,
                currentUser,
                emptyUnderstanding(),
                visionExecutionPlanner.plan(conversation)
        );
    }

    @Transactional(readOnly = true)
    public VisionConversationListResponseDTO listRecentConversations(AppUser currentUser) {
        validateAccess(currentUser);
        return VisionConversationListResponseDTO.builder()
                .items(visionConversationReadModelAssembler.recentConversationSummaries(currentUser))
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
        VisionIntent semanticIntent = understanding == null
                ? VisionIntent.UNSUPPORTED
                : understanding.semanticPlanOrEmpty().candidateIntentOrUnsupported();
        double semanticIntentConfidence = understanding == null || understanding.semanticPlanOrEmpty().getCandidateIntentConfidence() == null
                ? 0.0d
                : understanding.semanticPlanOrEmpty().getCandidateIntentConfidence();
        VisionIntent detectedIntent = existingConversation != null
                && action == VisionConversationAction.SUBMIT_PROMPT
                && semanticIntent == VisionIntent.UNSUPPORTED
                ? existingConversation.getIntent()
                : visionIntentRouter.detectIntent(normalizedPrompt, understanding);
        if (existingConversation != null) {
            if (action == VisionConversationAction.SUBMIT_PROMPT
                    && detectedIntent != VisionIntent.UNSUPPORTED
                    && detectedIntent != existingConversation.getIntent()) {
                if (visionIntentSignalSupport.sameIntentWorkspaceFamily(existingConversation.getIntent(), detectedIntent)) {
                    existingConversation.setIntent(detectedIntent);
                    existingConversation.setStatus(detectedIntent == VisionIntent.UNSUPPORTED
                            ? VisionConversationStatus.BLOCKED
                            : VisionConversationStatus.ACTIVE);
                    return visionConversationRepository.save(existingConversation);
                }
                if (visionIntentSignalSupport.shouldKeepExistingConversation(normalizedPrompt, detectedIntent, semanticIntentConfidence)) {
                    return existingConversation;
                }
                retireConversationForTaskSwitch(existingConversation);
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
            return visionConversationLifecycleSupport.loadExistingConversation(conversationId, currentUser);
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

    private void retireConversationForTaskSwitch(VisionConversation conversation) {
        if (conversation == null || conversation.getStatus() == VisionConversationStatus.COMPLETED) {
            return;
        }

        if (conversation.getSlotData() == null) {
            conversation.setSlotData(new LinkedHashMap<>());
        }
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.COMPLETED);
        conversation.getSlotData().put("conversation_outcome", "superseded");
        String message = "This vision task was left behind when a newer task started.";
        visionConversationMutationSupport.updateConversationMetadata(
                conversation,
                conversation.getLastUserPrompt() == null ? "" : conversation.getLastUserPrompt(),
                conversation.getLastNormalizedPrompt() == null ? "" : conversation.getLastNormalizedPrompt(),
                message,
                conversation.isLastTranslationReliable()
        );
        visionConversationRepository.save(conversation);
        visionConversationMutationSupport.createTurn(
                conversation,
                "system",
                conversation.getLastUserPrompt() == null ? "" : conversation.getLastUserPrompt(),
                conversation.getLastNormalizedPrompt() == null ? "" : conversation.getLastNormalizedPrompt(),
                conversation.getIntent(),
                VisionAgentState.COMPLETE,
                VisionNextAction.COMPLETE,
                null,
                false,
                conversation.isLastTranslationReliable(),
                message
        );
    }

    private VisionTurn handleConfirmReviewAction(
            VisionConversation conversation,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return switch (conversation.getIntent()) {
            case CREATE_QUEST -> visionQuestReviewSupport.handleConfirmReviewAction(conversation, understanding, source);
            case CREATE_CIRCLE -> handleCreateCircleReviewTurn(conversation, "confirm", "confirm", understanding, source);
            case CREATE_CIRCLE_REQUEST -> handleCreateCircleRequestReviewTurn(conversation, "confirm", "confirm", understanding, source);
            case ACCEPT_CIRCLE_REQUEST -> handleAcceptCircleRequestReviewTurn(conversation, "confirm", "confirm", understanding, source);
            case DELETE_CIRCLE_REQUEST -> handleDeleteCircleRequestReviewTurn(conversation, "confirm", "confirm", understanding, source);
            case UPDATE_CIRCLE -> handleUpdateCircleReviewTurn(conversation, "confirm", "confirm", understanding, source);
            case DELETE_CIRCLE -> handleDeleteCircleReviewTurn(conversation, "confirm", "confirm", understanding, source);
            case CREATE_APPLICATION -> handleCreateApplicationReviewTurn(conversation, "confirm", "confirm", understanding, source);
            case UPDATE_APPLICATION -> handleUpdateApplicationReviewTurn(conversation, "confirm", "confirm", understanding, source);
            case WITHDRAW_APPLICATION -> handleWithdrawApplicationReviewTurn(conversation, "confirm", "confirm", understanding, source);
            case APPROVE_APPLICATION -> handleApproveApplicationReviewTurn(conversation, "confirm", "confirm", understanding, source);
            case DECLINE_APPLICATION -> handleDeclineApplicationReviewTurn(conversation, "confirm", "confirm", understanding, source);
            case UPDATE_PROFILE -> handleUpdateProfileReviewTurn(conversation, "confirm", "confirm", understanding, source);
            case UPDATE_PROFILE_LOCATION -> handleUpdateProfileLocationReviewTurn(conversation, "confirm", "confirm", understanding, source);
            default -> throw ServiceErrors.conflict("Review confirmation is not supported for this vision conversation");
        };
    }

    private VisionTurn handleReviewEditAction(
            VisionConversation conversation,
            VisionPromptUnderstandingResult understanding,
            String source,
            VisionConversationTurnRequestDTO dto
    ) {
        if (conversation.getIntent() == VisionIntent.CREATE_QUEST) {
            return visionQuestReviewSupport.handleReviewEditAction(conversation, understanding, source, dto);
        }
        return handleGenericReviewEditAction(conversation, understanding, source, dto);
    }

    private String effectiveSource(VisionConversationTurnRequestDTO dto) {
        return dto == null ? null : dto.getEffectiveSource();
    }

    private VisionSemanticRuntimeHints runtimeHints(VisionConversationTurnRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return VisionSemanticRuntimeHints.builder()
                .inputType(dto.getInputType())
                .clientLocale(dto.getClientLocale())
                .clientTimezone(dto.getClientTimezone())
                .clientCapabilities(dto.getClientCapabilities())
                .clientStateVersion(dto.getClientStateVersion())
                .build();
    }

    private VisionPromptUnderstandingResult emptyUnderstanding() {
        return VisionPromptUnderstandingResult.empty("");
    }

    private void rememberRuntimeHints(VisionConversation conversation, VisionSemanticRuntimeHints runtimeHints) {
        if (conversation == null || runtimeHints == null) {
            return;
        }
        if (conversation.getSlotData() == null) {
            conversation.setSlotData(new LinkedHashMap<>());
        }

        String clientLocale = cleanRuntimeHint(runtimeHints.getClientLocale());
        if (clientLocale != null) {
            conversation.getSlotData().put("client_locale", clientLocale);
        }
        String clientTimezone = cleanRuntimeHint(runtimeHints.getClientTimezone());
        if (clientTimezone != null) {
            conversation.getSlotData().put("client_timezone", clientTimezone);
        }
    }

    private String cleanRuntimeHint(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isBlank() ? null : cleaned;
    }

    private VisionTurn handleGenericReviewEditAction(
            VisionConversation conversation,
            VisionPromptUnderstandingResult understanding,
            String source,
            VisionConversationTurnRequestDTO dto
    ) {
        if (conversation.getStatus() != VisionConversationStatus.REVIEW_READY) {
            throw ServiceErrors.conflict("Review editing requires a review-ready vision conversation");
        }

        VisionReviewTarget reviewTarget = VisionReviewTarget.from(dto == null ? null : dto.getReviewTarget());
        if (reviewTarget == null) {
            throw ServiceErrors.badRequest("Review target is required for typed review editing");
        }
        if (!supportsReviewTarget(conversation.getIntent(), reviewTarget)) {
            throw ServiceErrors.conflict("Typed review editing is not supported for this review target on " + conversation.getIntent().name());
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
                conversation.getIntent(),
                VisionAgentState.ASKING,
                VisionNextAction.ASK_FOR_SLOT,
                editSlot,
                false,
                understanding.isTranslationReliable(),
                message
        );
    }

    private boolean supportsReviewTarget(VisionIntent intent, VisionReviewTarget reviewTarget) {
        if (intent == null || reviewTarget == null) {
            return false;
        }
        return switch (intent) {
            case CREATE_CIRCLE -> reviewTarget == VisionReviewTarget.CIRCLE_NAME;
            case CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST -> reviewTarget == VisionReviewTarget.TARGET_USER;
            case UPDATE_CIRCLE -> reviewTarget == VisionReviewTarget.TARGET_CIRCLE || reviewTarget == VisionReviewTarget.CIRCLE_NAME;
            case DELETE_CIRCLE -> reviewTarget == VisionReviewTarget.TARGET_CIRCLE;
            case CREATE_APPLICATION, UPDATE_APPLICATION -> reviewTarget == VisionReviewTarget.TARGET_QUEST
                    || reviewTarget == VisionReviewTarget.APPLICATION_MESSAGE
                    || reviewTarget == VisionReviewTarget.APPLICATION_PRICE;
            case WITHDRAW_APPLICATION -> reviewTarget == VisionReviewTarget.TARGET_QUEST;
            case APPROVE_APPLICATION, DECLINE_APPLICATION -> reviewTarget == VisionReviewTarget.TARGET_QUEST
                    || reviewTarget == VisionReviewTarget.TARGET_USER;
            case UPDATE_PROFILE -> reviewTarget == VisionReviewTarget.PROFILE_USERNAME
                    || reviewTarget == VisionReviewTarget.PROFILE_DESCRIPTION;
            case UPDATE_PROFILE_LOCATION -> reviewTarget == VisionReviewTarget.PROFILE_LOCATION_MODE
                    || reviewTarget == VisionReviewTarget.PROFILE_LOCATION;
            default -> false;
        };
    }

    private VisionConversationTurnResponseDTO assembleConversationResponse(
            VisionConversation conversation,
            VisionTurn turn,
            AppUser currentUser,
            VisionPromptUnderstandingResult discoveryUnderstanding,
            VisionExecutionCandidateDTO executionPlan
    ) {
        return visionCanvasAssembler.assemble(
                conversation,
                turn,
                visionConversationReadModelAssembler.recentConversationSummaries(currentUser),
                executionPlan,
                visionQuestDiscoveryService.discover(conversation, discoveryUnderstanding, currentUser),
                searchDiscoveryForConversation(conversation, currentUser),
                VisionConversationSnapshotSupport.capabilityPreview(conversation, currentUser, visionCapabilityPreviewService),
                visionConversationReadModelAssembler.buildMemoryTrail(currentUser, conversation)
        );
    }

    private VisionTurn handleCreateQuestReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return visionQuestReviewSupport.handleCreateQuestReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
    }

    private VisionTurn handleCreateQuestTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return visionQuestReviewSupport.handleCreateQuestReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
        }

        Map<String, String> beforeSlots = new LinkedHashMap<>(conversation.getSlotData());
        Map<String, String> mergedSlots = visionSlotService.mergeCreateQuestSlots(conversation, normalizedPrompt, understanding);
        List<String> appliedSlotIds = appliedSlotIds(beforeSlots, mergedSlots);
        String missingSlot = visionClarificationService.nextMissingCreateQuestSlot(mergedSlots);
        boolean lowConfidence = isLowConfidenceCreateQuestUnderstanding(understanding);

        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;
        if (missingSlot == null && lowConfidence) {
            VisionSemanticUserMemoryContext userMemory = visionConversationLifecycleSupport.userMemoryFor(conversation);
            status = VisionConversationStatus.ACTIVE;
            nextAction = VisionNextAction.ASK_FOR_SLOT;
            agentState = VisionAgentState.ASKING;
            missingSlot = "quest_title";
            message = conversation.getRequestedSlot() != null && conversation.getRequestedSlot().equals("quest_title")
                    ? visionClarificationService.buildCreateQuestConfidenceRetryQuestion(userMemory)
                    : visionClarificationService.buildCreateQuestConfidenceQuestion(userMemory);
        } else if (missingSlot == null) {
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
        double minimumConfidence = visionSemanticRouteCatalogService.minimumConfidenceForIntent(VisionIntent.CREATE_QUEST);
        return confidence < minimumConfidence;
    }

    private VisionTurn handleCreateCircleTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleCreateCircleReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
        }

        String circleName = resolveCircleName(conversation, normalizedPrompt, understanding);
        if (circleName == null || circleName.isBlank()) {
            conversation.setRequestedSlot("circle_name");
            conversation.setStatus(VisionConversationStatus.ACTIVE);
            String message = "What should the circle be called?";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(
                    conversation,
                    source,
                    prompt,
                    normalizedPrompt,
                    VisionIntent.CREATE_CIRCLE,
                    VisionAgentState.ASKING,
                    VisionNextAction.ASK_FOR_SLOT,
                    "circle_name",
                    understanding.isTranslationApplied(),
                    understanding.isTranslationReliable(),
                    message
            );
        }

        conversation.getSlotData().put("circle_name", circleName);
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = visionProperties.isExecutionEnabled()
                ? "I have enough to prepare a circle review. Confirm when you want me to create it."
                : "I have enough for a circle review. Execution is still disabled, so this phase stops at review.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                VisionIntent.CREATE_CIRCLE,
                VisionAgentState.REVIEW_READY,
                VisionNextAction.SHOW_REVIEW,
                null,
                understanding.isTranslationApplied(),
                understanding.isTranslationReliable(),
                message
        );
    }

    private VisionTurn handleCreateCircleReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;

        if (VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                status = VisionConversationStatus.REVIEW_READY;
                nextAction = VisionNextAction.SHOW_REVIEW;
                agentState = VisionAgentState.REVIEW_READY;
                message = "The circle review is ready, but execution is still disabled.";
            } else {
                VisionExecutionResult executionResult = visionExecutionService.execute(conversation);
                if (executionResult.isExecuted()) {
                    conversation.getSlotData().put("created_circle_id", executionResult.getCreatedCircle().getId().toString());
                    conversation.getSlotData().put("conversation_outcome", "created_circle");
                    status = VisionConversationStatus.COMPLETED;
                    nextAction = VisionNextAction.COMPLETE;
                    agentState = VisionAgentState.COMPLETE;
                    message = "Circle created successfully.";
                } else {
                    status = VisionConversationStatus.REVIEW_READY;
                    nextAction = VisionNextAction.SHOW_REVIEW;
                    agentState = VisionAgentState.REVIEW_READY;
                    message = executionResult.getBlockingReason();
                }
            }
        } else {
            status = VisionConversationStatus.REVIEW_READY;
            nextAction = VisionNextAction.SHOW_REVIEW;
            agentState = VisionAgentState.REVIEW_READY;
            message = visionProperties.isExecutionEnabled()
                    ? "The circle review is ready. Confirm to create the circle, or say a different name to revise it."
                    : "The circle review is ready, but execution is still disabled.";
            String revisedCircleName = resolveCircleName(conversation, normalizedPrompt, understanding);
            if (revisedCircleName != null && !revisedCircleName.isBlank()) {
                conversation.getSlotData().put("circle_name", revisedCircleName);
                message = visionProperties.isExecutionEnabled()
                        ? "I updated the circle draft. Confirm when you want me to create it."
                        : "I updated the circle draft, but execution is still disabled.";
            }
        }

        conversation.setRequestedSlot(null);
        conversation.setStatus(status);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                VisionIntent.CREATE_CIRCLE,
                agentState,
                nextAction,
                null,
                understanding.isTranslationApplied(),
                understanding.isTranslationReliable(),
                message
        );
    }

    private VisionTurn handleCreateCircleRequestTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleCreateCircleRequestReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
        }

        String targetUserQuery = resolveCircleRequestTargetUser(conversation, normalizedPrompt, understanding);
        if (targetUserQuery == null || targetUserQuery.isBlank()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.CREATE_CIRCLE_REQUEST, "target_user", "Who should receive the circle request?");
        }
        VisionResolvedUserTarget target = visionCapabilityPreviewService.resolveCircleRequestRecipient(conversation.getOwner(), targetUserQuery);
        if (!target.resolved()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.CREATE_CIRCLE_REQUEST, "target_user", target.blockingMessage());
        }
        applyResolvedCircleRequestRecipient(conversation, targetUserQuery, target);
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = visionProperties.isExecutionEnabled()
                ? "I found the person. Confirm when you want me to send the circle request."
                : "I found the person, but execution is still disabled, so this phase stops at review.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.CREATE_CIRCLE_REQUEST,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleCreateCircleRequestReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String targetUserQuery = resolveCircleRequestTargetUser(conversation, normalizedPrompt, understanding);
        if (targetUserQuery != null && !targetUserQuery.isBlank()) {
            VisionResolvedUserTarget target = visionCapabilityPreviewService.resolveCircleRequestRecipient(conversation.getOwner(), targetUserQuery);
            if (!target.resolved()) {
                return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.CREATE_CIRCLE_REQUEST, "target_user", target.blockingMessage());
            }
            applyResolvedCircleRequestRecipient(conversation, targetUserQuery, target);
        }

        if (VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                String message = "The circle request review is ready, but execution is still disabled.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.CREATE_CIRCLE_REQUEST,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            VisionExecutionResult executionResult = visionExecutionService.execute(conversation);
            if (executionResult.isExecuted()) {
                if (executionResult.getCircleRequest() != null && executionResult.getCircleRequest().getId() != null) {
                    conversation.getSlotData().put("circle_request_id", executionResult.getCircleRequest().getId().toString());
                }
                conversation.getSlotData().put("conversation_outcome", "created_circle_request");
                conversation.setRequestedSlot(null);
                conversation.setStatus(VisionConversationStatus.COMPLETED);
                String message = "Circle request sent successfully.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.CREATE_CIRCLE_REQUEST,
                        VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            String message = executionResult.getBlockingReason();
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.CREATE_CIRCLE_REQUEST,
                    VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }

        String message = targetUserQuery != null
                ? "I updated the circle request target. Confirm when you want me to send it."
                : "The circle request review is ready. Confirm to send it, or say a different person to switch the target.";
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.CREATE_CIRCLE_REQUEST,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleAcceptCircleRequestTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleAcceptCircleRequestReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
        }
        String targetUserQuery = resolveCircleRequestTargetUser(conversation, normalizedPrompt, understanding);
        if (targetUserQuery == null || targetUserQuery.isBlank()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.ACCEPT_CIRCLE_REQUEST, "target_user", "Whose circle request should I accept?");
        }
        VisionResolvedCircleRequestTarget target = visionCapabilityPreviewService.resolveIncomingCircleRequest(conversation.getOwner(), targetUserQuery);
        if (!target.resolved()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.ACCEPT_CIRCLE_REQUEST, "target_user", target.blockingMessage());
        }
        applyResolvedCircleRequestTarget(conversation, targetUserQuery, target);
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = visionProperties.isExecutionEnabled()
                ? "I found the incoming circle request. Confirm when you want me to accept it."
                : "I found the incoming circle request, but execution is still disabled, so this phase stops at review.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.ACCEPT_CIRCLE_REQUEST,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleAcceptCircleRequestReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String targetUserQuery = resolveCircleRequestTargetUser(conversation, normalizedPrompt, understanding);
        if (targetUserQuery != null && !targetUserQuery.isBlank()) {
            VisionResolvedCircleRequestTarget target = visionCapabilityPreviewService.resolveIncomingCircleRequest(conversation.getOwner(), targetUserQuery);
            if (!target.resolved()) {
                return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.ACCEPT_CIRCLE_REQUEST, "target_user", target.blockingMessage());
            }
            applyResolvedCircleRequestTarget(conversation, targetUserQuery, target);
        }

        if (VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                String message = "The circle request acceptance review is ready, but execution is still disabled.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.ACCEPT_CIRCLE_REQUEST,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            VisionExecutionResult executionResult = visionExecutionService.execute(conversation);
            if (executionResult.isExecuted()) {
                if (executionResult.getCircleRequest() != null && executionResult.getCircleRequest().getId() != null) {
                    conversation.getSlotData().put("circle_request_id", executionResult.getCircleRequest().getId().toString());
                }
                conversation.getSlotData().put("conversation_outcome", "accepted_circle_request");
                conversation.setRequestedSlot(null);
                conversation.setStatus(VisionConversationStatus.COMPLETED);
                String message = "Circle request accepted successfully.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.ACCEPT_CIRCLE_REQUEST,
                        VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            String message = executionResult.getBlockingReason();
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.ACCEPT_CIRCLE_REQUEST,
                    VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }

        String message = targetUserQuery != null
                ? "I switched the circle request target. Confirm when you want me to accept it."
                : "The circle request acceptance review is ready. Confirm to accept it, or say a different person to switch the target.";
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.ACCEPT_CIRCLE_REQUEST,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleDeleteCircleRequestTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleDeleteCircleRequestReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
        }
        String targetUserQuery = resolveCircleRequestTargetUser(conversation, normalizedPrompt, understanding);
        if (targetUserQuery == null || targetUserQuery.isBlank()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.DELETE_CIRCLE_REQUEST, "target_user", "Whose pending circle request should I decline or cancel?");
        }
        VisionResolvedCircleRequestTarget target = visionCapabilityPreviewService.resolveAccessiblePendingCircleRequest(conversation.getOwner(), targetUserQuery);
        if (!target.resolved()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.DELETE_CIRCLE_REQUEST, "target_user", target.blockingMessage());
        }
        applyResolvedCircleRequestTarget(conversation, targetUserQuery, target);
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = visionProperties.isExecutionEnabled()
                ? "I found the pending circle request. Confirm when you want me to remove it."
                : "I found the pending circle request, but execution is still disabled, so this phase stops at review.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.DELETE_CIRCLE_REQUEST,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleDeleteCircleRequestReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String targetUserQuery = resolveCircleRequestTargetUser(conversation, normalizedPrompt, understanding);
        if (targetUserQuery != null && !targetUserQuery.isBlank()) {
            VisionResolvedCircleRequestTarget target = visionCapabilityPreviewService.resolveAccessiblePendingCircleRequest(conversation.getOwner(), targetUserQuery);
            if (!target.resolved()) {
                return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.DELETE_CIRCLE_REQUEST, "target_user", target.blockingMessage());
            }
            applyResolvedCircleRequestTarget(conversation, targetUserQuery, target);
        }

        if (VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                String message = "The circle request review is ready, but execution is still disabled.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.DELETE_CIRCLE_REQUEST,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            VisionExecutionResult executionResult = visionExecutionService.execute(conversation);
            if (executionResult.isExecuted()) {
                conversation.getSlotData().put("conversation_outcome", "deleted_circle_request");
                conversation.setRequestedSlot(null);
                conversation.setStatus(VisionConversationStatus.COMPLETED);
                String message = "Circle request removed successfully.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.DELETE_CIRCLE_REQUEST,
                        VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            String message = executionResult.getBlockingReason();
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.DELETE_CIRCLE_REQUEST,
                    VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }

        String message = targetUserQuery != null
                ? "I switched the pending circle request target. Confirm when you want me to remove it."
                : "The circle request review is ready. Confirm to remove it, or say a different person to switch the target.";
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.DELETE_CIRCLE_REQUEST,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleUpdateCircleTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleUpdateCircleReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
        }

        String circleQuery = visionDetailConversationTurnSupport.resolveCircleDetailQuery(conversation, normalizedPrompt, understanding);
        if (circleQuery != null && !circleQuery.isBlank()) {
            VisionResolvedCircleTarget target = visionCapabilityPreviewService.resolveOwnedCircle(conversation.getOwner(), circleQuery);
            if (!target.resolved()) {
                return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.UPDATE_CIRCLE, "target_circle_query", target.blockingMessage());
            }
            visionDetailConversationTurnSupport.applyResolvedCircleTarget(conversation, circleQuery, target);
        }

        String newCircleName = resolveCircleRename(conversation, normalizedPrompt, understanding);
        if (newCircleName != null && !newCircleName.isBlank()) {
            conversation.getSlotData().put("circle_name", newCircleName);
        }

        String missingSlot = visionClarificationService.nextMissingUpdateCircleSlot(conversation.getSlotData());
        if (missingSlot != null) {
            String message = missingSlot.equals(conversation.getRequestedSlot())
                    ? visionClarificationService.buildRetryQuestion(missingSlot)
                    : visionClarificationService.buildQuestion(missingSlot);
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.UPDATE_CIRCLE, missingSlot, message);
        }

        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = visionProperties.isExecutionEnabled()
                ? "I have enough to prepare a circle rename review. Confirm when you want me to save the new circle name."
                : "I have enough for a circle rename review. Execution is still disabled, so this phase stops at review.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_CIRCLE,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleUpdateCircleReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String circleQuery = visionDetailConversationTurnSupport.resolveCircleDetailQuery(conversation, normalizedPrompt, understanding);
        if (circleQuery != null && !circleQuery.isBlank()) {
            VisionResolvedCircleTarget target = visionCapabilityPreviewService.resolveOwnedCircle(conversation.getOwner(), circleQuery);
            if (!target.resolved()) {
                return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.UPDATE_CIRCLE, "target_circle_query", target.blockingMessage());
            }
            visionDetailConversationTurnSupport.applyResolvedCircleTarget(conversation, circleQuery, target);
        }
        String newCircleName = resolveCircleRename(conversation, normalizedPrompt, understanding);
        if (newCircleName != null && !newCircleName.isBlank()) {
            conversation.getSlotData().put("circle_name", newCircleName);
        }

        if (VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                updateConversationMetadata(conversation, prompt, normalizedPrompt, "The circle rename review is ready, but execution is still disabled.", understanding.isTranslationReliable());
                conversation.setStatus(VisionConversationStatus.REVIEW_READY);
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_CIRCLE,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(),
                        "The circle rename review is ready, but execution is still disabled.");
            }
            VisionExecutionResult executionResult = visionExecutionService.execute(conversation);
            if (executionResult.isExecuted()) {
                if (executionResult.getCreatedCircle() != null && executionResult.getCreatedCircle().getName() != null) {
                    conversation.getSlotData().put("resolved_circle_name", executionResult.getCreatedCircle().getName());
                }
                conversation.getSlotData().put("conversation_outcome", "updated_circle");
                conversation.setRequestedSlot(null);
                conversation.setStatus(VisionConversationStatus.COMPLETED);
                String message = "Circle updated successfully.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_CIRCLE,
                        VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            String message = executionResult.getBlockingReason();
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_CIRCLE,
                    VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }

        String message = (circleQuery != null || newCircleName != null)
                ? "I updated the circle rename draft. Confirm when you want me to save it."
                : "The circle rename review is ready. Confirm to save it, or revise the target circle or new name.";
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_CIRCLE,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleDeleteCircleTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleDeleteCircleReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
        }
        String circleQuery = visionDetailConversationTurnSupport.resolveCircleDetailQuery(conversation, normalizedPrompt, understanding);
        if (circleQuery == null || circleQuery.isBlank()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.DELETE_CIRCLE, "target_circle_query", "What circle should I delete? Say the circle name or circle id.");
        }
        VisionResolvedCircleTarget target = visionCapabilityPreviewService.resolveOwnedCircle(conversation.getOwner(), circleQuery);
        if (!target.resolved()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.DELETE_CIRCLE, "target_circle_query", target.blockingMessage());
        }
        visionDetailConversationTurnSupport.applyResolvedCircleTarget(conversation, circleQuery, target);
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = visionProperties.isExecutionEnabled()
                ? "I found the circle. Confirm when you want me to delete it."
                : "I found the circle, but execution is still disabled, so this phase stops at review.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.DELETE_CIRCLE,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleDeleteCircleReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String circleQuery = visionDetailConversationTurnSupport.resolveCircleDetailQuery(conversation, normalizedPrompt, understanding);
        if (circleQuery != null && !circleQuery.isBlank()) {
            VisionResolvedCircleTarget target = visionCapabilityPreviewService.resolveOwnedCircle(conversation.getOwner(), circleQuery);
            if (!target.resolved()) {
                return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.DELETE_CIRCLE, "target_circle_query", target.blockingMessage());
            }
            visionDetailConversationTurnSupport.applyResolvedCircleTarget(conversation, circleQuery, target);
        }

        if (VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                String message = "The circle deletion review is ready, but execution is still disabled.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.DELETE_CIRCLE,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            VisionExecutionResult executionResult = visionExecutionService.execute(conversation);
            if (executionResult.isExecuted()) {
                conversation.getSlotData().put("conversation_outcome", "deleted_circle");
                conversation.setRequestedSlot(null);
                conversation.setStatus(VisionConversationStatus.COMPLETED);
                String message = "Circle deleted successfully.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.DELETE_CIRCLE,
                        VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            String message = executionResult.getBlockingReason();
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.DELETE_CIRCLE,
                    VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }

        String message = circleQuery != null
                ? "I switched the circle deletion target. Confirm when you want me to delete it."
                : "The circle deletion review is ready. Confirm to delete it, or say a different circle to switch the target.";
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.DELETE_CIRCLE,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleCreateApplicationTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleCreateApplicationReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
        }

        String questQuery = resolveApplicationQuestQuery(conversation, normalizedPrompt, understanding);
        if (questQuery != null && !questQuery.isBlank()) {
            conversation.getSlotData().put("target_quest_query", questQuery);
            VisionResolvedQuestTarget target = visionCapabilityPreviewService.resolveApplicationQuest(conversation.getOwner(), questQuery);
            if (!target.resolved()) {
                conversation.setRequestedSlot("target_quest_query");
                conversation.setStatus(VisionConversationStatus.ACTIVE);
                String message = target.blockingMessage();
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(
                        conversation,
                        source,
                        prompt,
                        normalizedPrompt,
                        VisionIntent.CREATE_APPLICATION,
                        VisionAgentState.ASKING,
                        VisionNextAction.ASK_FOR_SLOT,
                        "target_quest_query",
                        understanding.isTranslationApplied(),
                        understanding.isTranslationReliable(),
                        message
                );
            }
            conversation.getSlotData().put("application_quest_id", target.questId().toString());
            conversation.getSlotData().put("application_quest_title", target.questTitle());
            conversation.getSlotData().put("application_quest_creator", target.creatorUsername());
            conversation.getSlotData().put("application_price_required", Boolean.toString(target.priceRequired()));
            conversation.getSlotData().put("application_quest_reward_label", target.rewardLabel());
        }

        String applicationMessage = resolveApplicationMessage(conversation, normalizedPrompt, understanding);
        if (applicationMessage != null && !applicationMessage.isBlank()) {
            conversation.getSlotData().put("application_message", applicationMessage);
        }

        String proposedPrice = resolveApplicationProposedPrice(conversation, normalizedPrompt, understanding);
        if (proposedPrice != null && !proposedPrice.isBlank()) {
            conversation.getSlotData().put("application_proposed_price", proposedPrice);
        }

        String missingSlot = visionClarificationService.nextMissingCreateApplicationSlot(conversation.getSlotData());
        if (missingSlot != null) {
            conversation.setRequestedSlot(missingSlot);
            conversation.setStatus(VisionConversationStatus.ACTIVE);
            String message = missingSlot.equals(conversation.getRequestedSlot())
                    ? visionClarificationService.buildRetryQuestion(missingSlot)
                    : visionClarificationService.buildQuestion(missingSlot);
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(
                    conversation,
                    source,
                    prompt,
                    normalizedPrompt,
                    VisionIntent.CREATE_APPLICATION,
                    VisionAgentState.ASKING,
                    VisionNextAction.ASK_FOR_SLOT,
                    missingSlot,
                    understanding.isTranslationApplied(),
                    understanding.isTranslationReliable(),
                    message
            );
        }

        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = visionProperties.isExecutionEnabled()
                ? "I have enough to prepare an application review. Confirm when you want me to send this application."
                : "I have enough for an application review. Execution is still disabled, so this phase stops at review.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                VisionIntent.CREATE_APPLICATION,
                VisionAgentState.REVIEW_READY,
                VisionNextAction.SHOW_REVIEW,
                null,
                understanding.isTranslationApplied(),
                understanding.isTranslationReliable(),
                message
        );
    }

    private VisionTurn handleCreateApplicationReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String questQuery = resolveApplicationQuestQuery(conversation, normalizedPrompt, understanding);
        if (questQuery != null && !questQuery.isBlank()) {
            VisionResolvedQuestTarget target = visionCapabilityPreviewService.resolveApplicationQuest(conversation.getOwner(), questQuery);
            if (!target.resolved()) {
                conversation.setRequestedSlot("target_quest_query");
                conversation.setStatus(VisionConversationStatus.ACTIVE);
                String message = target.blockingMessage();
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(
                        conversation,
                        source,
                        prompt,
                        normalizedPrompt,
                        VisionIntent.CREATE_APPLICATION,
                        VisionAgentState.ASKING,
                        VisionNextAction.ASK_FOR_SLOT,
                        "target_quest_query",
                        understanding.isTranslationApplied(),
                        understanding.isTranslationReliable(),
                        message
                );
            }
            conversation.getSlotData().put("target_quest_query", questQuery);
            conversation.getSlotData().put("application_quest_id", target.questId().toString());
            conversation.getSlotData().put("application_quest_title", target.questTitle());
            conversation.getSlotData().put("application_quest_creator", target.creatorUsername());
            conversation.getSlotData().put("application_price_required", Boolean.toString(target.priceRequired()));
            conversation.getSlotData().put("application_quest_reward_label", target.rewardLabel());
        }

        String revisedMessage = resolveApplicationMessage(conversation, normalizedPrompt, understanding);
        if (revisedMessage != null && !revisedMessage.isBlank()) {
            conversation.getSlotData().put("application_message", revisedMessage);
        }
        String revisedPrice = resolveApplicationProposedPrice(conversation, normalizedPrompt, understanding);
        if (revisedPrice != null && !revisedPrice.isBlank()) {
            conversation.getSlotData().put("application_proposed_price", revisedPrice);
        }

        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;

        if (VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                status = VisionConversationStatus.REVIEW_READY;
                nextAction = VisionNextAction.SHOW_REVIEW;
                agentState = VisionAgentState.REVIEW_READY;
                message = "The application review is ready, but execution is still disabled.";
            } else {
                VisionExecutionResult executionResult = visionExecutionService.execute(conversation);
                if (executionResult.isExecuted()) {
                    if (executionResult.getApplication() != null && executionResult.getApplication().getId() != null) {
                        conversation.getSlotData().put("created_application_id", executionResult.getApplication().getId().toString());
                    }
                    conversation.getSlotData().put("conversation_outcome", "created_application");
                    status = VisionConversationStatus.COMPLETED;
                    nextAction = VisionNextAction.COMPLETE;
                    agentState = VisionAgentState.COMPLETE;
                    message = "Application sent successfully.";
                } else {
                    status = VisionConversationStatus.REVIEW_READY;
                    nextAction = VisionNextAction.SHOW_REVIEW;
                    agentState = VisionAgentState.REVIEW_READY;
                    message = executionResult.getBlockingReason();
                }
            }
        } else {
            status = VisionConversationStatus.REVIEW_READY;
            nextAction = VisionNextAction.SHOW_REVIEW;
            agentState = VisionAgentState.REVIEW_READY;
            message = visionProperties.isExecutionEnabled()
                    ? "The application review is ready. Confirm to send it, or revise the quest, message, or price."
                    : "The application review is ready, but execution is still disabled.";
            if (questQuery != null || revisedMessage != null || revisedPrice != null) {
                message = visionProperties.isExecutionEnabled()
                        ? "I updated the application draft. Confirm when you want me to send it."
                        : "I updated the application draft, but execution is still disabled.";
            }
        }

        conversation.setRequestedSlot(null);
        conversation.setStatus(status);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                VisionIntent.CREATE_APPLICATION,
                agentState,
                nextAction,
                null,
                understanding.isTranslationApplied(),
                understanding.isTranslationReliable(),
                message
        );
    }

    private VisionTurn handleUpdateApplicationTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleUpdateApplicationReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
        }

        String questQuery = resolveApplicationQuestQuery(conversation, normalizedPrompt, understanding);
        if (questQuery != null && !questQuery.isBlank()) {
            VisionResolvedApplicationTarget target = visionCapabilityPreviewService.resolveMyPendingApplication(
                    conversation.getOwner(),
                    questQuery,
                    ApplicationAllowedActionDTO.EDIT
            );
            if (!target.resolved()) {
                conversation.setRequestedSlot("target_quest_query");
                conversation.setStatus(VisionConversationStatus.ACTIVE);
                String message = target.blockingMessage();
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(
                        conversation,
                        source,
                        prompt,
                        normalizedPrompt,
                        VisionIntent.UPDATE_APPLICATION,
                        VisionAgentState.ASKING,
                        VisionNextAction.ASK_FOR_SLOT,
                        "target_quest_query",
                        understanding.isTranslationApplied(),
                        understanding.isTranslationReliable(),
                        message
                );
            }
            visionDetailConversationTurnSupport.applyResolvedApplicationTarget(conversation, questQuery, target);
        }

        String revisedMessage = resolveApplicationMessage(conversation, normalizedPrompt, understanding);
        if (revisedMessage != null && !revisedMessage.isBlank()) {
            conversation.getSlotData().put("application_message", revisedMessage);
        }
        String revisedPrice = resolveApplicationProposedPrice(conversation, normalizedPrompt, understanding);
        if (revisedPrice != null && !revisedPrice.isBlank()) {
            conversation.getSlotData().put("application_proposed_price", revisedPrice);
        }

        String missingSlot = nextMissingUpdateApplicationSlot(conversation);
        if (missingSlot != null) {
            conversation.setRequestedSlot(missingSlot);
            conversation.setStatus(VisionConversationStatus.ACTIVE);
            String message = missingSlot.equals(conversation.getRequestedSlot())
                    ? retryQuestionForUpdateApplication(missingSlot)
                    : questionForUpdateApplication(missingSlot);
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(
                    conversation,
                    source,
                    prompt,
                    normalizedPrompt,
                    VisionIntent.UPDATE_APPLICATION,
                    VisionAgentState.ASKING,
                    VisionNextAction.ASK_FOR_SLOT,
                    missingSlot,
                    understanding.isTranslationApplied(),
                    understanding.isTranslationReliable(),
                    message
            );
        }

        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = visionProperties.isExecutionEnabled()
                ? "I have enough to prepare an application update review. Confirm when you want me to save these changes."
                : "I have enough for an application update review. Execution is still disabled, so this phase stops at review.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                VisionIntent.UPDATE_APPLICATION,
                VisionAgentState.REVIEW_READY,
                VisionNextAction.SHOW_REVIEW,
                null,
                understanding.isTranslationApplied(),
                understanding.isTranslationReliable(),
                message
        );
    }

    private VisionTurn handleUpdateApplicationReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String questQuery = resolveApplicationQuestQuery(conversation, normalizedPrompt, understanding);
        if (questQuery != null && !questQuery.isBlank()) {
            VisionResolvedApplicationTarget target = visionCapabilityPreviewService.resolveMyPendingApplication(
                    conversation.getOwner(),
                    questQuery,
                    ApplicationAllowedActionDTO.EDIT
            );
            if (!target.resolved()) {
                conversation.setRequestedSlot("target_quest_query");
                conversation.setStatus(VisionConversationStatus.ACTIVE);
                String message = target.blockingMessage();
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(
                        conversation,
                        source,
                        prompt,
                        normalizedPrompt,
                        VisionIntent.UPDATE_APPLICATION,
                        VisionAgentState.ASKING,
                        VisionNextAction.ASK_FOR_SLOT,
                        "target_quest_query",
                        understanding.isTranslationApplied(),
                        understanding.isTranslationReliable(),
                        message
                );
            }
            visionDetailConversationTurnSupport.applyResolvedApplicationTarget(conversation, questQuery, target);
        }

        String revisedMessage = resolveApplicationMessage(conversation, normalizedPrompt, understanding);
        if (revisedMessage != null && !revisedMessage.isBlank()) {
            conversation.getSlotData().put("application_message", revisedMessage);
        }
        String revisedPrice = resolveApplicationProposedPrice(conversation, normalizedPrompt, understanding);
        if (revisedPrice != null && !revisedPrice.isBlank()) {
            conversation.getSlotData().put("application_proposed_price", revisedPrice);
        }

        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;

        if (VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                status = VisionConversationStatus.REVIEW_READY;
                nextAction = VisionNextAction.SHOW_REVIEW;
                agentState = VisionAgentState.REVIEW_READY;
                message = "The application update review is ready, but execution is still disabled.";
            } else {
                VisionExecutionResult executionResult = visionExecutionService.execute(conversation);
                if (executionResult.isExecuted()) {
                    var updatedApplication = executionResult.getApplication();
                    if (updatedApplication != null) {
                        conversation.getSlotData().put("application_existing_message", updatedApplication.getMessage());
                        conversation.getSlotData().put("application_existing_proposed_price",
                                updatedApplication.getProposedPrice() == null ? "" : updatedApplication.getProposedPrice().stripTrailingZeros().toPlainString());
                        conversation.getSlotData().put("application_message", updatedApplication.getMessage());
                        conversation.getSlotData().put("application_proposed_price",
                                updatedApplication.getProposedPrice() == null ? "" : updatedApplication.getProposedPrice().stripTrailingZeros().toPlainString());
                        if (updatedApplication.getId() != null) {
                            conversation.getSlotData().put("updated_application_id", updatedApplication.getId().toString());
                        }
                    }
                    conversation.getSlotData().put("conversation_outcome", "updated_application");
                    status = VisionConversationStatus.COMPLETED;
                    nextAction = VisionNextAction.COMPLETE;
                    agentState = VisionAgentState.COMPLETE;
                    message = "Application updated successfully.";
                } else {
                    status = VisionConversationStatus.REVIEW_READY;
                    nextAction = VisionNextAction.SHOW_REVIEW;
                    agentState = VisionAgentState.REVIEW_READY;
                    message = executionResult.getBlockingReason();
                }
            }
        } else {
            status = VisionConversationStatus.REVIEW_READY;
            nextAction = VisionNextAction.SHOW_REVIEW;
            agentState = VisionAgentState.REVIEW_READY;
            message = visionProperties.isExecutionEnabled()
                    ? "The application update review is ready. Confirm to save it, or revise the quest, message, or price."
                    : "The application update review is ready, but execution is still disabled.";
            if (questQuery != null || revisedMessage != null || revisedPrice != null) {
                message = visionProperties.isExecutionEnabled()
                        ? "I updated the application change draft. Confirm when you want me to save it."
                        : "I updated the application change draft, but execution is still disabled.";
            }
        }

        conversation.setRequestedSlot(null);
        conversation.setStatus(status);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                VisionIntent.UPDATE_APPLICATION,
                agentState,
                nextAction,
                null,
                understanding.isTranslationApplied(),
                understanding.isTranslationReliable(),
                message
        );
    }

    private VisionTurn handleWithdrawApplicationTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleWithdrawApplicationReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
        }

        String questQuery = resolveApplicationQuestQuery(conversation, normalizedPrompt, understanding);
        if (questQuery == null || questQuery.isBlank()) {
            conversation.setRequestedSlot("target_quest_query");
            conversation.setStatus(VisionConversationStatus.ACTIVE);
            String message = "What application should I withdraw? Say the quest title or quest id.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(
                    conversation,
                    source,
                    prompt,
                    normalizedPrompt,
                    VisionIntent.WITHDRAW_APPLICATION,
                    VisionAgentState.ASKING,
                    VisionNextAction.ASK_FOR_SLOT,
                    "target_quest_query",
                    understanding.isTranslationApplied(),
                    understanding.isTranslationReliable(),
                    message
            );
        }

        VisionResolvedApplicationTarget target = visionCapabilityPreviewService.resolveMyPendingApplication(
                conversation.getOwner(),
                questQuery,
                ApplicationAllowedActionDTO.WITHDRAW
        );
        if (!target.resolved()) {
            conversation.setRequestedSlot("target_quest_query");
            conversation.setStatus(VisionConversationStatus.ACTIVE);
            String message = target.blockingMessage();
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(
                    conversation,
                    source,
                    prompt,
                    normalizedPrompt,
                    VisionIntent.WITHDRAW_APPLICATION,
                    VisionAgentState.ASKING,
                    VisionNextAction.ASK_FOR_SLOT,
                    "target_quest_query",
                    understanding.isTranslationApplied(),
                    understanding.isTranslationReliable(),
                    message
            );
        }

        visionDetailConversationTurnSupport.applyResolvedApplicationTarget(conversation, questQuery, target);
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = visionProperties.isExecutionEnabled()
                ? "I found the pending application. Confirm when you want me to withdraw it."
                : "I found the pending application, but execution is still disabled, so this phase stops at review.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                VisionIntent.WITHDRAW_APPLICATION,
                VisionAgentState.REVIEW_READY,
                VisionNextAction.SHOW_REVIEW,
                null,
                understanding.isTranslationApplied(),
                understanding.isTranslationReliable(),
                message
        );
    }

    private VisionTurn handleWithdrawApplicationReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String questQuery = resolveApplicationQuestQuery(conversation, normalizedPrompt, understanding);
        if (questQuery != null && !questQuery.isBlank()) {
            VisionResolvedApplicationTarget target = visionCapabilityPreviewService.resolveMyPendingApplication(
                    conversation.getOwner(),
                    questQuery,
                    ApplicationAllowedActionDTO.WITHDRAW
            );
            if (!target.resolved()) {
                conversation.setRequestedSlot("target_quest_query");
                conversation.setStatus(VisionConversationStatus.ACTIVE);
                String message = target.blockingMessage();
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(
                        conversation,
                        source,
                        prompt,
                        normalizedPrompt,
                        VisionIntent.WITHDRAW_APPLICATION,
                        VisionAgentState.ASKING,
                        VisionNextAction.ASK_FOR_SLOT,
                        "target_quest_query",
                        understanding.isTranslationApplied(),
                        understanding.isTranslationReliable(),
                        message
                );
            }
            visionDetailConversationTurnSupport.applyResolvedApplicationTarget(conversation, questQuery, target);
        }

        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;

        if (VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                status = VisionConversationStatus.REVIEW_READY;
                nextAction = VisionNextAction.SHOW_REVIEW;
                agentState = VisionAgentState.REVIEW_READY;
                message = "The application withdrawal review is ready, but execution is still disabled.";
            } else {
                VisionExecutionResult executionResult = visionExecutionService.execute(conversation);
                if (executionResult.isExecuted()) {
                    if (executionResult.getApplication() != null && executionResult.getApplication().getId() != null) {
                        conversation.getSlotData().put("withdrawn_application_id", executionResult.getApplication().getId().toString());
                    }
                    conversation.getSlotData().put("conversation_outcome", "withdrawn_application");
                    status = VisionConversationStatus.COMPLETED;
                    nextAction = VisionNextAction.COMPLETE;
                    agentState = VisionAgentState.COMPLETE;
                    message = "Application withdrawn successfully.";
                } else {
                    status = VisionConversationStatus.REVIEW_READY;
                    nextAction = VisionNextAction.SHOW_REVIEW;
                    agentState = VisionAgentState.REVIEW_READY;
                    message = executionResult.getBlockingReason();
                }
            }
        } else {
            status = VisionConversationStatus.REVIEW_READY;
            nextAction = VisionNextAction.SHOW_REVIEW;
            agentState = VisionAgentState.REVIEW_READY;
            message = visionProperties.isExecutionEnabled()
                    ? "The application withdrawal review is ready. Confirm to withdraw it, or say a different quest to switch the target."
                    : "The application withdrawal review is ready, but execution is still disabled.";
            if (questQuery != null) {
                message = visionProperties.isExecutionEnabled()
                        ? "I switched the application withdrawal target. Confirm when you want me to withdraw it."
                        : "I switched the application withdrawal target, but execution is still disabled.";
            }
        }

        conversation.setRequestedSlot(null);
        conversation.setStatus(status);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                VisionIntent.WITHDRAW_APPLICATION,
                agentState,
                nextAction,
                null,
                understanding.isTranslationApplied(),
                understanding.isTranslationReliable(),
                message
        );
    }

    private VisionTurn handleApproveApplicationTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleManagedApplicationDecisionTurn(
                conversation, prompt, normalizedPrompt, understanding, source,
                VisionIntent.APPROVE_APPLICATION, ApplicationAllowedActionDTO.APPROVE,
                "I found the pending application. Confirm when you want me to approve it."
        );
    }

    private VisionTurn handleApproveApplicationReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleManagedApplicationDecisionReviewTurn(
                conversation, prompt, normalizedPrompt, understanding, source,
                VisionIntent.APPROVE_APPLICATION, ApplicationAllowedActionDTO.APPROVE, true
        );
    }

    private VisionTurn handleDeclineApplicationTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleManagedApplicationDecisionTurn(
                conversation, prompt, normalizedPrompt, understanding, source,
                VisionIntent.DECLINE_APPLICATION, ApplicationAllowedActionDTO.DECLINE,
                "I found the pending application. Confirm when you want me to decline it."
        );
    }

    private VisionTurn handleDeclineApplicationReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleManagedApplicationDecisionReviewTurn(
                conversation, prompt, normalizedPrompt, understanding, source,
                VisionIntent.DECLINE_APPLICATION, ApplicationAllowedActionDTO.DECLINE, false
        );
    }

    private VisionTurn handleManagedApplicationDecisionTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source,
            VisionIntent intent,
            ApplicationAllowedActionDTO requiredAction,
            String readyMessage
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleManagedApplicationDecisionReviewTurn(conversation, prompt, normalizedPrompt, understanding, source, intent, requiredAction, intent == VisionIntent.APPROVE_APPLICATION);
        }

        String questQuery = resolveApplicationQuestQuery(conversation, normalizedPrompt, understanding);
        if (questQuery != null && !questQuery.isBlank()) {
            conversation.getSlotData().put("target_quest_query", questQuery);
        }
        String applicantQuery = resolveManagedApplicantQuery(conversation, normalizedPrompt, understanding);
        if (applicantQuery != null && !applicantQuery.isBlank()) {
            conversation.getSlotData().put("target_user", applicantQuery);
        }

        String missingSlot = visionClarificationService.nextMissingApproveDeclineApplicationSlot(conversation.getSlotData());
        if (missingSlot != null) {
            String message = missingSlot.equals(conversation.getRequestedSlot())
                    ? visionClarificationService.buildRetryQuestion(missingSlot)
                    : (missingSlot.equals("target_user")
                    ? "Who is the applicant? Say the applicant username."
                    : "What quest should I use? Say the quest title or quest id.");
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, intent, missingSlot, message);
        }

        VisionResolvedManagedApplicationTarget target = visionCapabilityPreviewService.resolveManagedPendingApplication(
                conversation.getOwner(),
                conversation.getSlotData().get("target_quest_query"),
                conversation.getSlotData().get("target_user"),
                requiredAction
        );
        if (!target.resolved()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, intent, "target_user", target.blockingMessage());
        }
        applyResolvedManagedApplicationTarget(conversation, target);
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = visionProperties.isExecutionEnabled()
                ? readyMessage
                : "The application decision review is ready, but execution is still disabled.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, intent,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleManagedApplicationDecisionReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source,
            VisionIntent intent,
            ApplicationAllowedActionDTO requiredAction,
            boolean approve
    ) {
        String questQuery = resolveApplicationQuestQuery(conversation, normalizedPrompt, understanding);
        if (questQuery != null && !questQuery.isBlank()) {
            conversation.getSlotData().put("target_quest_query", questQuery);
        }
        String applicantQuery = resolveManagedApplicantQuery(conversation, normalizedPrompt, understanding);
        if (applicantQuery != null && !applicantQuery.isBlank()) {
            conversation.getSlotData().put("target_user", applicantQuery);
        }
        VisionResolvedManagedApplicationTarget target = visionCapabilityPreviewService.resolveManagedPendingApplication(
                conversation.getOwner(),
                conversation.getSlotData().get("target_quest_query"),
                conversation.getSlotData().get("target_user"),
                requiredAction
        );
        if (!target.resolved()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, intent, "target_user", target.blockingMessage());
        }
        applyResolvedManagedApplicationTarget(conversation, target);

        if (VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                String message = "The application decision review is ready, but execution is still disabled.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, intent,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            VisionExecutionResult executionResult = visionExecutionService.execute(conversation);
            if (!executionResult.isExecuted()) {
                String message = executionResult.getBlockingReason();
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, intent,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            if (approve) {
                conversation.getSlotData().put("conversation_outcome", "approved_application");
            } else {
                conversation.getSlotData().put("conversation_outcome", "declined_application");
            }
            conversation.setRequestedSlot(null);
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            String message = approve ? "Application approved successfully." : "Application declined successfully.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, intent,
                    VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }

        String message = (questQuery != null || applicantQuery != null)
                ? "I updated the application decision target. Confirm when you want me to continue."
                : approve
                ? "The application approval review is ready. Confirm to approve it, or revise the quest or applicant."
                : "The application decline review is ready. Confirm to decline it, or revise the quest or applicant.";
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, intent,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleUpdateProfileTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleUpdateProfileReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
        }

        String profileUsername = resolveProfileUsername(conversation, normalizedPrompt, understanding);
        String profileDescription = resolveProfileDescription(conversation, normalizedPrompt, understanding);
        if (profileUsername != null && !profileUsername.isBlank()) {
            conversation.getSlotData().put("profile_username", profileUsername);
        }
        if (profileDescription != null) {
            conversation.getSlotData().put("profile_description", profileDescription);
        }

        if (!hasProfileUpdateDraft(conversation)) {
            conversation.setRequestedSlot("profile_username");
            conversation.setStatus(VisionConversationStatus.ACTIVE);
            String message = "What username should I use? You can also say 'set description to ...' if you only want to update the profile description.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(
                    conversation,
                    source,
                    prompt,
                    normalizedPrompt,
                    VisionIntent.UPDATE_PROFILE,
                    VisionAgentState.ASKING,
                    VisionNextAction.ASK_FOR_SLOT,
                    "profile_username",
                    understanding.isTranslationApplied(),
                    understanding.isTranslationReliable(),
                    message
            );
        }

        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = visionProperties.isExecutionEnabled()
                ? "I have enough to prepare a profile review. Confirm when you want me to save these profile changes."
                : "I have enough for a profile review. Execution is still disabled, so this phase stops at review.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                VisionIntent.UPDATE_PROFILE,
                VisionAgentState.REVIEW_READY,
                VisionNextAction.SHOW_REVIEW,
                null,
                understanding.isTranslationApplied(),
                understanding.isTranslationReliable(),
                message
        );
    }

    private VisionTurn handleUpdateProfileLocationTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleUpdateProfileLocationReviewTurn(conversation, prompt, normalizedPrompt, understanding, source);
        }

        String locationMode = resolveProfileLocationMode(conversation, normalizedPrompt, understanding);
        if (locationMode != null) {
            conversation.getSlotData().put("profile_location_mode", locationMode);
        }
        String locationLabel = resolveProfileLocationLabel(conversation, normalizedPrompt, understanding);
        if (locationLabel != null) {
            conversation.getSlotData().put("profile_location_label", locationLabel);
        }

        String missingSlot = nextMissingProfileLocationSlot(conversation);
        if (missingSlot != null) {
            String message = missingSlot.equals(conversation.getRequestedSlot())
                    ? visionClarificationService.buildRetryQuestion(missingSlot)
                    : visionClarificationService.buildQuestion(missingSlot);
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.UPDATE_PROFILE_LOCATION, missingSlot, message);
        }

        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = visionProperties.isExecutionEnabled()
                ? "I have enough to prepare a profile location review. Confirm when you want me to save these location changes."
                : "I have enough for a profile location review. Execution is still disabled, so this phase stops at review.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_PROFILE_LOCATION,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleUpdateProfileLocationReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String locationMode = resolveProfileLocationMode(conversation, normalizedPrompt, understanding);
        if (locationMode != null) {
            conversation.getSlotData().put("profile_location_mode", locationMode);
        }
        String locationLabel = resolveProfileLocationLabel(conversation, normalizedPrompt, understanding);
        if (locationLabel != null) {
            conversation.getSlotData().put("profile_location_label", locationLabel);
        }

        if (VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                String message = "The profile location review is ready, but execution is still disabled.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_PROFILE_LOCATION,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            VisionExecutionResult executionResult = visionExecutionService.execute(conversation);
            if (executionResult.isExecuted()) {
                var updatedProfile = executionResult.getUpdatedProfile();
                if (updatedProfile != null && updatedProfile.getLocationSettings() != null) {
                    conversation.getSlotData().put("profile_location_mode",
                            updatedProfile.getLocationSettings().getMode() == null ? "" : updatedProfile.getLocationSettings().getMode().name());
                    conversation.getSlotData().put("profile_location_label",
                            updatedProfile.getLocationSettings().getLabel() == null ? "" : updatedProfile.getLocationSettings().getLabel());
                }
                conversation.getSlotData().put("conversation_outcome", "updated_profile_location");
                conversation.setRequestedSlot(null);
                conversation.setStatus(VisionConversationStatus.COMPLETED);
                String message = "Profile location updated successfully.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_PROFILE_LOCATION,
                        VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            String message = executionResult.getBlockingReason();
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_PROFILE_LOCATION,
                    VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }

        String message = (locationMode != null || locationLabel != null)
                ? "I updated the profile location draft. Confirm when you want me to save it."
                : "The profile location review is ready. Confirm to save it, or revise the mode or location.";
        conversation.setRequestedSlot(null);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_PROFILE_LOCATION,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleUpdateProfileReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String revisedUsername = resolveProfileUsername(conversation, normalizedPrompt, understanding);
        String revisedDescription = resolveProfileDescription(conversation, normalizedPrompt, understanding);
        if (revisedUsername != null && !revisedUsername.isBlank()) {
            conversation.getSlotData().put("profile_username", revisedUsername);
        }
        if (revisedDescription != null) {
            conversation.getSlotData().put("profile_description", revisedDescription);
        }

        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;

        if (VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                status = VisionConversationStatus.REVIEW_READY;
                nextAction = VisionNextAction.SHOW_REVIEW;
                agentState = VisionAgentState.REVIEW_READY;
                message = "The profile review is ready, but execution is still disabled.";
            } else {
                VisionExecutionResult executionResult = visionExecutionService.execute(conversation);
                if (executionResult.isExecuted()) {
                    var updatedProfile = executionResult.getUpdatedProfile();
                    if (updatedProfile != null) {
                        conversation.getSlotData().put("profile_username", updatedProfile.getUsername());
                        conversation.getSlotData().put("profile_description",
                                updatedProfile.getProfileDescription() == null ? "" : updatedProfile.getProfileDescription());
                    }
                    conversation.getSlotData().put("conversation_outcome", "updated_profile");
                    status = VisionConversationStatus.COMPLETED;
                    nextAction = VisionNextAction.COMPLETE;
                    agentState = VisionAgentState.COMPLETE;
                    message = "Profile updated successfully.";
                } else {
                    status = VisionConversationStatus.REVIEW_READY;
                    nextAction = VisionNextAction.SHOW_REVIEW;
                    agentState = VisionAgentState.REVIEW_READY;
                    message = executionResult.getBlockingReason();
                }
            }
        } else {
            status = VisionConversationStatus.REVIEW_READY;
            nextAction = VisionNextAction.SHOW_REVIEW;
            agentState = VisionAgentState.REVIEW_READY;
            message = visionProperties.isExecutionEnabled()
                    ? "The profile review is ready. Confirm to save the profile changes, or say a different username or description to revise the draft."
                    : "The profile review is ready, but execution is still disabled.";
            if (revisedUsername != null || revisedDescription != null) {
                message = visionProperties.isExecutionEnabled()
                        ? "I updated the profile draft. Confirm when you want me to save these profile changes."
                        : "I updated the profile draft, but execution is still disabled.";
            }
        }

        conversation.setRequestedSlot(null);
        conversation.setStatus(status);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                VisionIntent.UPDATE_PROFILE,
                agentState,
                nextAction,
                null,
                understanding.isTranslationApplied(),
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
        String message = "This vision backend currently supports quest creation, quest news, circles, circle requests, applications, profile updates, quest discovery, chat, profile, circles, and applications.";
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

    private VisionTurn handleSearchTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        VisionSearchDiscoveryDTO discovery = visionSearchDiscoveryService.discover(conversation, understanding, conversation.getOwner());
        if (discovery == null) {
            throw ServiceErrors.conflict("Search is not available for this vision conversation");
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
        turn.setDetectedIntent(VisionIntent.SEARCH);
        turn.setAgentState(VisionAgentState.RECOMMENDING);
        turn.setNextAction(VisionNextAction.SHOW_RESULTS);
        turn.setRequestedSlot(null);
        turn.setTranslationApplied(understanding.isTranslationApplied());
        turn.setTranslationReliable(understanding.isTranslationReliable());
        turn.setAssistantMessage(message);
        return visionTurnRepository.save(turn);
    }

    private VisionSearchDiscoveryDTO searchDiscoveryForConversation(VisionConversation conversation, AppUser currentUser) {
        if (conversation == null || conversation.getIntent() != VisionIntent.SEARCH) {
            return null;
        }
        return visionSearchDiscoveryService.discover(conversation, VisionPromptUnderstandingResult.empty(""), currentUser);
    }

    private VisionTurn handleOpenChatTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String selectedCandidate = resolvePendingChatCandidateSelection(conversation, normalizedPrompt);
        VisionSemanticPlan semanticPlan = selectedCandidate == null
                ? understanding.semanticPlanOrEmpty()
                : VisionSemanticPlan.openChat(
                        understanding.semanticPlanOrEmpty().getCandidateIntentConfidence() == null
                                ? 0.85d
                                : understanding.semanticPlanOrEmpty().getCandidateIntentConfidence(),
                        understanding.semanticPlanOrEmpty().getPlanningNote(),
                        selectedCandidate
                );
        VisionChatExecutionResult result = visionChatExecutionService.openChat(
                conversation.getOwner(),
                normalizedPrompt,
                semanticPlan
        );

        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;

        if (result.isExecuted()) {
            clearPendingChatCandidates(conversation);
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
            storePendingChatCandidates(conversation, result);
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

    private String resolvePendingChatCandidateSelection(VisionConversation conversation, String normalizedPrompt) {
        if (conversation == null || conversation.getIntent() != VisionIntent.OPEN_CHAT || normalizedPrompt == null) {
            return null;
        }
        String countValue = conversation.getSlotData().get("pending_chat_candidate_count");
        if (countValue == null || countValue.isBlank()) {
            return null;
        }
        String lower = normalizedPrompt.trim().toLowerCase(Locale.ROOT);
        int selectedIndex = switch (lower) {
            case "1", "candidate 1", "first", "first one" -> 1;
            case "2", "candidate 2", "second", "second one" -> 2;
            case "3", "candidate 3", "third", "third one" -> 3;
            default -> 0;
        };
        if (selectedIndex <= 0) {
            return null;
        }
        return conversation.getSlotData().get("pending_chat_candidate_" + selectedIndex + "_value");
    }

    private void storePendingChatCandidates(VisionConversation conversation, VisionChatExecutionResult result) {
        clearPendingChatCandidates(conversation);
        if (result == null || result.getCandidates() == null || result.getCandidates().isEmpty()) {
            return;
        }
        conversation.getSlotData().put("pending_chat_candidate_count", Integer.toString(result.getCandidates().size()));
        for (int index = 0; index < result.getCandidates().size(); index++) {
            VisionChatTargetCandidate candidate = result.getCandidates().get(index);
            String prefix = "pending_chat_candidate_" + (index + 1);
            if (candidate.getValue() != null && !candidate.getValue().isBlank()) {
                conversation.getSlotData().put(prefix + "_value", candidate.getValue());
            }
            if (candidate.getLabel() != null && !candidate.getLabel().isBlank()) {
                conversation.getSlotData().put(prefix + "_label", candidate.getLabel());
            }
        }
    }

    private void clearPendingChatCandidates(VisionConversation conversation) {
        if (conversation == null || conversation.getSlotData() == null) {
            return;
        }
        String rawCount = conversation.getSlotData().remove("pending_chat_candidate_count");
        int count = 0;
        try {
            count = rawCount == null ? 0 : Integer.parseInt(rawCount);
        } catch (NumberFormatException ignored) {
            count = 0;
        }
        for (int index = 1; index <= Math.max(count, 3); index++) {
            String prefix = "pending_chat_candidate_" + index;
            conversation.getSlotData().remove(prefix + "_value");
            conversation.getSlotData().remove(prefix + "_label");
        }
    }

    private VisionTurn handleViewUserProfileTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String profileQuery = visionDetailConversationTurnSupport.resolveUserProfileQuery(conversation, normalizedPrompt, understanding);
        if (!hasText(profileQuery) && !hasText(conversation.getSlotData().get("resolved_profile_user_id"))) {
            return askForSlot(
                    conversation,
                    prompt,
                    normalizedPrompt,
                    understanding,
                    source,
                    VisionIntent.VIEW_USER_PROFILE,
                    "target_user",
                    "What profile should I open? Say a username, email, or user id."
            );
        }

        if (hasText(profileQuery)) {
            VisionResolvedUserTarget target = visionCapabilityPreviewService.resolveUserProfileTarget(conversation.getOwner(), profileQuery);
            if (!target.resolved()) {
                return askForSlot(conversation, prompt, normalizedPrompt, understanding, source,
                        VisionIntent.VIEW_USER_PROFILE, "target_user", target.blockingMessage());
            }
            visionDetailConversationTurnSupport.applyResolvedProfileTarget(conversation, profileQuery, target);
        }

        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_USER_PROFILE,
                VisionConversationSnapshotSupport.readOnlySnapshotMessage(VisionIntent.VIEW_USER_PROFILE)
        );
    }

    private VisionTurn handleViewCircleDetailTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String circleQuery = visionDetailConversationTurnSupport.resolveCircleDetailQuery(conversation, normalizedPrompt, understanding);
        if (!hasText(circleQuery) && !hasText(conversation.getSlotData().get("resolved_circle_id"))) {
            return askForSlot(
                    conversation,
                    prompt,
                    normalizedPrompt,
                    understanding,
                    source,
                    VisionIntent.VIEW_CIRCLE_DETAIL,
                    "target_circle_query",
                    visionClarificationService.buildQuestion("target_circle_query")
            );
        }

        if (hasText(circleQuery)) {
            VisionResolvedCircleTarget target = visionCapabilityPreviewService.resolveOwnedCircle(conversation.getOwner(), circleQuery);
            if (!target.resolved()) {
                return askForSlot(conversation, prompt, normalizedPrompt, understanding, source,
                        VisionIntent.VIEW_CIRCLE_DETAIL, "target_circle_query", target.blockingMessage());
            }
            visionDetailConversationTurnSupport.applyResolvedCircleTarget(conversation, circleQuery, target);
        }

        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_CIRCLE_DETAIL,
                VisionConversationSnapshotSupport.readOnlySnapshotMessage(VisionIntent.VIEW_CIRCLE_DETAIL)
        );
    }

    private VisionTurn handleViewQuestDetailTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String questQuery = visionDetailConversationTurnSupport.resolveQuestDetailQuery(conversation, normalizedPrompt, understanding);
        if (!hasText(questQuery) && !hasText(conversation.getSlotData().get("resolved_quest_id"))) {
            return askForSlot(
                    conversation,
                    prompt,
                    normalizedPrompt,
                    understanding,
                    source,
                    VisionIntent.VIEW_QUEST_DETAIL,
                    "target_quest_query",
                    "What quest should I open? Say the quest title or quest id."
            );
        }

        if (hasText(questQuery)) {
            VisionResolvedQuestTarget target = visionCapabilityPreviewService.resolveVisibleQuest(conversation.getOwner(), questQuery);
            if (!target.resolved()) {
                return askForSlot(conversation, prompt, normalizedPrompt, understanding, source,
                        VisionIntent.VIEW_QUEST_DETAIL, "target_quest_query", target.blockingMessage());
            }
            visionDetailConversationTurnSupport.applyResolvedQuestViewTarget(conversation, questQuery, target);
        }

        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_QUEST_DETAIL,
                VisionConversationSnapshotSupport.readOnlySnapshotMessage(VisionIntent.VIEW_QUEST_DETAIL)
        );
    }

    private VisionTurn handleViewApplicationDetailTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String applicationQuery = visionDetailConversationTurnSupport.resolveApplicationDetailQuery(conversation, normalizedPrompt, understanding);
        if (!hasText(applicationQuery) && !hasText(conversation.getSlotData().get("application_id"))) {
            return askForSlot(
                    conversation,
                    prompt,
                    normalizedPrompt,
                    understanding,
                    source,
                    VisionIntent.VIEW_APPLICATION_DETAIL,
                    "target_application_query",
                    visionClarificationService.buildQuestion("target_application_query")
            );
        }

        if (hasText(applicationQuery)) {
            VisionResolvedApplicationTarget target = visionCapabilityPreviewService.resolveMyApplicationDetail(conversation.getOwner(), applicationQuery);
            if (!target.resolved()) {
                return askForSlot(conversation, prompt, normalizedPrompt, understanding, source,
                        VisionIntent.VIEW_APPLICATION_DETAIL, "target_application_query", target.blockingMessage());
            }
            visionDetailConversationTurnSupport.applyResolvedApplicationTarget(conversation, applicationQuery, target);
            conversation.getSlotData().put("target_application_query", applicationQuery);
        }

        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_APPLICATION_DETAIL,
                VisionConversationSnapshotSupport.readOnlySnapshotMessage(VisionIntent.VIEW_APPLICATION_DETAIL)
        );
    }

    VisionTurn handleReadOnlySnapshotTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source,
            VisionIntent intent,
            String message
    ) {
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        conversation.setRequestedSlot(null);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);

        return createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                intent,
                VisionAgentState.RECOMMENDING,
                VisionNextAction.SHOW_RESULTS,
                null,
                understanding.isTranslationApplied(),
                understanding.isTranslationReliable(),
                message
        );
    }

    private VisionTurn askForSlot(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source,
            VisionIntent intent,
            String requestedSlot,
            String message
    ) {
        conversation.setRequestedSlot(requestedSlot);
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, intent,
                VisionAgentState.ASKING, VisionNextAction.ASK_FOR_SLOT, requestedSlot,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private String resolveCircleName(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        return visionConversationSlotResolutionSupport.resolveCircleName(conversation, normalizedPrompt, understanding);
    }

    private String resolveCircleRename(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        return visionConversationSlotResolutionSupport.resolveCircleRename(conversation, normalizedPrompt, understanding);
    }

    private String resolveApplicationQuestQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        return visionConversationSlotResolutionSupport.resolveApplicationQuestQuery(conversation, normalizedPrompt, understanding);
    }

    private String resolveApplicationMessage(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        return visionConversationSlotResolutionSupport.resolveApplicationMessage(conversation, normalizedPrompt, understanding);
    }

    private String resolveApplicationProposedPrice(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        return visionConversationSlotResolutionSupport.resolveApplicationProposedPrice(conversation, normalizedPrompt, understanding);
    }

    private String nextMissingUpdateApplicationSlot(VisionConversation conversation) {
        return visionConversationSlotResolutionSupport.nextMissingUpdateApplicationSlot(conversation);
    }

    private String questionForUpdateApplication(String slotId) {
        if ("application_message".equals(slotId)) {
            return "What should I change in your application? You can give a new message, a new price, or both.";
        }
        return visionClarificationService.buildQuestion(slotId);
    }

    private String retryQuestionForUpdateApplication(String slotId) {
        if ("application_message".equals(slotId)) {
            return "I still need at least one application change. Say a new message, a new price, or both.";
        }
        return visionClarificationService.buildRetryQuestion(slotId);
    }

    private String effectiveApplicationMessage(VisionConversation conversation) {
        String draftMessage = conversation.getSlotData().get("application_message");
        if (hasText(draftMessage)) {
            return draftMessage;
        }
        return conversation.getSlotData().get("application_existing_message");
    }

    private String effectiveApplicationPrice(VisionConversation conversation) {
        String draftPrice = conversation.getSlotData().get("application_proposed_price");
        if (hasText(draftPrice)) {
            return draftPrice;
        }
        return conversation.getSlotData().get("application_existing_proposed_price");
    }

    private String resolveManagedApplicantQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.semanticPlanOrEmpty().getTargetUserQuery();
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "target_user".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            List.of(
                                    "approve application",
                                    "decline application",
                                    "reject application",
                                    "accept application",
                                    "applicant",
                                    "approve",
                                    "decline",
                                    "reject",
                                    "accept"
                            ),
                            List.of(" for ")
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                List.of(
                        "approve application",
                        "decline application",
                        "reject application",
                        "accept application",
                        "applicant",
                        "approve",
                        "decline",
                        "reject",
                        "accept"
                ),
                List.of(" for ")
        );
    }

    private String resolveUserProfileQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.semanticPlanOrEmpty().getTargetUserQuery();
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "target_user".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            List.of(
                                    "show profile of",
                                    "open profile of",
                                    "show profile for",
                                    "open profile for",
                                    "show user",
                                    "open user"
                            )
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                List.of(
                        "show profile of",
                        "open profile of",
                        "show profile for",
                        "open profile for",
                        "show user",
                        "open user"
                )
        );
    }

    private String resolveCircleRequestTargetUser(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.semanticPlanOrEmpty().getTargetUserQuery();
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "target_user".equals(conversation.getRequestedSlot())) {
            return VisionPromptTextSupport.stripLeadingWords(
                    firstNonBlank(
                            VisionPromptTextSupport.extractAfterAnyPrefix(
                                    trimmed,
                                    List.of(
                                            "send a circle request to",
                                            "send circle request to",
                                            "invite to my circle",
                                            "invite to my circles",
                                            "add to my circle",
                                            "add to my circles",
                                            "connect with",
                                            "accept circle request from",
                                            "accept connection request from",
                                            "accept invite from",
                                            "decline circle request from",
                                            "reject circle request from",
                                            "decline invite from",
                                            "reject invite from",
                                            "cancel circle request to",
                                            "cancel invite to",
                                            "delete circle request with",
                                            "remove circle request with"
                                    )
                            ),
                            trimmed
                    ),
                    List.of("user", "users", "person", "people", "member", "members", "contact", "contacts", "profile", "profiles")
            );
        }
        String stripped = VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                List.of(
                        "send a circle request to",
                        "send circle request to",
                        "invite to my circle",
                        "invite to my circles",
                        "add to my circle",
                        "add to my circles",
                        "connect with",
                        "accept circle request from",
                        "accept connection request from",
                        "accept invite from",
                        "decline circle request from",
                        "reject circle request from",
                        "decline invite from",
                        "reject invite from",
                        "cancel circle request to",
                        "cancel invite to",
                        "delete circle request with",
                        "remove circle request with"
                )
        );
        return VisionPromptTextSupport.stripLeadingWords(
                stripped,
                List.of("user", "users", "person", "people", "member", "members", "contact", "contacts", "profile", "profiles")
        );
    }

    private void applyResolvedManagedApplicationTarget(
            VisionConversation conversation,
            VisionResolvedManagedApplicationTarget target
    ) {
        conversation.getSlotData().put("managed_application_quest_id", target.questId().toString());
        conversation.getSlotData().put("managed_application_quest_title", target.questTitle());
        conversation.getSlotData().put("managed_application_applicant_username", target.applicantUsername());
        conversation.getSlotData().put("managed_application_message", target.currentMessage() == null ? "" : target.currentMessage());
        conversation.getSlotData().put("managed_application_proposed_price", target.currentPrice() == null ? "" : target.currentPrice());
        conversation.getSlotData().put("managed_application_id", target.applicationId().toString());
    }

    private void applyResolvedCircleRequestRecipient(
            VisionConversation conversation,
            String targetUserQuery,
            VisionResolvedUserTarget target
    ) {
        conversation.getSlotData().put("target_user", targetUserQuery);
        conversation.getSlotData().put("circle_request_target_user_id", target.userId().toString());
        conversation.getSlotData().put("circle_request_target_username", target.username());
    }

    private void applyResolvedCircleRequestTarget(
            VisionConversation conversation,
            String targetUserQuery,
            VisionResolvedCircleRequestTarget target
    ) {
        conversation.getSlotData().put("target_user", targetUserQuery);
        conversation.getSlotData().put("circle_request_id", target.requestId().toString());
        conversation.getSlotData().put("circle_request_target_user_id", target.counterpartUserId().toString());
        conversation.getSlotData().put("circle_request_target_username", target.counterpartUsername());
        conversation.getSlotData().put("circle_request_direction", target.incoming() ? "incoming" : "outgoing");
    }

    private String resolveProfileLocationMode(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        return visionConversationSlotResolutionSupport.resolveProfileLocationMode(conversation, normalizedPrompt, understanding);
    }

    private String resolveProfileLocationLabel(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        return visionConversationSlotResolutionSupport.resolveProfileLocationLabel(conversation, normalizedPrompt, understanding);
    }

    private String nextMissingProfileLocationSlot(VisionConversation conversation) {
        return visionConversationSlotResolutionSupport.nextMissingProfileLocationSlot(conversation);
    }

    private String resolveProfileUsername(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        return visionConversationSlotResolutionSupport.resolveProfileUsername(conversation, normalizedPrompt, understanding);
    }

    private String resolveProfileDescription(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        return visionConversationSlotResolutionSupport.resolveProfileDescription(conversation, normalizedPrompt, understanding);
    }

    private boolean hasProfileUpdateDraft(VisionConversation conversation) {
        return visionConversationSlotResolutionSupport.hasProfileUpdateDraft(conversation);
    }

    private boolean looksLikeProfileDescriptionInstruction(String prompt) {
        return visionConversationSlotResolutionSupport.looksLikeProfileDescriptionInstruction(prompt);
    }

    private boolean looksLikeGenericProfileUpdateInstruction(String prompt) {
        return visionConversationSlotResolutionSupport.looksLikeGenericProfileUpdateInstruction(prompt);
    }

    private boolean hasText(String value) {
        return visionConversationSlotResolutionSupport.hasText(value);
    }

    private String firstNonBlank(String... values) {
        return visionConversationSlotResolutionSupport.firstNonBlank(values);
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
        return visionConversationMutationSupport.createTurn(
                conversation,
                source,
                prompt,
                normalizedPrompt,
                intent,
                agentState,
                nextAction,
                requestedSlot,
                translationApplied,
                translationReliable,
                assistantMessage
        );
    }

    private void updateConversationMetadata(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            String assistantMessage,
            boolean translationReliable
    ) {
        visionConversationMutationSupport.updateConversationMetadata(
                conversation,
                prompt,
                normalizedPrompt,
                assistantMessage,
                translationReliable
        );
    }

    private void ensureTurnCapacity(VisionConversation conversation) {
        visionConversationMutationSupport.ensureTurnCapacity(conversation);
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

}
