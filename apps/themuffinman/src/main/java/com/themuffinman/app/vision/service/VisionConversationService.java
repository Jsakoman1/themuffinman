package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.vision.dto.VisionConversationTurnRequestDTO;
import com.themuffinman.app.vision.dto.VisionConversationListResponseDTO;
import com.themuffinman.app.vision.dto.VisionConversationSummaryDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.vision.dto.VisionLearningMemoryDTO;
import com.themuffinman.app.vision.dto.VisionMemoryTrailDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.vision.dto.VisionQuestDiscoveryDTO;
import com.themuffinman.app.vision.dto.VisionSearchDiscoveryDTO;
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

import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class VisionConversationService {

    private static final double MIN_TOPIC_SWITCH_CONFIDENCE = 0.60d;
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().findAndAddModules().build();

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
        VisionSemanticRuntimeHints runtimeHints = dto == null ? null : VisionSemanticRuntimeHints.builder()
                .inputType(dto.getInputType())
                .clientLocale(dto.getClientLocale())
                .clientTimezone(dto.getClientTimezone())
                .clientCapabilities(dto.getClientCapabilities())
                .clientStateVersion(dto.getClientStateVersion())
                .build();
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
        ensureTurnCapacity(conversation);

        VisionTurn turn = switch (action) {
            case CONFIRM_REVIEW -> handleConfirmReviewAction(conversation, understanding, dto == null ? null : dto.getEffectiveSource());
            case REQUEST_REVIEW_EDIT -> handleReviewEditAction(conversation, understanding, dto == null ? null : dto.getEffectiveSource(), dto);
            case SUBMIT_PROMPT -> switch (conversation.getIntent()) {
                case CREATE_QUEST -> handleCreateQuestTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case CREATE_CIRCLE -> handleCreateCircleTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case CREATE_CIRCLE_REQUEST -> handleCreateCircleRequestTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case ACCEPT_CIRCLE_REQUEST -> handleAcceptCircleRequestTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case DELETE_CIRCLE_REQUEST -> handleDeleteCircleRequestTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case UPDATE_CIRCLE -> handleUpdateCircleTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case DELETE_CIRCLE -> handleDeleteCircleTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case CREATE_APPLICATION -> handleCreateApplicationTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case UPDATE_APPLICATION -> handleUpdateApplicationTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case WITHDRAW_APPLICATION -> handleWithdrawApplicationTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case APPROVE_APPLICATION -> handleApproveApplicationTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case DECLINE_APPLICATION -> handleDeclineApplicationTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case UPDATE_PROFILE -> handleUpdateProfileTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case UPDATE_PROFILE_LOCATION -> handleUpdateProfileLocationTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case DISCOVER_QUESTS -> handleDiscoverQuestsTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case SEARCH -> handleSearchTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case OPEN_CHAT -> handleOpenChatTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case VIEW_CHAT_WORKSPACE -> handleViewChatWorkspaceTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case VIEW_PROFILE -> handleViewProfileTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case VIEW_SETTINGS -> handleViewSettingsTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case VIEW_USER_PROFILE -> handleViewUserProfileTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case VIEW_CIRCLES -> handleViewCirclesTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case VIEW_CIRCLE_DETAIL -> handleViewCircleDetailTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case VIEW_QUEST_DETAIL -> handleViewQuestDetailTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case VIEW_NOTIFICATIONS -> handleViewNotificationsTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case VIEW_QUEST_NEWS -> handleViewQuestNewsTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case VIEW_APPLICATIONS -> handleViewApplicationsTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case VIEW_APPLICATION_DETAIL -> handleViewApplicationDetailTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case VIEW_THINGS -> handleViewThingsTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
                case UNSUPPORTED -> handleUnsupportedTurn(conversation, prompt, normalizedPrompt, understanding, dto.getEffectiveSource());
            };
        };

        if (visionLearningService != null) {
            visionLearningService.recordTurnOutcome(
                    currentUser,
                    conversation,
                    turn,
                    understanding,
                    dto == null ? null : VisionSemanticRuntimeHints.builder()
                            .inputType(dto.getInputType())
                            .clientLocale(dto.getClientLocale())
                            .clientTimezone(dto.getClientTimezone())
                            .clientCapabilities(dto.getClientCapabilities())
                            .clientStateVersion(dto.getClientStateVersion())
                            .build(),
                    action
            );
        }

        return visionCanvasAssembler.assemble(
                conversation,
                turn,
                understanding.getUnderstandingProvider(),
                understanding.getUnderstandingStatus(),
                recentConversationSummaries(currentUser),
                visionExecutionPlanner.plan(conversation, understanding),
                visionQuestDiscoveryService.discover(conversation, understanding, currentUser),
                visionSearchDiscoveryService.discover(conversation, understanding, currentUser),
                capabilityPreview(conversation, currentUser),
                learningMemory(currentUser),
                buildMemoryTrail(currentUser, conversation)
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
        if (visionLearningService != null) {
            visionLearningService.recordTurnOutcome(
                    currentUser,
                    conversation,
                    turn,
                    VisionPromptUnderstandingResult.empty(""),
                    null,
                    VisionConversationAction.SUBMIT_PROMPT
            );
        }
        return visionCanvasAssembler.assemble(
                conversation,
                turn,
                "none",
                "not_applicable",
                recentConversationSummaries(currentUser),
                visionExecutionPlanner.plan(conversation),
                visionQuestDiscoveryService.discover(conversation, VisionPromptUnderstandingResult.empty(""), currentUser),
                searchDiscoveryForConversation(conversation, currentUser),
                capabilityPreview(conversation, currentUser),
                learningMemory(currentUser),
                buildMemoryTrail(currentUser, conversation)
        );
    }

    @Transactional
    public VisionConversationTurnResponseDTO cancelConversation(Long conversationId, AppUser currentUser) {
        validateAccess(currentUser);
        VisionConversation conversation = loadExistingConversation(conversationId, currentUser);
        ensureTurnCapacity(conversation);
        VisionTurn turn = handleCancelConversationAction(conversation, "system");
        if (visionLearningService != null) {
            visionLearningService.recordTurnOutcome(
                    currentUser,
                    conversation,
                    turn,
                    VisionPromptUnderstandingResult.empty(""),
                    null,
                    VisionConversationAction.SUBMIT_PROMPT
            );
        }
        return visionCanvasAssembler.assemble(
                conversation,
                turn,
                "none",
                "not_applicable",
                recentConversationSummaries(currentUser),
                visionExecutionPlanner.plan(conversation),
                visionQuestDiscoveryService.discover(conversation, VisionPromptUnderstandingResult.empty(""), currentUser),
                searchDiscoveryForConversation(conversation, currentUser),
                capabilityPreview(conversation, currentUser),
                learningMemory(currentUser),
                buildMemoryTrail(currentUser, conversation)
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
                "unavailable",
                "historical_turn",
                recentConversationSummaries(currentUser),
                visionExecutionPlanner.plan(conversation),
                visionQuestDiscoveryService.discover(conversation, VisionPromptUnderstandingResult.empty(""), currentUser),
                searchDiscoveryForConversation(conversation, currentUser),
                capabilityPreview(conversation, currentUser),
                learningMemory(currentUser),
                buildMemoryTrail(currentUser, conversation)
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
                if (sameIntentWorkspaceFamily(existingConversation.getIntent(), detectedIntent)) {
                    existingConversation.setIntent(detectedIntent);
                    existingConversation.setStatus(detectedIntent == VisionIntent.UNSUPPORTED
                            ? VisionConversationStatus.BLOCKED
                            : VisionConversationStatus.ACTIVE);
                    return visionConversationRepository.save(existingConversation);
                }
                if (shouldKeepExistingConversation(normalizedPrompt, detectedIntent, semanticIntentConfidence)) {
                    return existingConversation;
                }
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

    private boolean shouldKeepExistingConversation(String normalizedPrompt, VisionIntent detectedIntent, double semanticIntentConfidence) {
        if (semanticIntentConfidence >= MIN_TOPIC_SWITCH_CONFIDENCE) {
            return false;
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return true;
        }
        String lower = normalizedPrompt.toLowerCase(java.util.Locale.ROOT);
        if (containsExplicitEntityFamilySignal(lower, detectedIntent)) {
            return false;
        }
        return isLikelyFollowUpPrompt(lower);
    }

    private boolean containsAny(String value, String... candidates) {
        if (value == null) {
            return false;
        }
        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            if (containsCandidate(value, candidate)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsCandidate(String value, String candidate) {
        if (candidate.indexOf(' ') >= 0) {
            return value.contains(candidate);
        }
        return Pattern.compile("\\b" + Pattern.quote(candidate) + "\\b").matcher(value).find();
    }

    private boolean containsExplicitEntityFamilySignal(String value, VisionIntent intent) {
        if (value == null || value.isBlank() || intent == null) {
            return false;
        }
        return switch (intent) {
            case CREATE_QUEST, DISCOVER_QUESTS, VIEW_QUEST_DETAIL, VIEW_QUEST_NEWS -> containsAny(value, "quest", "quests", "job", "jobs", "work", "news", "updates", "activity feed");
            case VIEW_NOTIFICATIONS -> containsAny(value, "notification", "notifications", "notification center", "alerts", "alert");
            case CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST,
                 UPDATE_CIRCLE, DELETE_CIRCLE, VIEW_CIRCLES, VIEW_CIRCLE_DETAIL -> containsAny(value, "circle", "circles");
            case CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION, APPROVE_APPLICATION,
                 DECLINE_APPLICATION, VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL -> containsAny(value, "application", "applications");
            case UPDATE_PROFILE, UPDATE_PROFILE_LOCATION, VIEW_PROFILE, VIEW_USER_PROFILE -> containsAny(value, "profile", "username", "bio", "location", "settings");
            case OPEN_CHAT, VIEW_CHAT_WORKSPACE -> containsAny(value, "chat", "message", "dm", "talk");
            default -> false;
        };
    }

    private boolean isLikelyFollowUpPrompt(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String normalized = value.trim();
        int wordCount = normalized.split("\\s+").length;
        if (wordCount <= 2) {
            return true;
        }
        return containsAny(normalized,
                "and ",
                "also ",
                "what about",
                "same ",
                "that ",
                "this ",
                "instead",
                "change it",
                "switch it",
                "continue",
                "keep going");
    }

    private boolean sameIntentWorkspaceFamily(VisionIntent left, VisionIntent right) {
        return workspaceFamily(left) != null && workspaceFamily(left).equals(workspaceFamily(right));
    }

    private String workspaceFamily(VisionIntent intent) {
        return switch (intent) {
            case VIEW_PROFILE, VIEW_SETTINGS, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION -> "profile";
            case VIEW_NOTIFICATIONS -> "notifications";
            case VIEW_CIRCLES, VIEW_CIRCLE_DETAIL, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST,
                    DELETE_CIRCLE_REQUEST, UPDATE_CIRCLE, DELETE_CIRCLE -> "circles";
            case VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL, CREATE_APPLICATION, UPDATE_APPLICATION,
                    WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION -> "applications";
            case CREATE_QUEST, VIEW_QUEST_DETAIL, VIEW_QUEST_NEWS -> "quests";
            case VIEW_CHAT_WORKSPACE, OPEN_CHAT -> "chat";
            default -> null;
        };
    }

    private VisionConversation loadExistingConversation(Long conversationId, AppUser currentUser) {
        return visionConversationRepository.findByIdAndOwner(conversationId, currentUser)
                .orElseThrow(() -> ServiceErrors.notFound("Vision conversation was not found"));
    }

    private List<VisionConversationSummaryDTO> recentConversationSummaries(AppUser currentUser) {
        List<VisionConversation> recentConversations = visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(currentUser);
        List<VisionConversationSummaryDTO> summaries = new ArrayList<>();
        String previousEntityFamily = null;
        for (VisionConversation conversation : recentConversations) {
            String entityFamily = entityFamilyFor(conversation == null ? null : conversation.getIntent());
            summaries.add(toConversationSummary(conversation, entityFamily, previousEntityFamily));
            if (entityFamily != null && !entityFamily.isBlank()) {
                previousEntityFamily = entityFamily;
            }
        }
        return summaries;
    }

    private VisionMemoryTrailDTO buildMemoryTrail(AppUser currentUser, VisionConversation conversation) {
        VisionSemanticMemoryContext memoryContext =
                visionSemanticOrchestrationContextService.buildMemoryContext(currentUser, conversation);
        String activeEntityFamily = memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getCurrentEntityFamily();
        String previousEntityFamily = previousEntityFamily(memoryContext.getUserMemory() == null ? List.of() : memoryContext.getUserMemory().getRecentEntityFamilies(), activeEntityFamily);
        return VisionMemoryTrailDTO.builder()
                .activeEntityFamily(activeEntityFamily)
                .previousEntityFamily(previousEntityFamily)
                .topicSwitchHint(topicSwitchHint(activeEntityFamily, previousEntityFamily))
                .currentIntent(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getCurrentIntent())
                .currentRequestedSlot(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getRequestedSlot())
                .currentStatus(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getStatus())
                .sessionSummary(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getSessionSummary())
                .lastUserPrompt(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getLastUserPrompt())
                .lastNormalizedPrompt(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getLastNormalizedPrompt())
                .lastAssistantMessage(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getLastAssistantMessage())
                .sessionMemorySnapshot(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getSessionMemorySnapshot())
                .openQuestions(memoryContext.getSessionMemory() == null ? List.of() : memoryContext.getSessionMemory().getOpenQuestions())
                .recentActions(memoryContext.getSessionMemory() == null ? List.of() : memoryContext.getSessionMemory().getRecentActions())
                .recentEntityFamilies(memoryContext.getUserMemory() == null ? List.of() : memoryContext.getUserMemory().getRecentEntityFamilies())
                .recentIntentTypes(memoryContext.getUserMemory() == null ? List.of() : memoryContext.getUserMemory().getRecentIntentTypes())
                .build();
    }

    private VisionLearningMemoryDTO learningMemory(AppUser currentUser) {
        if (visionLearningService == null || currentUser == null) {
            return null;
        }
        return visionLearningService.buildLearningMemory(currentUser);
    }

    private VisionSemanticUserMemoryContext userMemoryFor(VisionConversation conversation) {
        if (visionSemanticOrchestrationContextService == null || conversation == null) {
            return null;
        }
        VisionSemanticMemoryContext memoryContext = visionSemanticOrchestrationContextService.buildMemoryContext(conversation.getOwner(), conversation);
        return memoryContext == null ? null : memoryContext.getUserMemory();
    }

    private String entityFamilyFor(VisionIntent intent) {
        if (intent == null) {
            return null;
        }
        return switch (intent) {
            case VIEW_PROFILE, VIEW_SETTINGS, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION, VIEW_USER_PROFILE -> "profile";
            case VIEW_NOTIFICATIONS -> "notifications";
            case VIEW_QUEST_NEWS -> "quest news";
            case VIEW_CIRCLES, VIEW_CIRCLE_DETAIL, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST,
                    DELETE_CIRCLE_REQUEST, UPDATE_CIRCLE, DELETE_CIRCLE -> "circles";
            case VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL, CREATE_APPLICATION, UPDATE_APPLICATION,
                    WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION -> "applications";
            case DISCOVER_QUESTS, CREATE_QUEST -> "quests";
            case SEARCH -> "search";
            case OPEN_CHAT, VIEW_CHAT_WORKSPACE -> "chat";
            default -> null;
        };
    }

    private String previousEntityFamily(List<String> recentEntityFamilies, String activeEntityFamily) {
        if (recentEntityFamilies == null || recentEntityFamilies.isEmpty()) {
            return null;
        }

        for (String family : recentEntityFamilies) {
            if (family == null || family.isBlank()) {
                continue;
            }
            if (activeEntityFamily == null || !family.equals(activeEntityFamily)) {
                return family;
            }
        }
        return null;
    }

    private String topicSwitchHint(String activeEntityFamily, String previousEntityFamily) {
        if (activeEntityFamily == null || activeEntityFamily.isBlank() || previousEntityFamily == null || previousEntityFamily.isBlank()) {
            return null;
        }
        if (activeEntityFamily.equals(previousEntityFamily)) {
            return null;
        }
        return "Switched from " + previousEntityFamily + " to " + activeEntityFamily + ".";
    }

    private VisionConversationSummaryDTO toConversationSummary(VisionConversation conversation, String entityFamily, String previousEntityFamily) {
        String title = conversation.getSlotData().get("quest_title");
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("circle_name");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("resolved_circle_name");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("circle_request_target_username");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("application_quest_title");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("managed_application_quest_title");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("profile_username");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("profile_location_label");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("resolved_profile_username");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("resolved_quest_title");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("search_query");
        }
        if ((title == null || title.isBlank()) && conversation.getIntent() == VisionIntent.VIEW_QUEST_NEWS) {
            title = "Quest news";
        }
        if ((title == null || title.isBlank()) && conversation.getIntent() == VisionIntent.VIEW_NOTIFICATIONS) {
            title = "Notifications";
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
        String topicSwitchHint = topicSwitchHint(entityFamily, previousEntityFamily);
        List<VisionSlotSummaryDTO> appliedSlotSummaries = visionTurnRepository.findTopByConversationOrderByTurnIndexDesc(conversation)
                .map(this::appliedSlotSummariesForTurn)
                .orElseGet(List::of);
        return VisionConversationSummaryDTO.builder()
                .conversationId(conversation.getId())
                .intent(conversation.getIntent().name())
                .entityFamily(entityFamily)
                .previousEntityFamily(previousEntityFamily)
                .topicSwitchHint(topicSwitchHint)
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
            case "target_quest_query" -> "Quest";
            case "target_circle_query" -> "Circle";
            case "target_application_query" -> "Application";
            case "target_user" -> "Person";
            case "application_message" -> "Application message";
            case "application_proposed_price" -> "Proposed price";
            case "application_existing_message" -> "Current message";
            case "application_existing_proposed_price" -> "Current price";
            case "profile_username" -> "Username";
            case "profile_description" -> "Profile description";
            case "profile_location_mode" -> "Location mode";
            case "profile_location_label" -> "Location";
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
            case "target_quest_query" -> firstNonBlank(
                    slotData.get("application_quest_title"),
                    slotData.get("resolved_quest_title"),
                    slotData.get("target_quest_query")
            );
            case "target_circle_query" -> slotData.get("resolved_circle_name");
            case "target_application_query" -> firstNonBlank(
                    slotData.get("application_quest_title"),
                    slotData.get("target_application_query")
            );
            case "target_user" -> firstNonBlank(
                    slotData.get("managed_application_applicant_username"),
                    slotData.get("circle_request_target_username"),
                    slotData.get("opened_chat_username"),
                    slotData.get("resolved_profile_username"),
                    slotData.get("target_user")
            );
            case "application_message" -> slotData.get("application_message");
            case "application_proposed_price" -> slotData.get("application_proposed_price");
            case "application_existing_message" -> slotData.get("application_existing_message");
            case "application_existing_proposed_price" -> slotData.get("application_existing_proposed_price");
            case "profile_description" -> slotData.get("profile_description");
            case "profile_location_mode" -> slotData.get("profile_location_mode");
            case "profile_location_label" -> slotData.get("profile_location_label");
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
        return switch (conversation.getIntent()) {
            case CREATE_QUEST -> handleCreateQuestReviewTurnInternal(conversation, "", "", understanding, source, VisionConversationAction.CONFIRM_REVIEW);
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
        if (conversation.getIntent() != VisionIntent.CREATE_QUEST) {
            throw ServiceErrors.conflict("Typed review editing is only supported for create quest conversations");
        }
        VisionReviewTarget reviewTarget = VisionReviewTarget.from(dto == null ? null : dto.getReviewTarget());
        return handleCreateQuestReviewEditAction(conversation, understanding, source, reviewTarget);
    }

    private VisionTurn handleResetConversationAction(VisionConversation conversation, String source) {
        VisionIntent intent = conversation.getIntent() == VisionIntent.DISCOVER_QUESTS
                ? VisionIntent.DISCOVER_QUESTS
                : conversation.getIntent() == VisionIntent.SEARCH
                ? VisionIntent.SEARCH
                : conversation.getIntent() == VisionIntent.OPEN_CHAT
                ? VisionIntent.OPEN_CHAT
                : conversation.getIntent() == VisionIntent.CREATE_CIRCLE
                ? VisionIntent.CREATE_CIRCLE
                : conversation.getIntent() == VisionIntent.CREATE_CIRCLE_REQUEST
                ? VisionIntent.CREATE_CIRCLE_REQUEST
                : conversation.getIntent() == VisionIntent.ACCEPT_CIRCLE_REQUEST
                ? VisionIntent.ACCEPT_CIRCLE_REQUEST
                : conversation.getIntent() == VisionIntent.DELETE_CIRCLE_REQUEST
                ? VisionIntent.DELETE_CIRCLE_REQUEST
                : conversation.getIntent() == VisionIntent.UPDATE_CIRCLE
                ? VisionIntent.UPDATE_CIRCLE
                : conversation.getIntent() == VisionIntent.DELETE_CIRCLE
                ? VisionIntent.DELETE_CIRCLE
                : conversation.getIntent() == VisionIntent.CREATE_APPLICATION
                ? VisionIntent.CREATE_APPLICATION
                : conversation.getIntent() == VisionIntent.UPDATE_APPLICATION
                ? VisionIntent.UPDATE_APPLICATION
                : conversation.getIntent() == VisionIntent.WITHDRAW_APPLICATION
                ? VisionIntent.WITHDRAW_APPLICATION
                : conversation.getIntent() == VisionIntent.APPROVE_APPLICATION
                ? VisionIntent.APPROVE_APPLICATION
                : conversation.getIntent() == VisionIntent.DECLINE_APPLICATION
                ? VisionIntent.DECLINE_APPLICATION
                : conversation.getIntent() == VisionIntent.UPDATE_PROFILE
                ? VisionIntent.UPDATE_PROFILE
                : conversation.getIntent() == VisionIntent.UPDATE_PROFILE_LOCATION
                ? VisionIntent.UPDATE_PROFILE_LOCATION
                : conversation.getIntent() == VisionIntent.VIEW_CHAT_WORKSPACE
                ? VisionIntent.VIEW_CHAT_WORKSPACE
                : conversation.getIntent() == VisionIntent.VIEW_PROFILE
                ? VisionIntent.VIEW_PROFILE
                : conversation.getIntent() == VisionIntent.VIEW_SETTINGS
                ? VisionIntent.VIEW_SETTINGS
                : conversation.getIntent() == VisionIntent.VIEW_USER_PROFILE
                ? VisionIntent.VIEW_USER_PROFILE
                : conversation.getIntent() == VisionIntent.VIEW_CIRCLES
                ? VisionIntent.VIEW_CIRCLES
                : conversation.getIntent() == VisionIntent.VIEW_CIRCLE_DETAIL
                ? VisionIntent.VIEW_CIRCLE_DETAIL
                : conversation.getIntent() == VisionIntent.VIEW_QUEST_DETAIL
                ? VisionIntent.VIEW_QUEST_DETAIL
                : conversation.getIntent() == VisionIntent.VIEW_NOTIFICATIONS
                ? VisionIntent.VIEW_NOTIFICATIONS
                : conversation.getIntent() == VisionIntent.VIEW_APPLICATIONS
                ? VisionIntent.VIEW_APPLICATIONS
                : conversation.getIntent() == VisionIntent.VIEW_APPLICATION_DETAIL
                ? VisionIntent.VIEW_APPLICATION_DETAIL
                : conversation.getIntent() == VisionIntent.VIEW_THINGS
                ? VisionIntent.VIEW_THINGS
                : VisionIntent.CREATE_QUEST;
        conversation.setIntent(intent);
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        conversation.setRequestedSlot(intent == VisionIntent.DISCOVER_QUESTS
                ? null
                : intent == VisionIntent.SEARCH ? null
                : intent == VisionIntent.OPEN_CHAT ? "target_user"
                : intent == VisionIntent.CREATE_CIRCLE ? "circle_name"
                : intent == VisionIntent.CREATE_CIRCLE_REQUEST ? "target_user"
                : intent == VisionIntent.ACCEPT_CIRCLE_REQUEST ? "target_user"
                : intent == VisionIntent.DELETE_CIRCLE_REQUEST ? "target_user"
                : intent == VisionIntent.UPDATE_CIRCLE ? "target_circle_query"
                : intent == VisionIntent.DELETE_CIRCLE ? "target_circle_query"
                : intent == VisionIntent.CREATE_APPLICATION ? "target_quest_query"
                : intent == VisionIntent.UPDATE_APPLICATION ? "target_quest_query"
                : intent == VisionIntent.WITHDRAW_APPLICATION ? "target_quest_query"
                : intent == VisionIntent.APPROVE_APPLICATION ? "target_quest_query"
                : intent == VisionIntent.DECLINE_APPLICATION ? "target_quest_query"
                : intent == VisionIntent.UPDATE_PROFILE ? "profile_username"
                : intent == VisionIntent.UPDATE_PROFILE_LOCATION ? "profile_location_mode"
                : intent == VisionIntent.VIEW_CHAT_WORKSPACE ? null
                : intent == VisionIntent.VIEW_PROFILE ? null
                : intent == VisionIntent.VIEW_SETTINGS ? null
                : intent == VisionIntent.VIEW_USER_PROFILE ? "target_user"
                : intent == VisionIntent.VIEW_CIRCLES ? null
                : intent == VisionIntent.VIEW_CIRCLE_DETAIL ? "target_circle_query"
                : intent == VisionIntent.VIEW_QUEST_DETAIL ? "target_quest_query"
                : intent == VisionIntent.VIEW_NOTIFICATIONS ? null
                : intent == VisionIntent.VIEW_APPLICATIONS ? null
                : intent == VisionIntent.VIEW_APPLICATION_DETAIL ? "target_application_query"
                : intent == VisionIntent.VIEW_THINGS ? null
                : "quest_title");
        conversation.setSlotData(new LinkedHashMap<>());
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
                ? resetReadOnlySnapshotMessage(VisionIntent.VIEW_CHAT_WORKSPACE)
                : intent == VisionIntent.VIEW_PROFILE
                ? resetReadOnlySnapshotMessage(VisionIntent.VIEW_PROFILE)
                : intent == VisionIntent.VIEW_SETTINGS
                ? resetReadOnlySnapshotMessage(VisionIntent.VIEW_SETTINGS)
                : intent == VisionIntent.VIEW_USER_PROFILE
                ? "The current user profile view was reset. What profile should I open?"
                : intent == VisionIntent.VIEW_CIRCLES
                ? resetReadOnlySnapshotMessage(VisionIntent.VIEW_CIRCLES)
                : intent == VisionIntent.VIEW_CIRCLE_DETAIL
                ? "The current circle detail view was reset. What circle should I open?"
                : intent == VisionIntent.VIEW_QUEST_DETAIL
                ? "The current quest detail view was reset. What quest should I open?"
                : intent == VisionIntent.VIEW_NOTIFICATIONS
                ? resetReadOnlySnapshotMessage(VisionIntent.VIEW_NOTIFICATIONS)
                : intent == VisionIntent.VIEW_APPLICATIONS
                ? resetReadOnlySnapshotMessage(VisionIntent.VIEW_APPLICATIONS)
                : intent == VisionIntent.VIEW_APPLICATION_DETAIL
                ? "The current application detail view was reset. What application should I open?"
                : intent == VisionIntent.VIEW_THINGS
                ? resetReadOnlySnapshotMessage(VisionIntent.VIEW_THINGS)
                : "The current vision task was reset. What should the new quest be called?";
        updateConversationMetadata(conversation, "", "", message, true);
        visionConversationRepository.save(conversation);
        boolean showResultsIntent = intent == VisionIntent.DISCOVER_QUESTS
                || intent == VisionIntent.VIEW_CHAT_WORKSPACE
                || intent == VisionIntent.VIEW_PROFILE
                || intent == VisionIntent.VIEW_SETTINGS
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
        return createTurn(
                conversation,
                source,
                "",
                "",
                intent,
                showResultsIntent ? VisionAgentState.RECOMMENDING : VisionAgentState.ASKING,
                showResultsIntent ? VisionNextAction.SHOW_RESULTS : VisionNextAction.ASK_FOR_SLOT,
                showResultsIntent ? null : intent == VisionIntent.OPEN_CHAT ? "target_user"
                        : intent == VisionIntent.CREATE_CIRCLE ? "circle_name"
                        : intent == VisionIntent.CREATE_CIRCLE_REQUEST ? "target_user"
                        : intent == VisionIntent.ACCEPT_CIRCLE_REQUEST ? "target_user"
                        : intent == VisionIntent.DELETE_CIRCLE_REQUEST ? "target_user"
                        : intent == VisionIntent.UPDATE_CIRCLE ? "target_circle_query"
                        : intent == VisionIntent.DELETE_CIRCLE ? "target_circle_query"
                        : intent == VisionIntent.CREATE_APPLICATION ? "target_quest_query"
                        : intent == VisionIntent.UPDATE_APPLICATION ? "target_quest_query"
                        : intent == VisionIntent.WITHDRAW_APPLICATION ? "target_quest_query"
                        : intent == VisionIntent.APPROVE_APPLICATION ? "target_quest_query"
                        : intent == VisionIntent.DECLINE_APPLICATION ? "target_quest_query"
                        : intent == VisionIntent.UPDATE_PROFILE ? "profile_username"
                        : intent == VisionIntent.UPDATE_PROFILE_LOCATION ? "profile_location_mode"
                        : intent == VisionIntent.VIEW_CHAT_WORKSPACE ? null
                        : intent == VisionIntent.VIEW_USER_PROFILE ? "target_user"
                        : intent == VisionIntent.VIEW_CIRCLE_DETAIL ? "target_circle_query"
                        : intent == VisionIntent.VIEW_QUEST_DETAIL ? "target_quest_query"
                        : intent == VisionIntent.VIEW_APPLICATION_DETAIL ? "target_application_query"
                        : !needsClarificationIntent ? null
                        : "quest_title",
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
        boolean lowConfidence = isLowConfidenceCreateQuestUnderstanding(understanding);

        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;
        if (missingSlot == null && lowConfidence) {
            VisionSemanticUserMemoryContext userMemory = userMemoryFor(conversation);
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

        if (isConfirmationPrompt(normalizedPrompt)) {
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

        if (isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                String message = "The circle request review is ready, but execution is still disabled.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.CREATE_CIRCLE_REQUEST,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            var createdRequest = visionCapabilityPreviewService.createCircleRequest(
                    conversation.getOwner(),
                    Long.parseLong(conversation.getSlotData().get("circle_request_target_user_id"))
            );
            conversation.getSlotData().put("circle_request_id", createdRequest.getId().toString());
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

        if (isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                String message = "The circle request acceptance review is ready, but execution is still disabled.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.ACCEPT_CIRCLE_REQUEST,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            var acceptedRequest = visionCapabilityPreviewService.acceptCircleRequest(
                    conversation.getOwner(),
                    Long.parseLong(conversation.getSlotData().get("circle_request_id"))
            );
            conversation.getSlotData().put("circle_request_id", acceptedRequest.getId().toString());
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

        if (isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                String message = "The circle request review is ready, but execution is still disabled.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.DELETE_CIRCLE_REQUEST,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            visionCapabilityPreviewService.deleteCircleRequest(
                    conversation.getOwner(),
                    Long.parseLong(conversation.getSlotData().get("circle_request_id"))
            );
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

        String circleQuery = resolveTargetCircleQuery(conversation, normalizedPrompt, understanding);
        if (circleQuery != null && !circleQuery.isBlank()) {
            VisionResolvedCircleTarget target = visionCapabilityPreviewService.resolveOwnedCircle(conversation.getOwner(), circleQuery);
            if (!target.resolved()) {
                return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.UPDATE_CIRCLE, "target_circle_query", target.blockingMessage());
            }
            applyResolvedCircleTarget(conversation, circleQuery, target);
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
        String circleQuery = resolveTargetCircleQuery(conversation, normalizedPrompt, understanding);
        if (circleQuery != null && !circleQuery.isBlank()) {
            VisionResolvedCircleTarget target = visionCapabilityPreviewService.resolveOwnedCircle(conversation.getOwner(), circleQuery);
            if (!target.resolved()) {
                return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.UPDATE_CIRCLE, "target_circle_query", target.blockingMessage());
            }
            applyResolvedCircleTarget(conversation, circleQuery, target);
        }
        String newCircleName = resolveCircleRename(conversation, normalizedPrompt, understanding);
        if (newCircleName != null && !newCircleName.isBlank()) {
            conversation.getSlotData().put("circle_name", newCircleName);
        }

        if (isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                updateConversationMetadata(conversation, prompt, normalizedPrompt, "The circle rename review is ready, but execution is still disabled.", understanding.isTranslationReliable());
                conversation.setStatus(VisionConversationStatus.REVIEW_READY);
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_CIRCLE,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(),
                        "The circle rename review is ready, but execution is still disabled.");
            }
            var updatedCircle = visionCapabilityPreviewService.updateCircle(
                    conversation.getOwner(),
                    Long.parseLong(conversation.getSlotData().get("resolved_circle_id")),
                    conversation.getSlotData().get("circle_name")
            );
            conversation.getSlotData().put("resolved_circle_name", updatedCircle.getName());
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
        String circleQuery = resolveTargetCircleQuery(conversation, normalizedPrompt, understanding);
        if (circleQuery == null || circleQuery.isBlank()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.DELETE_CIRCLE, "target_circle_query", "What circle should I delete? Say the circle name or circle id.");
        }
        VisionResolvedCircleTarget target = visionCapabilityPreviewService.resolveOwnedCircle(conversation.getOwner(), circleQuery);
        if (!target.resolved()) {
            return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.DELETE_CIRCLE, "target_circle_query", target.blockingMessage());
        }
        applyResolvedCircleTarget(conversation, circleQuery, target);
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
        String circleQuery = resolveTargetCircleQuery(conversation, normalizedPrompt, understanding);
        if (circleQuery != null && !circleQuery.isBlank()) {
            VisionResolvedCircleTarget target = visionCapabilityPreviewService.resolveOwnedCircle(conversation.getOwner(), circleQuery);
            if (!target.resolved()) {
                return askForSlot(conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.DELETE_CIRCLE, "target_circle_query", target.blockingMessage());
            }
            applyResolvedCircleTarget(conversation, circleQuery, target);
        }

        if (isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                String message = "The circle deletion review is ready, but execution is still disabled.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.DELETE_CIRCLE,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            visionCapabilityPreviewService.deleteCircle(conversation.getOwner(), Long.parseLong(conversation.getSlotData().get("resolved_circle_id")));
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

        if (isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                status = VisionConversationStatus.REVIEW_READY;
                nextAction = VisionNextAction.SHOW_REVIEW;
                agentState = VisionAgentState.REVIEW_READY;
                message = "The application review is ready, but execution is still disabled.";
            } else {
                var createdApplication = visionCapabilityPreviewService.createApplication(
                        conversation.getOwner(),
                        Long.parseLong(conversation.getSlotData().get("application_quest_id")),
                        conversation.getSlotData().get("application_message"),
                        conversation.getSlotData().get("application_proposed_price")
                );
                conversation.getSlotData().put("created_application_id", createdApplication.getId().toString());
                conversation.getSlotData().put("conversation_outcome", "created_application");
                status = VisionConversationStatus.COMPLETED;
                nextAction = VisionNextAction.COMPLETE;
                agentState = VisionAgentState.COMPLETE;
                message = "Application sent successfully.";
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
            applyResolvedApplicationTarget(conversation, questQuery, target);
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
            applyResolvedApplicationTarget(conversation, questQuery, target);
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

        if (isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                status = VisionConversationStatus.REVIEW_READY;
                nextAction = VisionNextAction.SHOW_REVIEW;
                agentState = VisionAgentState.REVIEW_READY;
                message = "The application update review is ready, but execution is still disabled.";
            } else {
                var updatedApplication = visionCapabilityPreviewService.updateApplication(
                        conversation.getOwner(),
                        Long.parseLong(conversation.getSlotData().get("application_quest_id")),
                        effectiveApplicationMessage(conversation),
                        effectiveApplicationPrice(conversation)
                );
                conversation.getSlotData().put("application_existing_message", updatedApplication.getMessage());
                conversation.getSlotData().put("application_existing_proposed_price",
                        updatedApplication.getProposedPrice() == null ? "" : updatedApplication.getProposedPrice().stripTrailingZeros().toPlainString());
                conversation.getSlotData().put("application_message", updatedApplication.getMessage());
                conversation.getSlotData().put("application_proposed_price",
                        updatedApplication.getProposedPrice() == null ? "" : updatedApplication.getProposedPrice().stripTrailingZeros().toPlainString());
                conversation.getSlotData().put("updated_application_id", updatedApplication.getId().toString());
                conversation.getSlotData().put("conversation_outcome", "updated_application");
                status = VisionConversationStatus.COMPLETED;
                nextAction = VisionNextAction.COMPLETE;
                agentState = VisionAgentState.COMPLETE;
                message = "Application updated successfully.";
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

        applyResolvedApplicationTarget(conversation, questQuery, target);
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
            applyResolvedApplicationTarget(conversation, questQuery, target);
        }

        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;

        if (isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                status = VisionConversationStatus.REVIEW_READY;
                nextAction = VisionNextAction.SHOW_REVIEW;
                agentState = VisionAgentState.REVIEW_READY;
                message = "The application withdrawal review is ready, but execution is still disabled.";
            } else {
                var withdrawnApplication = visionCapabilityPreviewService.withdrawApplication(
                        conversation.getOwner(),
                        Long.parseLong(conversation.getSlotData().get("application_quest_id"))
                );
                conversation.getSlotData().put("withdrawn_application_id", withdrawnApplication.getId().toString());
                conversation.getSlotData().put("conversation_outcome", "withdrawn_application");
                status = VisionConversationStatus.COMPLETED;
                nextAction = VisionNextAction.COMPLETE;
                agentState = VisionAgentState.COMPLETE;
                message = "Application withdrawn successfully.";
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

        if (isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                String message = "The application decision review is ready, but execution is still disabled.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, intent,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            if (approve) {
                visionCapabilityPreviewService.approveManagedApplication(
                        conversation.getOwner(),
                        Long.parseLong(conversation.getSlotData().get("managed_application_quest_id")),
                        Long.parseLong(conversation.getSlotData().get("managed_application_id"))
                );
                conversation.getSlotData().put("conversation_outcome", "approved_application");
            } else {
                visionCapabilityPreviewService.declineManagedApplication(
                        conversation.getOwner(),
                        Long.parseLong(conversation.getSlotData().get("managed_application_quest_id")),
                        Long.parseLong(conversation.getSlotData().get("managed_application_id"))
                );
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

        if (isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                String message = "The profile location review is ready, but execution is still disabled.";
                updateConversationMetadata(conversation, prompt, normalizedPrompt, message, understanding.isTranslationReliable());
                visionConversationRepository.save(conversation);
                return createTurn(conversation, source, prompt, normalizedPrompt, VisionIntent.UPDATE_PROFILE_LOCATION,
                        VisionAgentState.REVIEW_READY, VisionNextAction.SHOW_REVIEW, null,
                        understanding.isTranslationApplied(), understanding.isTranslationReliable(), message);
            }
            var updatedProfile = visionCapabilityPreviewService.updateProfileLocation(
                    conversation.getOwner(),
                    conversation.getSlotData().get("profile_location_mode"),
                    conversation.getSlotData().get("profile_location_label")
            );
            if (updatedProfile.getLocationSettings() != null) {
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

        if (isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                status = VisionConversationStatus.REVIEW_READY;
                nextAction = VisionNextAction.SHOW_REVIEW;
                agentState = VisionAgentState.REVIEW_READY;
                message = "The profile review is ready, but execution is still disabled.";
            } else {
                var updatedProfile = visionCapabilityPreviewService.updateProfile(
                        conversation.getOwner(),
                        conversation.getSlotData().get("profile_username"),
                        conversation.getSlotData().get("profile_description")
                );
                conversation.getSlotData().put("profile_username", updatedProfile.getUsername());
                conversation.getSlotData().put("profile_description",
                        updatedProfile.getProfileDescription() == null ? "" : updatedProfile.getProfileDescription());
                conversation.getSlotData().put("conversation_outcome", "updated_profile");
                status = VisionConversationStatus.COMPLETED;
                nextAction = VisionNextAction.COMPLETE;
                agentState = VisionAgentState.COMPLETE;
                message = "Profile updated successfully.";
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

    private VisionTurn handleViewProfileTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_PROFILE,
                readOnlySnapshotMessage(VisionIntent.VIEW_PROFILE)
        );
    }

    private VisionTurn handleViewChatWorkspaceTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_CHAT_WORKSPACE,
                readOnlySnapshotMessage(VisionIntent.VIEW_CHAT_WORKSPACE)
        );
    }

    private VisionTurn handleViewSettingsTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_SETTINGS,
                readOnlySnapshotMessage(VisionIntent.VIEW_SETTINGS)
        );
    }

    private VisionTurn handleViewUserProfileTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String profileQuery = resolveUserProfileQuery(conversation, normalizedPrompt, understanding);
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
            applyResolvedProfileTarget(conversation, profileQuery, target);
        }

        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_USER_PROFILE,
                readOnlySnapshotMessage(VisionIntent.VIEW_USER_PROFILE)
        );
    }

    private VisionTurn handleViewCirclesTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_CIRCLES,
                readOnlySnapshotMessage(VisionIntent.VIEW_CIRCLES)
        );
    }

    private VisionTurn handleViewCircleDetailTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String circleQuery = resolveTargetCircleQuery(conversation, normalizedPrompt, understanding);
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
            applyResolvedCircleTarget(conversation, circleQuery, target);
        }

        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_CIRCLE_DETAIL,
                readOnlySnapshotMessage(VisionIntent.VIEW_CIRCLE_DETAIL)
        );
    }

    private VisionTurn handleViewQuestDetailTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String questQuery = resolveReadQuestQuery(conversation, normalizedPrompt, understanding);
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
            applyResolvedQuestViewTarget(conversation, questQuery, target);
        }

        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_QUEST_DETAIL,
                readOnlySnapshotMessage(VisionIntent.VIEW_QUEST_DETAIL)
        );
    }

    private VisionTurn handleViewQuestNewsTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_QUEST_NEWS,
                readOnlySnapshotMessage(VisionIntent.VIEW_QUEST_NEWS)
        );
    }

    private VisionTurn handleViewNotificationsTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_NOTIFICATIONS,
                readOnlySnapshotMessage(VisionIntent.VIEW_NOTIFICATIONS)
        );
    }

    private VisionTurn handleViewApplicationsTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_APPLICATIONS,
                readOnlySnapshotMessage(VisionIntent.VIEW_APPLICATIONS)
        );
    }

    private VisionTurn handleViewThingsTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_THINGS,
                readOnlySnapshotMessage(VisionIntent.VIEW_THINGS)
        );
    }

    private VisionTurn handleViewApplicationDetailTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        String applicationQuery = resolveTargetApplicationQuery(conversation, normalizedPrompt, understanding);
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
            applyResolvedApplicationTarget(conversation, applicationQuery, target);
            conversation.getSlotData().put("target_application_query", applicationQuery);
        }

        return handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                VisionIntent.VIEW_APPLICATION_DETAIL,
                readOnlySnapshotMessage(VisionIntent.VIEW_APPLICATION_DETAIL)
        );
    }

    private VisionTurn handleReadOnlySnapshotTurn(
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

    private String readOnlySnapshotMessage(VisionIntent intent) {
        return switch (intent) {
            case VIEW_PROFILE -> "Showing your profile snapshot. You can say change username to ..., set bio to ..., or set profile location to ...";
            case VIEW_SETTINGS -> "Showing your settings snapshot. You can say show profile, set profile location to ..., or turn profile location off.";
            case VIEW_USER_PROFILE -> "Showing the selected user profile. You can say open chat with this user or show another profile.";
            case VIEW_CIRCLES -> "Showing your circles snapshot. You can say create a circle, invite someone, accept a circle request, rename a circle, or delete a circle.";
            case VIEW_CIRCLE_DETAIL -> "Showing the selected circle. You can say rename this circle, delete this circle, or show my circles.";
            case VIEW_APPLICATIONS -> "Showing your applications snapshot. You can say apply to a quest, update an application, withdraw an application, approve an application, or decline an application.";
            case VIEW_APPLICATION_DETAIL -> "Showing the selected application. You can say update this application, withdraw this application, or show my applications.";
            case VIEW_NOTIFICATIONS -> "Showing your notifications inbox. You can say open the related quest, open the related application, or show quest news.";
            case VIEW_QUEST_NEWS -> "Showing your quest news feed. You can say show another quest update or open the related quest.";
            case VIEW_CHAT_WORKSPACE -> "Showing your chat workspace snapshot. You can say open chat with ... to move into a direct conversation.";
            case VIEW_QUEST_DETAIL -> "Showing the selected quest. You can say apply to this quest or show another quest.";
            case VIEW_THINGS -> "Showing your things snapshot. You can say show available listings, open a listing, or share a thing.";
            default -> "Showing the current vision snapshot.";
        };
    }

    private String resetReadOnlySnapshotMessage(VisionIntent intent) {
        return switch (intent) {
            case VIEW_PROFILE, VIEW_SETTINGS, VIEW_CIRCLES, VIEW_APPLICATIONS, VIEW_CHAT_WORKSPACE, VIEW_NOTIFICATIONS, VIEW_QUEST_NEWS, VIEW_THINGS ->
                    "The current view was reset. " + readOnlySnapshotMessage(intent);
            default -> "The current view was reset.";
        };
    }

    private VisionCapabilityPreviewDTO capabilityPreview(VisionConversation conversation, AppUser currentUser) {
        if (conversation == null || currentUser == null) {
            return null;
        }
        return switch (conversation.getIntent()) {
            case CREATE_CIRCLE -> visionCapabilityPreviewService.previewCircleDraft(conversation.getSlotData().get("circle_name"));
            case CREATE_CIRCLE_REQUEST -> visionCapabilityPreviewService.previewCreateCircleRequestDraft(
                    conversation.getSlotData().get("circle_request_target_username")
            );
            case ACCEPT_CIRCLE_REQUEST -> visionCapabilityPreviewService.previewAcceptCircleRequestDraft(
                    conversation.getSlotData().get("circle_request_target_username")
            );
            case DELETE_CIRCLE_REQUEST -> visionCapabilityPreviewService.previewDeleteCircleRequestDraft(
                    conversation.getSlotData().get("circle_request_target_username"),
                    "incoming".equals(conversation.getSlotData().get("circle_request_direction"))
            );
            case UPDATE_CIRCLE -> visionCapabilityPreviewService.previewUpdateCircleDraft(
                    conversation.getSlotData().get("resolved_circle_name"),
                    conversation.getSlotData().get("circle_name")
            );
            case DELETE_CIRCLE -> visionCapabilityPreviewService.previewDeleteCircleDraft(
                    conversation.getSlotData().get("resolved_circle_name"),
                    conversation.getSlotData().get("resolved_circle_member_count")
            );
            case CREATE_APPLICATION -> visionCapabilityPreviewService.previewApplicationDraft(
                    conversation.getSlotData().get("application_quest_title"),
                    conversation.getSlotData().get("application_quest_creator"),
                    conversation.getSlotData().get("application_quest_reward_label"),
                    "true".equals(conversation.getSlotData().get("application_price_required")),
                    conversation.getSlotData().get("application_message"),
                    conversation.getSlotData().get("application_proposed_price")
            );
            case UPDATE_APPLICATION -> visionCapabilityPreviewService.previewUpdateApplicationDraft(
                    conversation.getSlotData().get("application_quest_title"),
                    conversation.getSlotData().get("application_quest_creator"),
                    conversation.getSlotData().get("application_quest_reward_label"),
                    "true".equals(conversation.getSlotData().get("application_price_required")),
                    conversation.getSlotData().get("application_existing_message"),
                    conversation.getSlotData().get("application_existing_proposed_price"),
                    conversation.getSlotData().get("application_message"),
                    conversation.getSlotData().get("application_proposed_price")
            );
            case WITHDRAW_APPLICATION -> visionCapabilityPreviewService.previewWithdrawApplicationDraft(
                    conversation.getSlotData().get("application_quest_title"),
                    conversation.getSlotData().get("application_quest_creator"),
                    conversation.getSlotData().get("application_quest_reward_label"),
                    conversation.getSlotData().get("application_existing_message"),
                    conversation.getSlotData().get("application_existing_proposed_price")
            );
            case APPROVE_APPLICATION -> visionCapabilityPreviewService.previewManagedApplicationDecisionDraft(
                    "approve_application",
                    "Application approval review",
                    "Review the pending application you are about to approve.",
                    conversation.getSlotData().get("managed_application_quest_title"),
                    conversation.getSlotData().get("managed_application_applicant_username"),
                    conversation.getSlotData().get("managed_application_message"),
                    conversation.getSlotData().get("managed_application_proposed_price")
            );
            case DECLINE_APPLICATION -> visionCapabilityPreviewService.previewManagedApplicationDecisionDraft(
                    "decline_application",
                    "Application decline review",
                    "Review the pending application you are about to decline.",
                    conversation.getSlotData().get("managed_application_quest_title"),
                    conversation.getSlotData().get("managed_application_applicant_username"),
                    conversation.getSlotData().get("managed_application_message"),
                    conversation.getSlotData().get("managed_application_proposed_price")
            );
            case UPDATE_PROFILE -> visionCapabilityPreviewService.previewProfileDraft(
                    currentUser,
                    conversation.getSlotData().get("profile_username"),
                    conversation.getSlotData().get("profile_description")
            );
            case UPDATE_PROFILE_LOCATION -> visionCapabilityPreviewService.previewProfileLocationDraft(
                    currentUser,
                    conversation.getSlotData().get("profile_location_mode"),
                    conversation.getSlotData().get("profile_location_label")
            );
            case VIEW_CHAT_WORKSPACE -> visionCapabilityPreviewService.previewChatWorkspace(currentUser);
            case VIEW_PROFILE -> visionCapabilityPreviewService.previewProfile(currentUser);
            case VIEW_SETTINGS -> visionCapabilityPreviewService.previewSettings(currentUser);
            case VIEW_USER_PROFILE -> hasText(conversation.getSlotData().get("resolved_profile_user_id"))
                    ? visionCapabilityPreviewService.previewUserProfile(currentUser, Long.parseLong(conversation.getSlotData().get("resolved_profile_user_id")))
                    : null;
            case VIEW_CIRCLES -> visionCapabilityPreviewService.previewCircles(currentUser);
            case VIEW_CIRCLE_DETAIL -> hasText(conversation.getSlotData().get("resolved_circle_id"))
                    ? visionCapabilityPreviewService.previewCircleDetail(currentUser, Long.parseLong(conversation.getSlotData().get("resolved_circle_id")))
                    : null;
            case VIEW_QUEST_DETAIL -> hasText(conversation.getSlotData().get("resolved_quest_id"))
                    ? visionCapabilityPreviewService.previewQuestDetail(currentUser, Long.parseLong(conversation.getSlotData().get("resolved_quest_id")))
                    : null;
            case VIEW_NOTIFICATIONS -> visionCapabilityPreviewService.previewNotifications(currentUser);
            case VIEW_QUEST_NEWS -> visionCapabilityPreviewService.previewQuestNews(currentUser);
            case VIEW_APPLICATIONS -> visionCapabilityPreviewService.previewApplications(currentUser);
            case VIEW_APPLICATION_DETAIL -> hasText(conversation.getSlotData().get("application_id"))
                    ? visionCapabilityPreviewService.previewApplicationDetail(currentUser, Long.parseLong(conversation.getSlotData().get("application_id")))
                    : null;
            case VIEW_THINGS -> visionCapabilityPreviewService.previewThings(currentUser);
            default -> null;
        };
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
        String semanticValue = understanding == null ? null : understanding.toExtractedSlotMap().get("circle_name");
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }

        if (conversation != null && "circle_name".equals(conversation.getRequestedSlot())) {
            return normalizedPrompt == null ? null : normalizedPrompt.trim();
        }

        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }

        String stripped = normalizedPrompt
                .replaceFirst("(?i)^create circle\\s+", "")
                .replaceFirst("(?i)^create a circle\\s+", "")
                .replaceFirst("(?i)^new circle\\s+", "")
                .replaceFirst("(?i)^make a circle\\s+", "")
                .replaceFirst("(?i)^start a circle\\s+", "")
                .replaceFirst("(?i)^called\\s+", "")
                .trim();
        if (stripped.equalsIgnoreCase(normalizedPrompt.trim())) {
            return null;
        }
        return stripped.isBlank() ? null : stripped;
    }

    private String resolveTargetCircleQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.toExtractedSlotMap().get("target_circle_query");
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "target_circle_query".equals(conversation.getRequestedSlot())) {
            return trimmed;
        }
        String stripped = trimmed
                .replaceFirst("(?i)^update circle\\s+", "")
                .replaceFirst("(?i)^rename circle\\s+", "")
                .replaceFirst("(?i)^delete circle\\s+", "")
                .replaceFirst("(?i)^remove circle\\s+", "")
                .trim();
        if (stripped.equalsIgnoreCase(trimmed) || stripped.isBlank()) {
            return null;
        }
        if (stripped.contains(" to ")) {
            return stripped.substring(0, stripped.indexOf(" to ")).trim();
        }
        return stripped;
    }

    private String resolveCircleRename(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.toExtractedSlotMap().get("circle_name");
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "circle_name".equals(conversation.getRequestedSlot())) {
            return trimmed;
        }
        String stripped = trimmed
                .replaceFirst("(?i)^rename to\\s+", "")
                .replaceFirst("(?i)^new name\\s+", "")
                .replaceFirst("(?i)^change name to\\s+", "")
                .trim();
        if (stripped.equalsIgnoreCase(trimmed) && trimmed.contains(" to ")) {
            return trimmed.substring(trimmed.lastIndexOf(" to ") + 4).trim();
        }
        if (stripped.equalsIgnoreCase(trimmed) || stripped.isBlank()) {
            return null;
        }
        return stripped;
    }

    private String resolveApplicationQuestQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.toExtractedSlotMap().get("target_quest_query");
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "target_quest_query".equals(conversation.getRequestedSlot())) {
            return trimmed;
        }
        String stripped = trimmed
                .replaceFirst("(?i)^apply to quest\\s+", "")
                .replaceFirst("(?i)^apply for quest\\s+", "")
                .replaceFirst("(?i)^apply to job\\s+", "")
                .replaceFirst("(?i)^apply for job\\s+", "")
                .replaceFirst("(?i)^apply to\\s+", "")
                .replaceFirst("(?i)^apply for\\s+", "")
                .replaceFirst("(?i)^create application for\\s+", "")
                .replaceFirst("(?i)^send application for\\s+", "")
                .trim();
        if (stripped.equalsIgnoreCase(trimmed) || stripped.isBlank()) {
            return null;
        }
        return stripped;
    }

    private String resolveReadQuestQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.toExtractedSlotMap().get("target_quest_query");
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "target_quest_query".equals(conversation.getRequestedSlot())) {
            return trimmed;
        }
        String stripped = trimmed
                .replaceFirst("(?i)^show quest\\s+", "")
                .replaceFirst("(?i)^open quest\\s+", "")
                .replaceFirst("(?i)^show quest details for\\s+", "")
                .replaceFirst("(?i)^show quest detail for\\s+", "")
                .trim();
        if (stripped.equalsIgnoreCase(trimmed) || stripped.isBlank()) {
            return null;
        }
        return stripped;
    }

    private String resolveTargetApplicationQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.toExtractedSlotMap().get("target_application_query");
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "target_application_query".equals(conversation.getRequestedSlot())) {
            return trimmed;
        }
        String stripped = trimmed
                .replaceFirst("(?i)^show application\\s+", "")
                .replaceFirst("(?i)^open application\\s+", "")
                .replaceFirst("(?i)^show my application\\s+", "")
                .replaceFirst("(?i)^open my application\\s+", "")
                .replaceFirst("(?i)^application\\s+", "")
                .trim();
        if (stripped.equalsIgnoreCase(trimmed) || stripped.isBlank()) {
            return null;
        }
        return stripped;
    }

    private String resolveApplicationMessage(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.toExtractedSlotMap().get("application_message");
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "application_message".equals(conversation.getRequestedSlot())) {
            return trimmed;
        }
        String stripped = trimmed
                .replaceFirst("(?i)^message\\s+", "")
                .replaceFirst("(?i)^my message is\\s+", "")
                .replaceFirst("(?i)^application message\\s+", "")
                .trim();
        if (stripped.equalsIgnoreCase(trimmed) || stripped.isBlank()) {
            return null;
        }
        return stripped;
    }

    private String resolveApplicationProposedPrice(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.toExtractedSlotMap().get("application_proposed_price");
        if (semanticValue != null && !semanticValue.isBlank()) {
            return normalizeApplicationPrice(semanticValue);
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        if (conversation != null && "application_proposed_price".equals(conversation.getRequestedSlot())) {
            return normalizeApplicationPrice(normalizedPrompt);
        }
        return null;
    }

    private void applyResolvedApplicationTarget(
            VisionConversation conversation,
            String questQuery,
            VisionResolvedApplicationTarget target
    ) {
        conversation.getSlotData().put("target_quest_query", questQuery);
        conversation.getSlotData().put("application_quest_id", target.questId().toString());
        conversation.getSlotData().put("application_quest_title", target.questTitle());
        conversation.getSlotData().put("application_quest_creator", target.creatorUsername());
        conversation.getSlotData().put("application_price_required", Boolean.toString(target.priceRequired()));
        conversation.getSlotData().put("application_quest_reward_label", target.rewardLabel());
        conversation.getSlotData().put("application_existing_message", target.currentMessage() == null ? "" : target.currentMessage());
        conversation.getSlotData().put("application_existing_proposed_price", target.currentPrice() == null ? "" : target.currentPrice());
        if (target.applicationId() != null) {
            conversation.getSlotData().put("application_id", target.applicationId().toString());
        }
    }

    private void applyResolvedQuestViewTarget(
            VisionConversation conversation,
            String questQuery,
            VisionResolvedQuestTarget target
    ) {
        conversation.getSlotData().put("target_quest_query", questQuery);
        conversation.getSlotData().put("resolved_quest_id", target.questId().toString());
        conversation.getSlotData().put("resolved_quest_title", target.questTitle());
        conversation.getSlotData().put("resolved_quest_creator", target.creatorUsername());
    }

    private String nextMissingUpdateApplicationSlot(VisionConversation conversation) {
        if (conversation == null) {
            return "target_quest_query";
        }
        if (!hasText(conversation.getSlotData().get("application_quest_id"))) {
            return "target_quest_query";
        }
        if (!hasText(conversation.getSlotData().get("application_message"))
                && !hasText(conversation.getSlotData().get("application_proposed_price"))) {
            return "application_message";
        }
        return null;
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

    private String normalizeApplicationPrice(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim()
                .replaceAll("(?i)\\s*(euros?|eur|chf|francs?)\\s*", "")
                .replace(',', '.')
                .trim();
        if (!normalized.matches("\\d+(\\.\\d{1,2})?")) {
            return null;
        }
        return normalized;
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
            return trimmed;
        }
        String stripped = trimmed
                .replaceFirst("(?i)^applicant\\s+", "")
                .replaceFirst("(?i)^approve\\s+", "")
                .replaceFirst("(?i)^decline\\s+", "")
                .replaceFirst("(?i)^reject\\s+", "")
                .replaceFirst("(?i)^accept\\s+", "")
                .trim();
        if (stripped.equalsIgnoreCase(trimmed) || stripped.isBlank()) {
            return null;
        }
        if (stripped.contains(" for ")) {
            return stripped.substring(0, stripped.indexOf(" for ")).trim();
        }
        return stripped;
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
            return trimmed;
        }
        String stripped = trimmed
                .replaceFirst("(?i)^show user\\s+", "")
                .replaceFirst("(?i)^open user\\s+", "")
                .replaceFirst("(?i)^show profile for\\s+", "")
                .replaceFirst("(?i)^open profile for\\s+", "")
                .replaceFirst("(?i)^show profile of\\s+", "")
                .replaceFirst("(?i)^open profile of\\s+", "")
                .trim();
        if (stripped.equalsIgnoreCase(trimmed) || stripped.isBlank()) {
            return null;
        }
        return stripped;
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
            return trimmed;
        }
        String stripped = trimmed
                .replaceFirst("(?i)^send a circle request to\\s+", "")
                .replaceFirst("(?i)^send circle request to\\s+", "")
                .replaceFirst("(?i)^invite to my circle\\s+", "")
                .replaceFirst("(?i)^invite to my circles\\s+", "")
                .replaceFirst("(?i)^add to my circle\\s+", "")
                .replaceFirst("(?i)^add to my circles\\s+", "")
                .replaceFirst("(?i)^connect with\\s+", "")
                .replaceFirst("(?i)^accept circle request from\\s+", "")
                .replaceFirst("(?i)^accept connection request from\\s+", "")
                .replaceFirst("(?i)^accept invite from\\s+", "")
                .replaceFirst("(?i)^decline circle request from\\s+", "")
                .replaceFirst("(?i)^reject circle request from\\s+", "")
                .replaceFirst("(?i)^decline invite from\\s+", "")
                .replaceFirst("(?i)^reject invite from\\s+", "")
                .replaceFirst("(?i)^cancel circle request to\\s+", "")
                .replaceFirst("(?i)^cancel invite to\\s+", "")
                .replaceFirst("(?i)^delete circle request with\\s+", "")
                .replaceFirst("(?i)^remove circle request with\\s+", "")
                .trim();
        if (stripped.equalsIgnoreCase(trimmed) || stripped.isBlank()) {
            return null;
        }
        return stripped;
    }

    private void applyResolvedCircleTarget(
            VisionConversation conversation,
            String circleQuery,
            VisionResolvedCircleTarget target
    ) {
        conversation.getSlotData().put("target_circle_query", circleQuery);
        conversation.getSlotData().put("resolved_circle_id", target.circleId().toString());
        conversation.getSlotData().put("resolved_circle_name", target.circleName());
        conversation.getSlotData().put("resolved_circle_member_count", target.memberCountLabel());
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

    private void applyResolvedProfileTarget(
            VisionConversation conversation,
            String targetUserQuery,
            VisionResolvedUserTarget target
    ) {
        conversation.getSlotData().put("target_user", targetUserQuery);
        conversation.getSlotData().put("resolved_profile_user_id", target.userId().toString());
        conversation.getSlotData().put("resolved_profile_username", target.username());
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

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String resolveProfileLocationMode(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.toExtractedSlotMap().get("profile_location_mode");
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim().toUpperCase(Locale.ROOT);
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String lower = normalizedPrompt.trim().toLowerCase(Locale.ROOT);
        if (conversation != null && "profile_location_mode".equals(conversation.getRequestedSlot())) {
            if (lower.contains("off") || lower.contains("hide")) {
                return "OFF";
            }
            if (lower.contains("approx")) {
                return "APPROXIMATE";
            }
            if (lower.contains("exact")) {
                return "EXACT";
            }
        }
        if (lower.contains("turn off") || lower.contains("hide my location") || lower.contains("location off")) {
            return "OFF";
        }
        if (lower.contains("approximate")) {
            return "APPROXIMATE";
        }
        if (lower.contains("exact")) {
            return "EXACT";
        }
        return null;
    }

    private String resolveProfileLocationLabel(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.toExtractedSlotMap().get("profile_location_label");
        if (semanticValue != null) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        if (conversation != null && "profile_location_label".equals(conversation.getRequestedSlot())) {
            return normalizedPrompt.trim();
        }
        String stripped = normalizedPrompt.trim()
                .replaceFirst("(?i)^set my location to\\s+", "")
                .replaceFirst("(?i)^update my location to\\s+", "")
                .replaceFirst("(?i)^change my location to\\s+", "")
                .replaceFirst("(?i)^location\\s+", "")
                .trim();
        if (stripped.equalsIgnoreCase(normalizedPrompt.trim()) || stripped.isBlank()) {
            return null;
        }
        return stripped;
    }

    private String nextMissingProfileLocationSlot(VisionConversation conversation) {
        if (conversation == null) {
            return "profile_location_mode";
        }
        String mode = conversation.getSlotData().get("profile_location_mode");
        if (!hasText(mode)) {
            return "profile_location_mode";
        }
        if (!"OFF".equalsIgnoreCase(mode)) {
            String draftLabel = conversation.getSlotData().get("profile_location_label");
            if (!hasText(draftLabel)) {
                return "profile_location_label";
            }
        }
        return null;
    }

    private String resolveProfileUsername(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.toExtractedSlotMap().get("profile_username");
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }

        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }

        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "profile_username".equals(conversation.getRequestedSlot())
                && !looksLikeProfileDescriptionInstruction(trimmed)
                && !looksLikeGenericProfileUpdateInstruction(trimmed)) {
            return trimmed;
        }

        String stripped = trimmed
                .replaceFirst("(?i)^update my username to\\s+", "")
                .replaceFirst("(?i)^change my username to\\s+", "")
                .replaceFirst("(?i)^set my username to\\s+", "")
                .replaceFirst("(?i)^username\\s+", "")
                .replaceFirst("(?i)^my username is\\s+", "")
                .trim();
        if (stripped.equalsIgnoreCase(trimmed)) {
            return null;
        }
        return stripped.isBlank() ? null : stripped;
    }

    private String resolveProfileDescription(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.toExtractedSlotMap().get("profile_description");
        if (semanticValue != null) {
            return semanticValue.trim();
        }

        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }

        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "profile_description".equals(conversation.getRequestedSlot())) {
            return trimmed;
        }

        String stripped = trimmed
                .replaceFirst("(?i)^set description to\\s+", "")
                .replaceFirst("(?i)^set my description to\\s+", "")
                .replaceFirst("(?i)^change my description to\\s+", "")
                .replaceFirst("(?i)^update my description to\\s+", "")
                .replaceFirst("(?i)^set bio to\\s+", "")
                .replaceFirst("(?i)^change my bio to\\s+", "")
                .replaceFirst("(?i)^update my bio to\\s+", "")
                .replaceFirst("(?i)^set my profile description to\\s+", "")
                .replaceFirst("(?i)^change my profile description to\\s+", "")
                .replaceFirst("(?i)^update my profile description to\\s+", "")
                .trim();
        if (stripped.equalsIgnoreCase(trimmed)) {
            return null;
        }
        return stripped;
    }

    private boolean hasProfileUpdateDraft(VisionConversation conversation) {
        if (conversation == null || conversation.getSlotData() == null) {
            return false;
        }
        return hasText(conversation.getSlotData().get("profile_username"))
                || conversation.getSlotData().containsKey("profile_description");
    }

    private boolean looksLikeProfileDescriptionInstruction(String prompt) {
        if (prompt == null) {
            return false;
        }
        String lower = prompt.trim().toLowerCase(Locale.ROOT);
        return lower.startsWith("set description")
                || lower.startsWith("set my description")
                || lower.startsWith("change my description")
                || lower.startsWith("update my description")
                || lower.startsWith("set bio")
                || lower.startsWith("change my bio")
                || lower.startsWith("update my bio")
                || lower.startsWith("set my profile description")
                || lower.startsWith("change my profile description")
                || lower.startsWith("update my profile description");
    }

    private boolean looksLikeGenericProfileUpdateInstruction(String prompt) {
        if (prompt == null) {
            return false;
        }
        String lower = prompt.trim().toLowerCase(Locale.ROOT);
        return lower.equals("update my profile")
                || lower.equals("edit my profile")
                || lower.equals("change my profile");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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
        conversation.setSessionMemorySnapshot(serializeSessionMemorySnapshot(conversation));
        conversation.setUpdatedAt(now);
        conversation.setLastTurnAt(now);
    }

    private String serializeSessionMemorySnapshot(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return null;
        }

        VisionSemanticMemoryContext memoryContext = visionSemanticOrchestrationContextService.buildMemoryContext(conversation.getOwner(), conversation);
        VisionSemanticSessionMemoryContext sessionMemory = memoryContext == null ? null : memoryContext.getSessionMemory();
        if (sessionMemory == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(sessionMemory);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize vision session memory snapshot", exception);
        }
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
