package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import com.themuffinman.app.vision.dto.VisionConversationTurnRequestDTO;
import com.themuffinman.app.vision.dto.VisionConversationListResponseDTO;
import com.themuffinman.app.vision.dto.VisionConversationSummaryDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.dto.VisionWorkspaceHandoffDTO;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.VisionExecutionCandidateDTO;
import com.themuffinman.app.vision.dto.VisionMemoryTrailDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.vision.dto.VisionQuestDiscoveryDTO;
import com.themuffinman.app.vision.dto.VisionSearchDiscoveryDTO;
import com.themuffinman.app.vision.dto.VisionSearchComparisonDTO;
import com.themuffinman.app.workmarket.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.notification.model.NotificationPreferenceCategory;
import com.themuffinman.app.notification.model.NotificationPreferenceLevel;
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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;
import com.themuffinman.app.rides.dto.RideOfferResponseDTO;
import com.themuffinman.app.rides.service.RideOfferService;

@Service
public class VisionConversationService {

    @Autowired(required = false)
    private RideOfferService rideOfferService;

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
        String clientRequestId = clientRequestId(dto);
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
                existingConversation,
                clientRequestId
        );
        if (action == VisionConversationAction.SUBMIT_PROMPT
                && dto != null
                && dto.getConversationId() == null
                && clientRequestId != null
                && isConversationReplayCandidate(conversation, clientRequestId)) {
            VisionTurn lastTurn = visionTurnRepository.findTopByConversationOrderByTurnIndexDesc(conversation)
                    .orElseGet(() -> visionConversationLifecycleSupport.snapshotTurn(conversation));
            return replayConversation(conversation, lastTurn, currentUser, understanding);
        }
        rememberRuntimeHints(conversation, runtimeHints);
        if (isDuplicateClientRequest(conversation, clientRequestId)) {
            VisionTurn lastTurn = visionTurnRepository.findTopByConversationOrderByTurnIndexDesc(conversation)
                    .orElseThrow(() -> ServiceErrors.conflict("Vision conversation retry state was not found"));
            return replayConversation(conversation, lastTurn, currentUser, understanding);
        }
        visionConversationMutationSupport.ensureTurnCapacity(conversation);

        String source = effectiveSource(dto);
        VisionTurn turn = handleConversationTurn(action, conversation, prompt, normalizedPrompt, understanding, source, dto);
        rememberClientRequestId(conversation, clientRequestId);

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
                visionExecutionPlanner.plan(conversation, understanding),
                workspaceHandoff(dto, turn)
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
            case FETCH_MORE_RESULTS -> handleFetchMoreResultsTurn(conversation, source);
            case SUBMIT_PROMPT -> handleSubmitPromptTurn(conversation, prompt, normalizedPrompt, understanding, source);
        };
    }

    private VisionTurn handleFetchMoreResultsTurn(VisionConversation conversation, String source) {
        if (conversation.getIntent() != VisionIntent.DISCOVER_QUESTS && conversation.getIntent() != VisionIntent.SEARCH) {
            throw ServiceErrors.conflict("More results are not available for this vision conversation");
        }
        String pageKey = conversation.getIntent() == VisionIntent.SEARCH ? "search_page" : "discovery_page";
        int currentPage = discoveryPage(conversation, pageKey);
        boolean hasMore;
        String message;
        if (conversation.getIntent() == VisionIntent.SEARCH) {
            var current = visionSearchDiscoveryService.discover(conversation, VisionPromptUnderstandingResult.empty(""), conversation.getOwner());
            hasMore = current != null && current.isHasMore();
            message = current == null ? "No more results are available." : current.getSummary();
        } else {
            VisionQuestDiscoveryDTO current = visionQuestDiscoveryService.discover(conversation, VisionPromptUnderstandingResult.empty(""), conversation.getOwner());
            hasMore = current != null && current.isHasMore();
            message = current == null ? "No more results are available." : current.getSummary();
        }
        if (!hasMore) throw ServiceErrors.conflict("No more results are available");
        conversation.getSlotData().put(pageKey, Integer.toString(currentPage + 1));
        if (conversation.getIntent() == VisionIntent.SEARCH) {
            var next = visionSearchDiscoveryService.discover(conversation, VisionPromptUnderstandingResult.empty(""), conversation.getOwner());
            message = next == null ? "No more results are available." : next.getSummary();
        } else {
            VisionQuestDiscoveryDTO next = visionQuestDiscoveryService.discover(conversation, VisionPromptUnderstandingResult.empty(""), conversation.getOwner());
            message = next == null ? "No more results are available." : next.getSummary();
        }
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setTurnIndex((int) visionTurnRepository.countByConversation(conversation) + 1);
        turn.setSource(VisionTurnSource.from(source));
        turn.setPrompt("");
        turn.setNormalizedPrompt("");
        turn.setDetectedIntent(VisionIntent.DISCOVER_QUESTS);
        turn.setAgentState(VisionAgentState.RECOMMENDING);
        turn.setNextAction(VisionNextAction.SHOW_RESULTS);
        turn.setAssistantMessage(message);
        return visionTurnRepository.save(turn);
    }

    private int discoveryPage(VisionConversation conversation, String pageKey) {
        String raw = conversation.getSlotData().get(pageKey);
        try {
            return raw == null ? 0 : Math.max(0, Integer.parseInt(raw));
        } catch (NumberFormatException exception) {
            return 0;
        }
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
            case LEAVE_CIRCLE -> handleLeaveCircleTurn(conversation, prompt, normalizedPrompt, understanding, source);
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
            case SYNC_CHAT -> visionReadOnlyConversationTurnHandler.handleSyncChatTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_CHAT_ATTACHMENT -> visionReadOnlyConversationTurnHandler.handleViewChatAttachmentTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case MARK_CHAT_READ -> handleMarkChatReadTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case EDIT_CHAT_MESSAGE, REPLY_TO_CHAT_MESSAGE, REACT_TO_CHAT_MESSAGE -> handleChatMessageMutationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case CREATE_BUSINESS_PROFILE, UPDATE_BUSINESS_PROFILE -> handleBusinessProfileMutationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case CREATE_GALLERY_IMAGE, UPDATE_GALLERY_IMAGE, DELETE_GALLERY_IMAGE -> handleGalleryMutationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case CREATE_AVAILABILITY_RULE, UPDATE_AVAILABILITY_RULE, DELETE_AVAILABILITY_RULE, CREATE_AVAILABILITY_EXCEPTION, UPDATE_AVAILABILITY_EXCEPTION, DELETE_AVAILABILITY_EXCEPTION -> handleAvailabilityMutationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case CONFIRM_BOOKING, CANCEL_BOOKING -> handleBookingMutationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case REJECT_BOOKING, COMPLETE_BOOKING, MARK_BOOKING_NO_SHOW -> handleBookingMutationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case RESCHEDULE_BOOKING -> handleRescheduleBookingTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case ARCHIVE_OFFERING -> handleArchiveOfferingTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case UPDATE_QUEST -> handleUpdateQuestTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case CREATE_OFFERING, UPDATE_OFFERING -> handleOfferingMutationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case CREATE_BOOKING -> handleCreateBookingTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case MARK_NOTIFICATIONS_READ -> handleMarkNotificationsReadTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case MARK_NOTIFICATION_READ -> handleMarkNotificationReadTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case UPDATE_NOTIFICATION_PREFERENCES -> handleUpdateNotificationPreferencesTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case RELEASE_WORKER, REPLACE_WORKER -> handleWorkerManagementTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case REOPEN_QUEST -> handleReopenQuestTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case CANCEL_QUEST -> handleCancelQuestTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case PAUSE_QUEST -> handlePauseQuestTurn(conversation, prompt, normalizedPrompt, understanding, source, false);
            case RESUME_QUEST -> handlePauseQuestTurn(conversation, prompt, normalizedPrompt, understanding, source, true);
            case CREATE_THING -> handleCreateThingTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case REQUEST_BORROW -> handleRequestBorrowTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case CANCEL_BORROW -> handleBorrowLifecycleTurn(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.CANCEL_BORROW, "cancel_borrow", "Cancel borrow request ", "borrow_cancelled");
            case DECIDE_BORROW -> handleBorrowLifecycleTurn(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.DECIDE_BORROW, "decide_borrow", "Decide borrow request ", "borrow_decided");
            case RETURN_BORROW -> handleBorrowLifecycleTurn(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.RETURN_BORROW, "return_borrow", "Return borrow request ", "borrow_returned");
            case CREATE_RIDE, JOIN_RIDE, UPDATE_RIDE, LEAVE_RIDE, CANCEL_RIDE, START_RIDE, COMPLETE_RIDE -> handleRideTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case UPDATE_THING, ARCHIVE_THING -> handleThingMutationTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_PROFILE -> visionReadOnlyConversationTurnHandler.handleViewProfileTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_SETTINGS -> visionReadOnlyConversationTurnHandler.handleViewSettingsTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_BUSINESS -> visionReadOnlyConversationTurnHandler.handleViewBusinessTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_BUSINESS_AVAILABILITY -> visionReadOnlyConversationTurnHandler.handleViewBusinessAvailabilityTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_BUSINESS_BOOKINGS -> visionReadOnlyConversationTurnHandler.handleViewBusinessBookingsTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_USER_PROFILE -> handleViewUserProfileTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_CIRCLES -> visionReadOnlyConversationTurnHandler.handleViewCirclesTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_CIRCLE_DETAIL -> handleViewCircleDetailTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_ACCESSIBLE_CIRCLE -> handleViewAccessibleCircleTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_QUEST_DETAIL -> handleViewQuestDetailTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_THING_DETAIL -> handleViewThingDetailTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_NOTIFICATIONS -> visionReadOnlyConversationTurnHandler.handleViewNotificationsTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_ACTIVITY -> visionReadOnlyConversationTurnHandler.handleViewActivityTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_QUEST_NEWS -> visionReadOnlyConversationTurnHandler.handleViewQuestNewsTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_APPLICATIONS -> visionReadOnlyConversationTurnHandler.handleViewApplicationsTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_APPLICATION_DETAIL -> handleViewApplicationDetailTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_THINGS -> visionReadOnlyConversationTurnHandler.handleViewThingsTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_BORROW_REQUESTS -> visionReadOnlyConversationTurnHandler.handleViewBorrowRequestsTurn(this, conversation, prompt, normalizedPrompt, understanding, source);
            case VIEW_RIDES -> handleViewRidesTurn(conversation, prompt, normalizedPrompt, understanding, source);
            case UNSUPPORTED -> handleUnsupportedTurn(conversation, prompt, normalizedPrompt, understanding, source);
        };
    }

    private VisionTurn handleCreateBookingTurn(
            VisionConversation conversation, String prompt, String normalizedPrompt,
            VisionPromptUnderstandingResult understanding, String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        java.util.regex.Matcher offering = java.util.regex.Pattern.compile("(?i)(?:offering|service)\\s*#?(\\d+)").matcher(value);
        if (!conversation.getSlotData().containsKey("business_offering_id") && offering.find()) conversation.getSlotData().put("business_offering_id", offering.group(1));
        java.util.regex.Matcher times = java.util.regex.Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T[^\\s]+)\\s+(?:until|to)\\s+(\\d{4}-\\d{2}-\\d{2}T[^\\s]+)").matcher(value);
        if (!conversation.getSlotData().containsKey("booking_starts_at") && times.find()) {
            conversation.getSlotData().put("booking_starts_at", times.group(1));
            conversation.getSlotData().put("booking_ends_at", times.group(2));
        }
        String missing = !conversation.getSlotData().containsKey("business_offering_id") ? "business_offering_id" : !conversation.getSlotData().containsKey("booking_starts_at") ? "booking_starts_at" : !conversation.getSlotData().containsKey("booking_ends_at") ? "booking_ends_at" : null;
        if (missing != null) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.CREATE_BOOKING, missing, "Please provide " + missing.replace('_', ' ') + ".");
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(value)) {
            if (!visionProperties.isExecutionEnabled()) return bookingCreationReviewTurn(conversation, source, "The appointment review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return bookingCreationReviewTurn(conversation, source, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", "booking_created");
            String message = "Appointment request created.";
            updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, value, VisionIntent.CREATE_BOOKING, VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can request this appointment. Confirm to continue.";
        updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, value, VisionIntent.CREATE_BOOKING, VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleRescheduleBookingTurn(VisionConversation conversation, String prompt, String normalizedPrompt, VisionPromptUnderstandingResult understanding, String source) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:booking|appointment)?\\s*#?(\\d+)").matcher(normalizedPrompt == null ? "" : normalizedPrompt);
        if (!conversation.getSlotData().containsKey("booking_id") && matcher.find()) conversation.getSlotData().put("booking_id", matcher.group(1));
        java.util.regex.Matcher times = java.util.regex.Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T[^\\s]+)\\s+(?:to|until|-|–)\\s+(\\d{4}-\\d{2}-\\d{2}T[^\\s]+)").matcher(normalizedPrompt == null ? "" : normalizedPrompt);
        if (times.find()) { conversation.getSlotData().put("booking_starts_at", times.group(1)); conversation.getSlotData().put("booking_ends_at", times.group(2)); }
        String missing = !conversation.getSlotData().containsKey("booking_id") ? "booking_id" : !conversation.getSlotData().containsKey("booking_starts_at") ? "booking_starts_at" : !conversation.getSlotData().containsKey("booking_ends_at") ? "booking_ends_at" : null;
        if (missing != null) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.RESCHEDULE_BOOKING, missing, "Please provide " + missing.replace('_', ' ') + ".");
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) return rescheduleBookingReviewTurn(conversation, source, "The booking reschedule review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return rescheduleBookingReviewTurn(conversation, source, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            String message = "Booking rescheduled successfully.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.RESCHEDULE_BOOKING, VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can reschedule booking " + conversation.getSlotData().get("booking_id") + " to " + conversation.getSlotData().get("booking_starts_at") + ". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.RESCHEDULE_BOOKING, VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn rescheduleBookingReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", VisionIntent.RESCHEDULE_BOOKING, VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, false, true, message);
    }

    private VisionTurn bookingCreationReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", VisionIntent.CREATE_BOOKING, VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, false, true, message);
    }

    private VisionTurn handleThingMutationTurn(
            VisionConversation conversation, String prompt, String normalizedPrompt,
            VisionPromptUnderstandingResult understanding, String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        boolean update = conversation.getIntent() == VisionIntent.UPDATE_THING;
        if (!conversation.getSlotData().containsKey("thing_listing_id")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:thing|listing)\\s*#?(\\d+)").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("thing_listing_id", matcher.group(1));
        }
        if (!conversation.getSlotData().containsKey("thing_listing_id")) return askForSlot(conversation,prompt,normalizedPrompt,understanding,source,conversation.getIntent(),"thing_listing_id","Which thing listing id should I use?");
        if (update && !conversation.getSlotData().containsKey("thing_title")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:to|named|title)\\s+(.+)$").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("thing_title", matcher.group(1).trim());
        }
        if (update && !conversation.getSlotData().containsKey("thing_title")) return askForSlot(conversation,prompt,normalizedPrompt,understanding,source,VisionIntent.UPDATE_THING,"thing_title","What should the new listing title be?");
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(value)) {
            if (!visionProperties.isExecutionEnabled()) return thingMutationReviewTurn(conversation,source,"The thing listing review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return thingMutationReviewTurn(conversation,source,result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED); conversation.getSlotData().put("conversation_outcome",update ? "thing_updated" : "thing_archived");
            String message=update ? "Thing listing updated." : "Thing listing archived."; updateConversationMetadata(conversation,prompt,value,message,understanding.isTranslationReliable()); visionConversationRepository.save(conversation);
            return createTurn(conversation,source,prompt,value,conversation.getIntent(),VisionAgentState.COMPLETE,VisionNextAction.COMPLETE,null,understanding.isTranslationApplied(),understanding.isTranslationReliable(),message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message=update ? "I can update thing listing " + conversation.getSlotData().get("thing_listing_id") + ". Confirm to continue." : "I can archive thing listing " + conversation.getSlotData().get("thing_listing_id") + ". Confirm to continue.";
        updateConversationMetadata(conversation,prompt,value,message,understanding.isTranslationReliable()); visionConversationRepository.save(conversation);
        return createTurn(conversation,source,prompt,value,conversation.getIntent(),VisionAgentState.REVIEW_READY,VisionNextAction.SHOW_REVIEW,null,understanding.isTranslationApplied(),understanding.isTranslationReliable(),message);
    }

    private VisionTurn thingMutationReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation,"","",message,true); visionConversationRepository.save(conversation);
        return createTurn(conversation,source,"","",conversation.getIntent(),VisionAgentState.REVIEW_READY,VisionNextAction.SHOW_REVIEW,null,false,true,message);
    }

    private VisionTurn handleAvailabilityMutationTurn(
            VisionConversation conversation, String prompt, String normalizedPrompt,
            VisionPromptUnderstandingResult understanding, String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        VisionIntent intent = conversation.getIntent();
        boolean exception = intent == VisionIntent.CREATE_AVAILABILITY_EXCEPTION || intent == VisionIntent.UPDATE_AVAILABILITY_EXCEPTION || intent == VisionIntent.DELETE_AVAILABILITY_EXCEPTION;
        boolean deletion = intent == VisionIntent.DELETE_AVAILABILITY_RULE || intent == VisionIntent.DELETE_AVAILABILITY_EXCEPTION;
        String idKey = exception ? "availability_exception_id" : "availability_rule_id";
        if (!intent.name().startsWith("CREATE_") && !conversation.getSlotData().containsKey(idKey)) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:rule|exception)\\s*#?(\\d+)").matcher(value);
            if (matcher.find()) conversation.getSlotData().put(idKey, matcher.group(1));
        }
        if (!intent.name().startsWith("CREATE_") && !conversation.getSlotData().containsKey(idKey)) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, intent, idKey, "Which availability " + (exception ? "exception" : "rule") + " id should I use?");
        if (!deletion && exception) {
            captureAvailability(conversation, value, "(?i)(BLOCK|REPLACE_WINDOW)", "availability_exception_type");
            captureAvailability(conversation, value, "(?i)(\\d{4}-\\d{2}-\\d{2}T[^\\s]+)", "availability_exception_start");
            java.util.regex.Matcher times = java.util.regex.Pattern.compile("(?i)(\\d{4}-\\d{2}-\\d{2}T[^\\s]+)\\s+(?:until|to)\\s+(\\d{4}-\\d{2}-\\d{2}T[^\\s]+)").matcher(value);
            if (times.find()) { conversation.getSlotData().put("availability_exception_start", times.group(1)); conversation.getSlotData().put("availability_exception_end", times.group(2)); }
            if (!conversation.getSlotData().containsKey("availability_exception_type")) return askForSlot(conversation,prompt,normalizedPrompt,understanding,source,intent,"availability_exception_type","Should this exception block time or replace the window?");
            if (!conversation.getSlotData().containsKey("availability_exception_start") || !conversation.getSlotData().containsKey("availability_exception_end")) return askForSlot(conversation,prompt,normalizedPrompt,understanding,source,intent,"availability_exception_start","Provide the exception start and end as ISO timestamps.");
        } else if (!deletion) {
            captureAvailability(conversation, value, "(?i)(?:day|weekday)\\s*([1-7])", "availability_day_of_week");
            captureAvailability(conversation, value, "(?i)(?:from|start)\\s*(\\d{2}:\\d{2})", "availability_start_time");
            captureAvailability(conversation, value, "(?i)(?:until|to|end)\\s*(\\d{2}:\\d{2})", "availability_end_time");
            captureAvailability(conversation, value, "(?i)(?:every|slot)\\s*(\\d+)\\s*min", "availability_slot_minutes");
            if (!conversation.getSlotData().containsKey("availability_day_of_week")) return askForSlot(conversation,prompt,normalizedPrompt,understanding,source,intent,"availability_day_of_week","Which weekday number (1-7) should this rule use?");
            if (!conversation.getSlotData().containsKey("availability_start_time") || !conversation.getSlotData().containsKey("availability_end_time")) return askForSlot(conversation,prompt,normalizedPrompt,understanding,source,intent,"availability_start_time","Provide the local start and end time, for example 09:00 to 17:00.");
            if (!conversation.getSlotData().containsKey("availability_slot_minutes")) return askForSlot(conversation,prompt,normalizedPrompt,understanding,source,intent,"availability_slot_minutes","How many minutes should each booking slot use?");
        }
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(value)) {
            if (!visionProperties.isExecutionEnabled()) return availabilityMutationReviewTurn(conversation, source, "The availability review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return availabilityMutationReviewTurn(conversation, source, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED); conversation.getSlotData().put("conversation_outcome", "availability_updated");
            String message = "Availability updated."; updateConversationMetadata(conversation,prompt,value,message,understanding.isTranslationReliable()); visionConversationRepository.save(conversation);
            return createTurn(conversation,source,prompt,value,intent,VisionAgentState.COMPLETE,VisionNextAction.COMPLETE,null,understanding.isTranslationApplied(),understanding.isTranslationReliable(),message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can update your business availability. Confirm to continue."; updateConversationMetadata(conversation,prompt,value,message,understanding.isTranslationReliable()); visionConversationRepository.save(conversation);
        return createTurn(conversation,source,prompt,value,intent,VisionAgentState.REVIEW_READY,VisionNextAction.SHOW_REVIEW,null,understanding.isTranslationApplied(),understanding.isTranslationReliable(),message);
    }

    private void captureAvailability(VisionConversation conversation, String value, String regex, String key) {
        if (conversation.getSlotData().containsKey(key)) return;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(regex).matcher(value);
        if (matcher.find()) conversation.getSlotData().put(key, matcher.group(1));
    }

    private VisionTurn availabilityMutationReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation,"","",message,true); visionConversationRepository.save(conversation);
        return createTurn(conversation,source,"","",conversation.getIntent(),VisionAgentState.REVIEW_READY,VisionNextAction.SHOW_REVIEW,null,false,true,message);
    }

    private VisionTurn handleGalleryMutationTurn(
            VisionConversation conversation, String prompt, String normalizedPrompt,
            VisionPromptUnderstandingResult understanding, String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        boolean create = conversation.getIntent() == VisionIntent.CREATE_GALLERY_IMAGE;
        boolean delete = conversation.getIntent() == VisionIntent.DELETE_GALLERY_IMAGE;
        if (!create && !conversation.getSlotData().containsKey("gallery_image_id")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:image|photo)\\s*#?(\\d+)").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("gallery_image_id", matcher.group(1));
        }
        if (!create && !conversation.getSlotData().containsKey("gallery_image_id")) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(), "gallery_image_id", "Which gallery image id should I use?");
        if (!delete && !conversation.getSlotData().containsKey("gallery_image_url")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(https?://\\S+)").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("gallery_image_url", matcher.group(1));
        }
        if (!delete && !conversation.getSlotData().containsKey("gallery_image_url")) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(), "gallery_image_url", "What image URL should I use?");
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(value)) {
            if (!visionProperties.isExecutionEnabled()) return galleryMutationReviewTurn(conversation, source, "The gallery review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return galleryMutationReviewTurn(conversation, source, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", delete ? "gallery_image_deleted" : create ? "gallery_image_created" : "gallery_image_updated");
            String message = delete ? "Gallery image deleted." : create ? "Gallery image added." : "Gallery image updated.";
            updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, value, conversation.getIntent(), VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = delete ? "I can delete gallery image " + conversation.getSlotData().get("gallery_image_id") + ". Confirm to continue."
                : (create ? "I can add this image to your business gallery." : "I can update gallery image " + conversation.getSlotData().get("gallery_image_id") + ".") + " Confirm to continue.";
        updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, value, conversation.getIntent(), VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn galleryMutationReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", conversation.getIntent(), VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, false, true, message);
    }

    private VisionTurn handleOfferingMutationTurn(
            VisionConversation conversation, String prompt, String normalizedPrompt,
            VisionPromptUnderstandingResult understanding, String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        boolean update = conversation.getIntent() == VisionIntent.UPDATE_OFFERING;
        if (update && !conversation.getSlotData().containsKey("offering_id")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:offering|service)\\s*#?(\\d+)").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("offering_id", matcher.group(1));
        }
        if (update && !conversation.getSlotData().containsKey("offering_id")) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(), "offering_id", "Which offering id should I update?");
        if (!conversation.getSlotData().containsKey("offering_title")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:to|named|title)\\s+(.+)$").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("offering_title", matcher.group(1).trim());
        }
        if (!conversation.getSlotData().containsKey("offering_title")) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(), "offering_title", "What should the offering be called?");
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(value)) {
            if (!visionProperties.isExecutionEnabled()) return offeringMutationReviewTurn(conversation, source, "The offering review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return offeringMutationReviewTurn(conversation, source, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", update ? "offering_updated" : "offering_created");
            String message = update ? "Offering updated." : "Offering created.";
            updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, value, conversation.getIntent(), VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can " + (update ? "rename offering " + conversation.getSlotData().get("offering_id") : "create an offering") + " as \"" + conversation.getSlotData().get("offering_title") + "\". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, value, conversation.getIntent(), VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn offeringMutationReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", conversation.getIntent(), VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, false, true, message);
    }

    private VisionTurn handleUpdateQuestTurn(
            VisionConversation conversation, String prompt, String normalizedPrompt,
            VisionPromptUnderstandingResult understanding, String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        if (!conversation.getSlotData().containsKey("quest_id")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:quest|job|work)\\s*#?(\\d+)").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("quest_id", matcher.group(1));
        }
        if (!conversation.getSlotData().containsKey("quest_id")) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.UPDATE_QUEST, "quest_id", "Which quest id should I update?");
        if (!conversation.getSlotData().containsKey("quest_title")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:to|title)\\s+(.+)$").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("quest_title", matcher.group(1).trim());
        }
        if (!conversation.getSlotData().containsKey("quest_title")) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.UPDATE_QUEST, "quest_title", "What should the new quest title be?");
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(value)) {
            if (!visionProperties.isExecutionEnabled()) return questUpdateReviewTurn(conversation, source, "The quest update review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return questUpdateReviewTurn(conversation, source, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", "quest_updated");
            String message = "Quest updated.";
            updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, value, VisionIntent.UPDATE_QUEST, VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can rename quest " + conversation.getSlotData().get("quest_id") + " to \"" + conversation.getSlotData().get("quest_title") + "\". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, value, VisionIntent.UPDATE_QUEST, VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn questUpdateReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", VisionIntent.UPDATE_QUEST, VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, false, true, message);
    }

    private VisionTurn handleArchiveOfferingTurn(
            VisionConversation conversation, String prompt, String normalizedPrompt,
            VisionPromptUnderstandingResult understanding, String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        if (!conversation.getSlotData().containsKey("offering_id")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:offering|service)\\s*#?(\\d+)").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("offering_id", matcher.group(1));
        }
        if (!conversation.getSlotData().containsKey("offering_id")) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.ARCHIVE_OFFERING, "offering_id", "Which offering id should I archive?");
        }
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(value)) {
            if (!visionProperties.isExecutionEnabled()) return offeringReviewTurn(conversation, source, "The offering archive review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return offeringReviewTurn(conversation, source, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", "offering_archived");
            String message = "Offering archived.";
            updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, value, VisionIntent.ARCHIVE_OFFERING, VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can archive offering " + conversation.getSlotData().get("offering_id") + ". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, value, VisionIntent.ARCHIVE_OFFERING, VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn offeringReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", VisionIntent.ARCHIVE_OFFERING, VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, false, true, message);
    }

    private VisionTurn handleBookingMutationTurn(
            VisionConversation conversation, String prompt, String normalizedPrompt,
            VisionPromptUnderstandingResult understanding, String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        if (!conversation.getSlotData().containsKey("booking_id")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:booking|appointment)\\s*#?(\\d+)").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("booking_id", matcher.group(1));
        }
        if (!conversation.getSlotData().containsKey("booking_id")) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(), "booking_id", "Which booking id should I use?");
        }
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(value)) {
            if (!visionProperties.isExecutionEnabled()) return bookingReviewTurn(conversation, source, "The booking review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return bookingReviewTurn(conversation, source, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", conversation.getIntent() == VisionIntent.CONFIRM_BOOKING ? "booking_confirmed" : "booking_cancelled");
            String message = conversation.getIntent() == VisionIntent.CONFIRM_BOOKING ? "Booking confirmed." : "Booking cancelled.";
            updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, value, conversation.getIntent(), VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can " + (conversation.getIntent() == VisionIntent.CONFIRM_BOOKING ? "confirm" : "cancel") + " booking " + conversation.getSlotData().get("booking_id") + ". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, value, conversation.getIntent(), VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn bookingReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", conversation.getIntent(), VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, false, true, message);
    }

    private VisionTurn handleBusinessProfileMutationTurn(
            VisionConversation conversation, String prompt, String normalizedPrompt,
            VisionPromptUnderstandingResult understanding, String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        if (!conversation.getSlotData().containsKey("business_name")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:named|name|to)\\s+(.+)$").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("business_name", matcher.group(1).trim());
        }
        if (!conversation.getSlotData().containsKey("business_name")) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(), "business_name", "What should the business be called?");
        }
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(value)) {
            if (!visionProperties.isExecutionEnabled()) return businessProfileReviewTurn(conversation, source, "The business profile review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return businessProfileReviewTurn(conversation, source, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", conversation.getIntent() == VisionIntent.CREATE_BUSINESS_PROFILE ? "business_profile_created" : "business_profile_updated");
            String message = conversation.getIntent() == VisionIntent.CREATE_BUSINESS_PROFILE ? "Business profile created." : "Business profile updated.";
            updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, value, conversation.getIntent(), VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can " + (conversation.getIntent() == VisionIntent.CREATE_BUSINESS_PROFILE ? "create" : "update") + " the business profile named " + conversation.getSlotData().get("business_name") + ". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, value, conversation.getIntent(), VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn businessProfileReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", conversation.getIntent(), VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, false, true, message);
    }

    private VisionTurn handleChatMessageMutationTurn(
            VisionConversation conversation, String prompt, String normalizedPrompt,
            VisionPromptUnderstandingResult understanding, String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        java.util.regex.Matcher idMatcher = java.util.regex.Pattern.compile("(?i)(?:message|msg)\\s*#?(\\d+)").matcher(value);
        if (!conversation.getSlotData().containsKey("message_id") && idMatcher.find()) {
            conversation.getSlotData().put("message_id", idMatcher.group(1));
        }
        if (!conversation.getSlotData().containsKey("message_id")) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(), "message_id", "Which message id should I use?");
        }
        if (conversation.getIntent() != VisionIntent.REACT_TO_CHAT_MESSAGE && !conversation.getSlotData().containsKey("message_text")) {
            java.util.regex.Matcher textMatcher = java.util.regex.Pattern.compile("(?i)(?:to|that says|with)\\s+(.+)$").matcher(value);
            if (textMatcher.find()) conversation.getSlotData().put("message_text", textMatcher.group(1).trim());
        }
        if (conversation.getIntent() == VisionIntent.REACT_TO_CHAT_MESSAGE && !conversation.getSlotData().containsKey("reaction_emoji")) {
            java.util.regex.Matcher emojiMatcher = java.util.regex.Pattern.compile("(?i)(?:with|using)\\s+(.+)$").matcher(value);
            if (emojiMatcher.find()) conversation.getSlotData().put("reaction_emoji", emojiMatcher.group(1).trim());
        }
        String requiredSlot = conversation.getIntent() == VisionIntent.REACT_TO_CHAT_MESSAGE ? "reaction_emoji" : "message_text";
        if (!conversation.getSlotData().containsKey(requiredSlot)) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(), requiredSlot,
                    conversation.getIntent() == VisionIntent.REACT_TO_CHAT_MESSAGE ? "Which emoji should I add?" : "What should the message say?");
        }
        if (!conversation.getSlotData().containsKey("opened_chat_conversation_id")) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(), "opened_chat_conversation_id", "Which open chat conversation should I use?");
        }
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(value)) {
            if (!visionProperties.isExecutionEnabled()) return chatMutationReviewTurn(conversation, source, "The chat message review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return chatMutationReviewTurn(conversation, source, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            String outcome = switch (conversation.getIntent()) {
                case EDIT_CHAT_MESSAGE -> "chat_message_edited";
                case REPLY_TO_CHAT_MESSAGE -> "chat_message_replied";
                default -> "chat_message_reacted";
            };
            conversation.getSlotData().put("conversation_outcome", outcome);
            String message = switch (conversation.getIntent()) {
                case EDIT_CHAT_MESSAGE -> "Chat message edited.";
                case REPLY_TO_CHAT_MESSAGE -> "Chat reply sent.";
                default -> "Chat reaction added.";
            };
            updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, value, conversation.getIntent(), VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can apply this chat message action to message " + conversation.getSlotData().get("message_id") + ". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, value, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, value, conversation.getIntent(), VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn chatMutationReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", conversation.getIntent(), VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, false, true, message);
    }

    private VisionTurn handleMarkChatReadTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY
                && VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                return markChatReadReviewTurn(conversation, source, "The chat read-state review is ready, but execution is disabled.");
            }
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) {
                return markChatReadReviewTurn(conversation, source, result.getBlockingReason());
            }
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", "chat_marked_read");
            String message = "Chat marked as read.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.MARK_CHAT_READ,
                    VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }

        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can mark the currently opened chat as read. Confirm to continue.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.MARK_CHAT_READ,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn markChatReadReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", VisionIntent.MARK_CHAT_READ,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                false, true, message);
    }

    private VisionTurn handleMarkNotificationsReadTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY
                && VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                return markNotificationsReadReviewTurn(conversation, source, "The notification read-state review is ready, but execution is disabled.");
            }
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) {
                return markNotificationsReadReviewTurn(conversation, source, result.getBlockingReason());
            }
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", "notifications_marked_read");
            String message = "Notifications marked as read.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.MARK_NOTIFICATIONS_READ,
                    VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }

        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can mark all your notifications as read. Confirm to continue.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.MARK_NOTIFICATIONS_READ,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn markNotificationsReadReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", VisionIntent.MARK_NOTIFICATIONS_READ,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                false, true, message);
    }

    private VisionTurn handleUpdateNotificationPreferencesTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String lower = TextValueNormalizer.lowerTrimToEmpty(normalizedPrompt);
        String category = conversation.getSlotData().get("notification_category");
        String level = conversation.getSlotData().get("notification_level");
        String enabled = conversation.getSlotData().get("notification_enabled");
        if (category == null) {
            category = notificationCategoryFrom(lower);
            if (category != null) conversation.getSlotData().put("notification_category", category);
        }
        if (level == null) {
            level = notificationLevelFrom(lower);
            if (level != null) conversation.getSlotData().put("notification_level", level);
        }
        if (enabled == null) {
            enabled = notificationEnabledFrom(lower);
            if (enabled != null) conversation.getSlotData().put("notification_enabled", enabled);
        }
        if (category == null) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.UPDATE_NOTIFICATION_PREFERENCES, "notification_category", "Which notification category should I change: chat, work, bookings, circles, location, or system?");
        if (level == null) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.UPDATE_NOTIFICATION_PREFERENCES, "notification_level", "Which delivery channel should I change: in-app, push, or email?");
        if (enabled == null) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.UPDATE_NOTIFICATION_PREFERENCES, "notification_enabled", "Should I enable or disable that notification channel?");
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) return notificationPreferenceReviewTurn(conversation, source, "The notification preference review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return notificationPreferenceReviewTurn(conversation, source, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", "notification_preferences_updated");
            String message = "Notification preferences updated.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_NOTIFICATION_PREFERENCES, VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can " + (Boolean.parseBoolean(enabled) ? "enable" : "disable") + " " + category.toLowerCase() + " " + level.toLowerCase().replace('_', '-') + " notifications. Confirm to continue.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_NOTIFICATION_PREFERENCES, VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn notificationPreferenceReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", VisionIntent.UPDATE_NOTIFICATION_PREFERENCES, VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, false, true, message);
    }

    private String notificationCategoryFrom(String value) {
        if (value.contains("chat")) return NotificationPreferenceCategory.CHAT.name();
        if (value.contains("work") || value.contains("quest")) return NotificationPreferenceCategory.WORK.name();
        if (value.contains("booking")) return NotificationPreferenceCategory.BOOKING.name();
        if (value.contains("circle")) return NotificationPreferenceCategory.CIRCLE.name();
        if (value.contains("location")) return NotificationPreferenceCategory.LOCATION.name();
        if (value.contains("system")) return NotificationPreferenceCategory.SYSTEM.name();
        return null;
    }

    private String notificationLevelFrom(String value) {
        if (value.contains("in-app") || value.contains("in app") || value.contains("inapp")) return NotificationPreferenceLevel.IN_APP.name();
        if (value.contains("push")) return NotificationPreferenceLevel.PUSH.name();
        if (value.contains("email")) return NotificationPreferenceLevel.EMAIL.name();
        return null;
    }

    private String notificationEnabledFrom(String value) {
        if (value.contains("turn off") || value.contains("disable") || value.contains("mute") || value.contains("off")) return "false";
        if (value.contains("turn on") || value.contains("enable") || value.contains("unmute") || value.contains("on")) return "true";
        return null;
    }

    private VisionTurn handleMarkNotificationReadTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String notificationId = conversation.getSlotData().get("notification_id");
        if (notificationId == null || notificationId.isBlank()) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:notification|alert|update)?\\s*#?(\\d+)").matcher(normalizedPrompt == null ? "" : normalizedPrompt);
            if (matcher.find()) {
                notificationId = matcher.group(1);
                conversation.getSlotData().put("notification_id", notificationId);
            }
        }
        if (notificationId == null || notificationId.isBlank()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source,
                    VisionIntent.MARK_NOTIFICATION_READ, "notification_id", "Which notification id should I mark as read?");
        }
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY
                && VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                return markNotificationReadReviewTurn(conversation, source, "The notification read-state review is ready, but execution is disabled.");
            }
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) {
                return markNotificationReadReviewTurn(conversation, source, result.getBlockingReason());
            }
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", "notification_marked_read");
            String message = "Notification marked as read.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.MARK_NOTIFICATION_READ,
                    VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can mark notification " + notificationId + " as read. Confirm to continue.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.MARK_NOTIFICATION_READ,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn markNotificationReadReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", VisionIntent.MARK_NOTIFICATION_READ,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                false, true, message);
    }

    private VisionTurn handleWorkerManagementTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String value = TextValueNormalizer.lowerTrimToEmpty(normalizedPrompt);
        captureWorkerId(conversation, value, "worker_quest_id", "(?:quest|work|job)\\s*#?(\\d+)");
        captureWorkerId(conversation, value, "worker_application_id", "(?:worker|application|assignment)\\s*#?(\\d+)");
        if (conversation.getIntent() == VisionIntent.REPLACE_WORKER) {
            captureWorkerId(conversation, value, "replacement_application_id", "(?:replacement|new)\\s+(?:worker|application)\\s*#?(\\d+)");
        }
        if (!conversation.getSlotData().containsKey("worker_quest_id")) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(), "worker_quest_id", "Which quest id should I use?");
        }
        if (!conversation.getSlotData().containsKey("worker_application_id")) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(), "worker_application_id", "Which current worker or application id should I use?");
        }
        if (conversation.getIntent() == VisionIntent.REPLACE_WORKER
                && !conversation.getSlotData().containsKey("replacement_application_id")) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(), "replacement_application_id", "Which pending application id should replace the current worker?");
        }
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY
                && VisionReviewInteractionSupport.isConfirmationPrompt(value)) {
            if (!visionProperties.isExecutionEnabled()) return workerManagementReviewTurn(conversation, source, "The worker management review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return workerManagementReviewTurn(conversation, source, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", conversation.getIntent() == VisionIntent.REPLACE_WORKER ? "worker_replaced" : "worker_released");
            String message = conversation.getIntent() == VisionIntent.REPLACE_WORKER ? "Worker assignment replaced." : "Worker assignment released.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, conversation.getIntent(), VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = conversation.getIntent() == VisionIntent.REPLACE_WORKER
                ? "I can replace worker application " + conversation.getSlotData().get("worker_application_id") + " with application " + conversation.getSlotData().get("replacement_application_id") + ". Confirm to continue."
                : "I can release worker application " + conversation.getSlotData().get("worker_application_id") + " from quest " + conversation.getSlotData().get("worker_quest_id") + ". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, conversation.getIntent(), VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private void captureWorkerId(VisionConversation conversation, String value, String key, String regex) {
        if (conversation.getSlotData().containsKey(key)) return;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)" + regex).matcher(value);
        if (matcher.find()) conversation.getSlotData().put(key, matcher.group(1));
    }

    private VisionTurn workerManagementReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", conversation.getIntent(), VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, false, true, message);
    }

    private VisionTurn handleReopenQuestTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String questId = conversation.getSlotData().get("quest_id");
        if (questId == null || questId.isBlank()) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:quest|job|work)?\\s*#?(\\d+)").matcher(normalizedPrompt == null ? "" : normalizedPrompt);
            if (matcher.find()) {
                questId = matcher.group(1);
                conversation.getSlotData().put("quest_id", questId);
            }
        }
        if (questId == null || questId.isBlank()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source,
                    VisionIntent.REOPEN_QUEST, "quest_id", "Which quest should I reopen?");
        }
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY
                && VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                return reopenQuestReviewTurn(conversation, source, "The quest reopen review is ready, but execution is disabled.");
            }
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) {
                return reopenQuestReviewTurn(conversation, source, result.getBlockingReason());
            }
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", "quest_reopened");
            String message = "Quest reopened successfully.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.REOPEN_QUEST,
                    VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can reopen quest " + questId + ". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.REOPEN_QUEST,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn reopenQuestReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", VisionIntent.REOPEN_QUEST,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                false, true, message);
    }

    private VisionTurn handleCancelQuestTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String questId = conversation.getSlotData().get("quest_id");
        if (questId == null || questId.isBlank()) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:quest|job|work)?\\s*#?(\\d+)")
                    .matcher(normalizedPrompt == null ? "" : normalizedPrompt);
            if (matcher.find()) {
                questId = matcher.group(1);
                conversation.getSlotData().put("quest_id", questId);
            }
        }
        if (questId == null || questId.isBlank()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source,
                    VisionIntent.CANCEL_QUEST, "quest_id", "Which quest should I cancel?");
        }
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY
                && VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                return cancelQuestReviewTurn(conversation, source, "The quest cancellation review is ready, but execution is disabled.");
            }
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) {
                return cancelQuestReviewTurn(conversation, source, result.getBlockingReason());
            }
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", "quest_cancelled");
            String message = "Quest cancelled successfully. Affected applicants and workers were notified.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.CANCEL_QUEST,
                    VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can cancel quest " + questId + " and notify affected applicants or workers. Confirm to continue.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.CANCEL_QUEST,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn cancelQuestReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", VisionIntent.CANCEL_QUEST,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                false, true, message);
    }

    private VisionTurn handlePauseQuestTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source,
            boolean resume
    ) {
        String questId = conversation.getSlotData().get("quest_id");
        if (questId == null || questId.isBlank()) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:quest|job|work)?\\s*#?(\\d+)").matcher(normalizedPrompt == null ? "" : normalizedPrompt);
            if (matcher.find()) { questId = matcher.group(1); conversation.getSlotData().put("quest_id", questId); }
        }
        VisionIntent intent = resume ? VisionIntent.RESUME_QUEST : VisionIntent.PAUSE_QUEST;
        String action = resume ? "resume" : "pause";
        if (questId == null || questId.isBlank()) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, intent, "quest_id", "Which quest should I " + action + "?");
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) return pauseQuestReviewTurn(conversation, source, intent, "The quest " + action + " review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return pauseQuestReviewTurn(conversation, source, intent, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", "quest_" + (resume ? "resumed" : "paused"));
            String message = "Quest " + action + "d successfully.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, intent, VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can " + action + " quest " + questId + ". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, intent, VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn pauseQuestReviewTurn(VisionConversation conversation, String source, VisionIntent intent, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", intent, VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, false, true, message);
    }

    private VisionTurn handleCreateThingTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String title = conversation.getSlotData().get("thing_title");
        if (title == null || title.isBlank()) {
            title = normalizedPrompt == null ? "" : normalizedPrompt.trim();
            title = title.replaceFirst("(?i)^(create|offer|list|share)(?:\\s+(?:a|an|my))?(?:\\s+thing)?\\s*", "").trim();
            if (!title.isBlank()) {
                conversation.getSlotData().put("thing_title", title);
            }
        }
        if (title == null || title.isBlank()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source,
                    VisionIntent.CREATE_THING, "thing_title", "What thing would you like to list?");
        }
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY
                && VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                return createThingReviewTurn(conversation, source, "The thing listing review is ready, but execution is disabled.");
            }
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) {
                return createThingReviewTurn(conversation, source, result.getBlockingReason());
            }
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", "thing_created");
            String message = "Thing listing created successfully.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.CREATE_THING,
                    VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can create a thing listing for: " + title + ". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.CREATE_THING,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn createThingReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", VisionIntent.CREATE_THING,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                false, true, message);
    }

    private VisionTurn handleRideTurn(
            VisionConversation conversation, String prompt, String normalizedPrompt,
            VisionPromptUnderstandingResult understanding, String source) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt.trim();
        VisionIntent intent = conversation.getIntent();
        if (intent == VisionIntent.CREATE_RIDE) {
            if (!conversation.getSlotData().containsKey("ride_origin")) {
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:from|origin)\\s+(.+?)\\s+(?:to|destination)\\s+(.+?)(?=\\s+(?:at|on|departure|seats?)\\b|$)").matcher(value);
                if (matcher.find()) { conversation.getSlotData().put("ride_origin", matcher.group(1).trim()); conversation.getSlotData().put("ride_destination", matcher.group(2).trim()); }
            }
            if (!conversation.getSlotData().containsKey("ride_departure_at")) {
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b(20\\d{2}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(?::\\d{2}(?:Z|[+-]\\d{2}:?\\d{2})?)?)\\b").matcher(value);
                if (matcher.find()) conversation.getSlotData().put("ride_departure_at", matcher.group(1));
            }
            if (!conversation.getSlotData().containsKey("ride_seats")) {
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)\\b(?:seats?|passengers?)\\s*[:=]?\\s*(\\d+)\\b").matcher(value);
                if (matcher.find()) conversation.getSlotData().put("ride_seats", matcher.group(1));
            }
            if (!conversation.getSlotData().containsKey("ride_origin")) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, intent, "ride_origin", "Where would the ride start? Use: from ... to ...");
            if (!conversation.getSlotData().containsKey("ride_destination")) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, intent, "ride_destination", "Where is the ride going?");
            if (!conversation.getSlotData().containsKey("ride_departure_at")) return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, intent, "ride_departure_at", "What future departure time should I use? Use ISO format, for example 2099-07-20T10:00:00Z.");
            if (!conversation.getSlotData().containsKey("ride_seats")) conversation.getSlotData().put("ride_seats", "1");
        } else if (!conversation.getSlotData().containsKey("ride_id")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:ride|offer|trip)?\\s*#?(\\d+)\\b").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("ride_id", matcher.group(1));
            else return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, intent, "ride_id", "Which ride should I use? Give me its ride number.");
        }
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) return rideReviewTurn(conversation, source, "The ride review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return rideReviewTurn(conversation, source, result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            String outcome = intent.name().toLowerCase(java.util.Locale.ROOT);
            conversation.getSlotData().put("conversation_outcome", outcome);
            String message = "Ride action completed successfully.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, intent, VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = intent == VisionIntent.CREATE_RIDE
                ? "I can offer a ride from " + conversation.getSlotData().get("ride_origin") + " to " + conversation.getSlotData().get("ride_destination") + " at " + conversation.getSlotData().get("ride_departure_at") + " with " + conversation.getSlotData().get("ride_seats") + " passenger seat(s). Confirm to continue."
                : "I can perform " + intent.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ') + " for ride " + conversation.getSlotData().get("ride_id") + ". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, intent, VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn rideReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true); visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", conversation.getIntent(), VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null, false, true, message);
    }

    private VisionTurn handleViewRidesTurn(VisionConversation conversation, String prompt, String normalizedPrompt, VisionPromptUnderstandingResult understanding, String source) {
        String message = "Rides are available in the Web Rides surface. I can also help you offer, join, leave, update, cancel, start, or complete a ride when you give me a ride number.";
        if (rideOfferService != null) {
            List<RideOfferResponseDTO> offers = rideOfferService.getVisibleOffers(conversation.getOwner()).getItems();
            if (offers.isEmpty()) {
                message = "There are no rides currently visible to you. I can help you offer one.";
            } else {
                message = "Visible rides:\n" + offers.stream().limit(8)
                        .map(ride -> "#" + ride.getId() + " " + ride.getOrigin() + " → " + ride.getDestination()
                                + " · " + ride.getDepartureAt() + " · " + ride.getJoinedSeats() + "/" + ride.getSeats() + " seats")
                        .collect(java.util.stream.Collectors.joining("\n"));
            }
        }
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.VIEW_RIDES, VisionAgentState.RECOMMENDING, VisionNextAction.SHOW_RESULTS, null, understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn handleRequestBorrowTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String listingId = conversation.getSlotData().get("thing_listing_id");
        if (listingId == null || listingId.isBlank()) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:thing|listing)?\\s*#?(\\d+)").matcher(normalizedPrompt == null ? "" : normalizedPrompt);
            if (matcher.find()) {
                listingId = matcher.group(1);
                conversation.getSlotData().put("thing_listing_id", listingId);
            }
        }
        if (listingId == null || listingId.isBlank()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source,
                    VisionIntent.REQUEST_BORROW, "thing_listing_id", "Which thing listing would you like to borrow?");
        }
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY
                && VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                return requestBorrowReviewTurn(conversation, source, "The borrow request review is ready, but execution is disabled.");
            }
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) {
                return requestBorrowReviewTurn(conversation, source, result.getBlockingReason());
            }
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", "borrow_requested");
            String message = "Borrow request sent successfully.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.REQUEST_BORROW,
                    VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = "I can request to borrow thing listing " + listingId + ". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.REQUEST_BORROW,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn requestBorrowReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", VisionIntent.REQUEST_BORROW,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                false, true, message);
    }

    private VisionTurn handleBorrowLifecycleTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source,
            VisionIntent intent,
            String capabilityId,
            String reviewPrefix,
            String outcome
    ) {
        String requestId = conversation.getSlotData().get("borrow_request_id");
        if (requestId == null || requestId.isBlank()) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:borrow|request)?\\s*#?(\\d+)").matcher(normalizedPrompt == null ? "" : normalizedPrompt);
            if (matcher.find()) {
                requestId = matcher.group(1);
                conversation.getSlotData().put("borrow_request_id", requestId);
            }
        }
        if (intent == VisionIntent.DECIDE_BORROW && !conversation.getSlotData().containsKey("borrow_approve")) {
            String lower = normalizedPrompt == null ? "" : normalizedPrompt.toLowerCase(java.util.Locale.ROOT);
            if (lower.contains("decline") || lower.contains("reject") || lower.contains("deny")) {
                conversation.getSlotData().put("borrow_approve", "false");
            } else if (lower.contains("approve") || lower.contains("accept")) {
                conversation.getSlotData().put("borrow_approve", "true");
            }
        }
        if (requestId == null || requestId.isBlank()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, intent,
                    "borrow_request_id", "Which borrow request id should I use?");
        }
        if (intent == VisionIntent.DECIDE_BORROW && !conversation.getSlotData().containsKey("borrow_approve")) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, intent,
                    "borrow_decision", "Should I approve or decline this borrow request?");
        }
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY
                && VisionReviewInteractionSupport.isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                return borrowLifecycleReviewTurn(conversation, source, "The borrow request review is ready, but execution is disabled.", intent);
            }
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) {
                return borrowLifecycleReviewTurn(conversation, source, result.getBlockingReason(), intent);
            }
            conversation.setStatus(VisionConversationStatus.COMPLETED);
            conversation.getSlotData().put("conversation_outcome", outcome);
            String message = "Borrow request action completed.";
            updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
            visionConversationRepository.save(conversation);
            return createTurn(conversation, source, prompt, normalizedPrompt, intent,
                    VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null,
                    understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        String message = reviewPrefix + requestId + ". Confirm to continue.";
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, prompt, normalizedPrompt, intent,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
    }

    private VisionTurn borrowLifecycleReviewTurn(VisionConversation conversation, String source, String message, VisionIntent intent) {
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", intent,
                VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                false, true, message);
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
            VisionConversation existingConversation,
            String clientRequestId
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
        if (clientRequestId != null) {
            Optional<VisionConversation> replayConversation =
                    visionConversationRepository.findFirstByOwnerAndLastClientRequestIdOrderByUpdatedAtDesc(currentUser, clientRequestId);
            if (replayConversation.isPresent()) {
                return replayConversation.get();
            }
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

    private VisionConversationTurnResponseDTO replayConversation(
            VisionConversation conversation,
            VisionTurn turn,
            AppUser currentUser,
            VisionPromptUnderstandingResult understanding
    ) {
        return assembleConversationResponse(
                conversation,
                turn,
                currentUser,
                understanding,
                visionExecutionPlanner.plan(conversation, understanding)
        );
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
            case RELEASE_WORKER, REPLACE_WORKER -> handleWorkerManagementTurn(conversation, "confirm", "confirm", understanding, source);
            case REOPEN_QUEST -> handleReopenQuestTurn(conversation, "confirm", "confirm", understanding, source);
            case PAUSE_QUEST -> handlePauseQuestTurn(conversation, "confirm", "confirm", understanding, source, false);
            case RESUME_QUEST -> handlePauseQuestTurn(conversation, "confirm", "confirm", understanding, source, true);
            case RESCHEDULE_BOOKING -> handleRescheduleBookingTurn(conversation, "confirm", "confirm", understanding, source);
            case MARK_CHAT_READ -> handleMarkChatReadTurn(conversation, "confirm", "confirm", understanding, source);
            case MARK_NOTIFICATIONS_READ -> handleMarkNotificationsReadTurn(conversation, "confirm", "confirm", understanding, source);
            case MARK_NOTIFICATION_READ -> handleMarkNotificationReadTurn(conversation, "confirm", "confirm", understanding, source);
            case UPDATE_NOTIFICATION_PREFERENCES -> handleUpdateNotificationPreferencesTurn(conversation, "confirm", "confirm", understanding, source);
            case CREATE_RIDE, JOIN_RIDE, UPDATE_RIDE, LEAVE_RIDE, CANCEL_RIDE, START_RIDE, COMPLETE_RIDE -> handleRideConfirmation(conversation, understanding, source);
            default -> throw ServiceErrors.conflict("Review confirmation is not supported for this vision conversation");
        };
    }

    private VisionTurn handleRideConfirmation(VisionConversation conversation, VisionPromptUnderstandingResult understanding, String source) {
        if (conversation.getStatus() != VisionConversationStatus.REVIEW_READY) throw ServiceErrors.conflict("Ride review is not ready for confirmation");
        if (!visionProperties.isExecutionEnabled()) return rideReviewTurn(conversation, source, "The ride review is ready, but execution is disabled.");
        VisionExecutionResult result = visionExecutionService.execute(conversation);
        if (!result.isExecuted()) return rideReviewTurn(conversation, source, result.getBlockingReason());
        conversation.setStatus(VisionConversationStatus.COMPLETED);
        conversation.getSlotData().put("conversation_outcome", conversation.getIntent().name().toLowerCase(java.util.Locale.ROOT));
        String message = "Ride action completed successfully.";
        updateConversationMetadata(conversation, "", "", message, understanding.isTranslationReliable());
        visionConversationRepository.save(conversation);
        return createTurn(conversation, source, "", "", conversation.getIntent(), VisionAgentState.COMPLETE, VisionNextAction.COMPLETE, null, false, true, message);
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
                .clientDeviceRole(dto.getClientDeviceRole())
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
        String clientDeviceRole = cleanRuntimeHint(runtimeHints.getClientDeviceRole());
        if (clientDeviceRole != null) {
            conversation.getSlotData().put("client_device_role", clientDeviceRole);
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
        return assembleConversationResponse(conversation, turn, currentUser, discoveryUnderstanding, executionPlan, null);
    }

    private VisionConversationTurnResponseDTO assembleConversationResponse(
            VisionConversation conversation,
            VisionTurn turn,
            AppUser currentUser,
            VisionPromptUnderstandingResult discoveryUnderstanding,
            VisionExecutionCandidateDTO executionPlan,
            VisionWorkspaceHandoffDTO workspaceHandoff
    ) {
        return visionCanvasAssembler.assemble(
                conversation,
                turn,
                visionConversationReadModelAssembler.recentConversationSummaries(currentUser),
                executionPlan,
                visionQuestDiscoveryService.discover(conversation, discoveryUnderstanding, currentUser),
                searchDiscoveryForConversation(conversation, currentUser),
                VisionConversationSnapshotSupport.capabilityPreview(conversation, currentUser, visionCapabilityPreviewService),
                visionConversationReadModelAssembler.buildMemoryTrail(currentUser, conversation),
                workspaceHandoff,
                searchComparisonForConversation(conversation, currentUser)
        );
    }

    private VisionWorkspaceHandoffDTO workspaceHandoff(VisionConversationTurnRequestDTO dto, VisionTurn turn) {
        if (dto == null) {
            return null;
        }
        String contextLabel = cleanWorkspaceValue(dto.getWorkspaceContext(), 120);
        String source = cleanWorkspaceSource(dto.getWorkspaceSource());
        String returnTo = cleanWorkspaceReturnTo(dto.getWorkspaceReturnTo());
        if (contextLabel == null && source == null && returnTo == null
                && isWorkspaceNavigationPrompt(dto.getEffectivePrompt()) && turn != null) {
            return inferredWorkspaceNavigation(turn.getDetectedIntent(), dto.getEffectivePrompt());
        }
        if (contextLabel == null && source == null && returnTo == null) {
            return null;
        }
        return VisionWorkspaceHandoffDTO.builder()
                .contractVersion("workspace-v1")
                .contextLabel(contextLabel)
                .source(source)
                .returnTo(returnTo)
                .explanation(contextLabel == null ? "Opened from the workspace." : "Opened from " + contextLabel + ".")
                .build();
    }

    private VisionWorkspaceHandoffDTO inferredWorkspaceNavigation(VisionIntent intent, String prompt) {
        if (intent == null) {
            return null;
        }
        String normalizedPrompt = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        String returnTo = switch (intent) {
            case VIEW_PROFILE -> normalizedPrompt.contains("visibility")
                    ? "/profile/settings?visibility=1"
                    : "/profile";
            case VIEW_SETTINGS -> normalizedPrompt.contains("current location")
                    ? "/profile/settings?location=current"
                    : normalizedPrompt.contains("visibility")
                    ? "/profile/settings?visibility=1"
                    : "/settings";
            case VIEW_BUSINESS -> "/business";
            case VIEW_BUSINESS_AVAILABILITY -> "/business/availability";
            case VIEW_BUSINESS_BOOKINGS -> "/business/bookings";
            case VIEW_CIRCLES -> "/circles";
            case VIEW_NOTIFICATIONS -> "/notifications";
            case VIEW_ACTIVITY -> "/activity";
            case VIEW_APPLICATIONS -> "/work/applications";
            case VIEW_THINGS -> "/things";
            case VIEW_BORROW_REQUESTS -> "/things/requests";
            case VIEW_RIDES -> "/rides";
            case VIEW_CHAT_WORKSPACE, OPEN_CHAT, SYNC_CHAT -> normalizedPrompt.contains("attach")
                    ? "/chat?attach=1"
                    : "/chat";
            case VIEW_QUEST_NEWS -> "/activity";
            default -> null;
        };
        if (returnTo == null) {
            return null;
        }
        return VisionWorkspaceHandoffDTO.builder()
                .contractVersion("workspace-v1")
                .contextLabel("the " + intent.name().toLowerCase(Locale.ROOT).replace('_', ' ') + " workspace")
                .source("shell.surface.vision-navigation")
                .returnTo(returnTo)
                .explanation("Open this result in the authenticated workspace.")
                .build();
    }

    private boolean isWorkspaceNavigationPrompt(String prompt) {
        String normalized = cleanRuntimeHint(prompt);
        return normalized != null && normalized.toLowerCase(Locale.ROOT)
                .matches(".*(\\bopen\\b|\\bgo to\\b|\\bnavigate to\\b|\\btake me to\\b|\\bshow in workspace\\b|\\bshow .*\\bworkspace\\b).*");
    }

    private String cleanWorkspaceValue(String value, int maxLength) {
        String cleaned = cleanRuntimeHint(value);
        return cleaned == null || cleaned.length() > maxLength ? null : cleaned;
    }

    private String cleanWorkspaceSource(String value) {
        String cleaned = cleanWorkspaceValue(value, 100);
        return cleaned != null && cleaned.matches("shell\\.surface\\.[a-z0-9-]+") ? cleaned : null;
    }

    private String cleanWorkspaceReturnTo(String value) {
        String cleaned = cleanWorkspaceValue(value, 240);
        return cleaned != null && cleaned.startsWith("/") && !cleaned.startsWith("//")
                && !cleaned.contains("://") && !cleaned.contains("\\") ? cleaned : null;
    }

    private String clientRequestId(VisionConversationTurnRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return cleanRuntimeHint(dto.getClientRequestId());
    }

    private boolean isDuplicateClientRequest(VisionConversation conversation, String clientRequestId) {
        if (conversation == null || clientRequestId == null) {
            return false;
        }
        if (clientRequestId.equals(conversation.getLastClientRequestId())) {
            return true;
        }
        return conversation.getSlotData() != null
                && clientRequestId.equals(conversation.getSlotData().get("last_client_request_id"));
    }

    private boolean isConversationReplayCandidate(VisionConversation conversation, String clientRequestId) {
        if (conversation == null || clientRequestId == null) {
            return false;
        }
        return clientRequestId.equals(conversation.getLastClientRequestId())
                || (conversation.getSlotData() != null
                && clientRequestId.equals(conversation.getSlotData().get("last_client_request_id")));
    }

    private void rememberClientRequestId(VisionConversation conversation, String clientRequestId) {
        if (conversation == null || clientRequestId == null) {
            return;
        }
        if (conversation.getSlotData() == null) {
            conversation.setSlotData(new LinkedHashMap<>());
        }
        conversation.setLastClientRequestId(clientRequestId);
        conversation.getSlotData().put("last_client_request_id", clientRequestId);
        visionConversationRepository.save(conversation);
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
                    ? visionClarificationService.buildCreateQuestRetryGuidanceQuestion(missingSlot, visionConversationLifecycleSupport.userMemoryFor(conversation))
                    : visionClarificationService.buildCreateQuestGuidanceQuestion(missingSlot, visionConversationLifecycleSupport.userMemoryFor(conversation));
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

    private VisionTurn handleLeaveCircleTurn(
            VisionConversation conversation, String prompt, String normalizedPrompt,
            VisionPromptUnderstandingResult understanding, String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        if (!conversation.getSlotData().containsKey("circle_id")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)circle\\s*#?(\\d+)").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("circle_id", matcher.group(1));
        }
        if (!conversation.getSlotData().containsKey("circle_id")) return askForSlot(conversation,prompt,normalizedPrompt,understanding,source,VisionIntent.LEAVE_CIRCLE,"circle_id","Which circle id should I leave?");
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY && VisionReviewInteractionSupport.isConfirmationPrompt(value)) {
            if (!visionProperties.isExecutionEnabled()) return leaveCircleReviewTurn(conversation,source,"The circle leave review is ready, but execution is disabled.");
            VisionExecutionResult result = visionExecutionService.execute(conversation);
            if (!result.isExecuted()) return leaveCircleReviewTurn(conversation,source,result.getBlockingReason());
            conversation.setStatus(VisionConversationStatus.COMPLETED); conversation.getSlotData().put("conversation_outcome","circle_left");
            String message="Circle membership removed."; updateConversationMetadata(conversation,prompt,value,message,understanding.isTranslationReliable()); visionConversationRepository.save(conversation);
            return createTurn(conversation,source,prompt,value,VisionIntent.LEAVE_CIRCLE,VisionAgentState.COMPLETE,VisionNextAction.COMPLETE,null,understanding.isTranslationApplied(),understanding.isTranslationReliable(),message);
        }
        conversation.setStatus(VisionConversationStatus.REVIEW_READY); String message="I can remove you from circle " + conversation.getSlotData().get("circle_id") + ". Confirm to continue.";
        updateConversationMetadata(conversation,prompt,value,message,understanding.isTranslationReliable()); visionConversationRepository.save(conversation);
        return createTurn(conversation,source,prompt,value,VisionIntent.LEAVE_CIRCLE,VisionAgentState.REVIEW_READY,VisionNextAction.SHOW_REVIEW,null,understanding.isTranslationApplied(),understanding.isTranslationReliable(),message);
    }

    private VisionTurn leaveCircleReviewTurn(VisionConversation conversation, String source, String message) {
        updateConversationMetadata(conversation,"","",message,true); visionConversationRepository.save(conversation);
        return createTurn(conversation,source,"","",VisionIntent.LEAVE_CIRCLE,VisionAgentState.REVIEW_READY,VisionNextAction.SHOW_REVIEW,null,false,true,message);
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
        conversation.getSlotData().put("search_page", "0");
        conversation.getSlotData().put("discovery_page", "0");
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
        boolean comparisonPrompt = isComparisonPrompt(normalizedPrompt);
        String storedSearchQuery = conversation.getSlotData() == null
                ? ""
                : conversation.getSlotData().getOrDefault("search_query", "");
        VisionSearchDiscoveryDTO discovery = comparisonPrompt
                ? visionSearchDiscoveryService.discoverWeb(conversation.getOwner(), storedSearchQuery, 0)
                : visionSearchDiscoveryService.discover(conversation, understanding, conversation.getOwner());
        if (discovery == null) {
            throw ServiceErrors.conflict("Search is not available for this vision conversation");
        }

        conversation.setStatus(VisionConversationStatus.ACTIVE);
        conversation.setRequestedSlot(null);
        conversation.getSlotData().put("search_query", discovery.getQuery() == null ? "" : discovery.getQuery());
        conversation.getSlotData().remove("comparison_selections");
        String message = discovery.getSummary();
        if (comparisonPrompt) {
            List<String> selections = comparisonSelections(normalizedPrompt, discovery);
            if (selections.isEmpty()) {
                message = discovery.getSummary() + " Select up to three results by saying ‘compare first and second’ or naming keys such as quest:12.";
            } else {
                conversation.getSlotData().put("comparison_selections", String.join(",", selections));
                VisionSearchComparisonDTO comparison = visionSearchDiscoveryService.compareVision(
                        conversation.getOwner(),
                        discovery.getQuery(),
                        selections
                );
                message = comparison.getItems().size() + " permitted result" + (comparison.getItems().size() == 1 ? "" : "s") + " compared."
                        + (comparison.getFallbackMessage() == null ? "" : " " + comparison.getFallbackMessage());
            }
        }
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

    private VisionSearchComparisonDTO searchComparisonForConversation(VisionConversation conversation, AppUser currentUser) {
        if (conversation == null || conversation.getIntent() != VisionIntent.SEARCH || conversation.getSlotData() == null) {
            return null;
        }
        String rawSelections = conversation.getSlotData().get("comparison_selections");
        if (rawSelections == null || rawSelections.isBlank()) {
            return null;
        }
        List<String> selections = List.of(rawSelections.split(",")).stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
        return visionSearchDiscoveryService.compareVision(
                currentUser,
                conversation.getSlotData().getOrDefault("search_query", ""),
                selections
        );
    }

    private boolean isComparisonPrompt(String normalizedPrompt) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt.toLowerCase(Locale.ROOT);
        return value.contains("compare")
                || value.contains("contrast")
                || value.contains("side by side")
                || value.contains("which is better");
    }

    private List<String> comparisonSelections(String normalizedPrompt, VisionSearchDiscoveryDTO discovery) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt.toLowerCase(Locale.ROOT);
        Set<String> selections = new LinkedHashSet<>();
        Matcher matcher = Pattern.compile("\\b(quest|thing|circle|user|application)\\s*[:#]\\s*(\\d+)\\b").matcher(value);
        while (matcher.find() && selections.size() < 3) {
            selections.add(matcher.group(1) + ":" + matcher.group(2));
        }
        if (discovery != null && discovery.getItems() != null) {
            String[] ordinalSignals = {"first", "1st", "second", "2nd", "third", "3rd"};
            for (int index = 0; index < ordinalSignals.length && selections.size() < 3; index++) {
                if (value.contains(ordinalSignals[index])) {
                    int itemIndex = index / 2;
                    if (itemIndex < discovery.getItems().size()) {
                        var item = discovery.getItems().get(itemIndex);
                        selections.add(item.getEntityFamily() + ":" + item.getTargetId());
                    }
                }
            }
        }
        return new ArrayList<>(selections);
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
        String lower = TextValueNormalizer.lowerTrimToEmpty(normalizedPrompt);
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

    private VisionTurn handleViewAccessibleCircleTurn(
            VisionConversation conversation, String prompt, String normalizedPrompt,
            VisionPromptUnderstandingResult understanding, String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        if (!conversation.getSlotData().containsKey("accessible_circle_id")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)circle\\s*#?(\\d+)").matcher(value);
            if (matcher.find()) conversation.getSlotData().put("accessible_circle_id", matcher.group(1));
        }
        if (!conversation.getSlotData().containsKey("accessible_circle_id")) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source,
                    VisionIntent.VIEW_ACCESSIBLE_CIRCLE, "accessible_circle_id", "Which accessible circle id should I open?");
        }
        return handleReadOnlySnapshotTurn(conversation, prompt, normalizedPrompt, understanding, source,
                VisionIntent.VIEW_ACCESSIBLE_CIRCLE,
                VisionConversationSnapshotSupport.readOnlySnapshotMessage(VisionIntent.VIEW_ACCESSIBLE_CIRCLE));
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

    private VisionTurn handleViewThingDetailTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)(?:thing|listing)\\s*(?:#|id\\s*)?(\\d+)").matcher(value);
        if (!conversation.getSlotData().containsKey("thing_listing_id") && matcher.find()) {
            conversation.getSlotData().put("thing_listing_id", matcher.group(1));
        }
        if (!hasText(conversation.getSlotData().get("thing_listing_id"))) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source,
                    VisionIntent.VIEW_THING_DETAIL, "thing_listing_id",
                    "What thing listing should I open? Say its listing id.");
        }
        return handleReadOnlySnapshotTurn(conversation, prompt, normalizedPrompt, understanding, source,
                VisionIntent.VIEW_THING_DETAIL,
                VisionConversationSnapshotSupport.readOnlySnapshotMessage(VisionIntent.VIEW_THING_DETAIL));
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

    VisionTurn handleViewChatAttachmentTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String value = normalizedPrompt == null ? "" : normalizedPrompt;
        java.util.regex.Matcher conversationMatcher = java.util.regex.Pattern
                .compile("(?i)(?:chat|conversation|thread)\\s*#?(\\d+)")
                .matcher(value);
        if (!conversation.getSlotData().containsKey("chat_conversation_id") && conversationMatcher.find()) {
            conversation.getSlotData().put("chat_conversation_id", conversationMatcher.group(1));
        }
        java.util.regex.Matcher messageMatcher = java.util.regex.Pattern
                .compile("(?i)(?:message|attachment)\\s*#?(\\d+)")
                .matcher(value);
        if (!conversation.getSlotData().containsKey("chat_message_id") && messageMatcher.find()) {
            conversation.getSlotData().put("chat_message_id", messageMatcher.group(1));
        }
        if (!conversation.getSlotData().containsKey("chat_conversation_id")) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(),
                    "chat_conversation_id", "Which chat conversation id should I use?");
        }
        if (!conversation.getSlotData().containsKey("chat_message_id")) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, conversation.getIntent(),
                    "chat_message_id", "Which chat message id contains the attachment?");
        }
        return handleReadOnlySnapshotTurn(conversation, prompt, normalizedPrompt, understanding, source,
                VisionIntent.VIEW_CHAT_ATTACHMENT,
                VisionConversationSnapshotSupport.readOnlySnapshotMessage(VisionIntent.VIEW_CHAT_ATTACHMENT));
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
