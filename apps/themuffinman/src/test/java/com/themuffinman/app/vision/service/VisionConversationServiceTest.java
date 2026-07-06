package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.location.service.LocationLookupService;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.things.dto.ThingListingListResponseDTO;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.testing.TestFixtures;
import com.themuffinman.app.vision.dto.VisionLearningMemoryDTO;
import com.themuffinman.app.vision.dto.VisionLearningPreferenceDTO;
import com.themuffinman.app.vision.dto.VisionConversationListResponseDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnRequestDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.dto.VisionMemoryTrailDTO;
import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.vision.dto.VisionQuestDiscoveryDTO;
import com.themuffinman.app.vision.dto.VisionSearchDiscoveryDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionNextAction;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.vision.repository.VisionTurnRepository;
import com.themuffinman.app.vision.testing.VisionConversationTestBuilder;
import com.themuffinman.app.vision.testing.VisionLocationCandidatePresets;
import com.themuffinman.app.vision.testing.VisionSchedulePhrasePresets;
import com.themuffinman.app.vision.testing.VisionSlotStatePresets;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.dto.QuestListResponseDTO;
import com.themuffinman.app.vision.service.QuestReadService;
import com.themuffinman.app.vision.service.QuestApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VisionConversationServiceTest {

    @Mock
    private VisionConversationRepository visionConversationRepository;

    @Mock
    private VisionTurnRepository visionTurnRepository;

    @Mock
    private VisionPromptUnderstandingService visionPromptUnderstandingService;

    @Mock
    private VisionCreateQuestExecutionAdapter visionCreateQuestExecutionAdapter;

    @Mock
    private VisionChatExecutionService visionChatExecutionService;

    @Mock
    private VisionCapabilityPreviewService visionCapabilityPreviewService;

    private VisionConversationReadModelAssembler visionConversationReadModelAssembler;

    @Mock
    private QuestReadService questReadService;

    @Mock
    private LocationLookupService locationLookupService;

    @Mock
    private CircleReadService circleReadService;

    @Mock
    private ThingSharingService thingSharingService;

    @Mock
    private QuestApplicationService questApplicationService;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private VisionLearningService visionLearningService;

    private VisionConversationService visionConversationService;
    private AtomicLong conversationIds;
    private AtomicLong turnIds;
    private AppUser currentUser;
    private VisionProperties visionProperties;
    private final java.util.Map<Long, VisionTurn> lastSavedTurns = new HashMap<>();
    private final java.util.Map<Long, VisionConversation> savedConversations = new HashMap<>();

    @BeforeEach
    void setUp() {
        visionProperties = new VisionProperties();
        SemanticAliasRegistry semanticAliasRegistry = new SemanticAliasRegistry();
        VisionSemanticRouteCatalogService visionSemanticRouteCatalogService = new VisionSemanticRouteCatalogService();
        VisionIntentRouter visionIntentRouter = new VisionIntentRouter(visionProperties, visionSemanticRouteCatalogService);
        VisionScheduleParserService visionScheduleParserService = new VisionScheduleParserService();
        VisionLocationParserService visionLocationParserService = new VisionLocationParserService();
        VisionLocationResolutionService visionLocationResolutionService = new VisionLocationResolutionService(
                visionLocationParserService,
                locationLookupService
        );
        VisionSemanticMapper visionSemanticMapper = new VisionSemanticMapper();
        VisionSlotService visionSlotService = new VisionSlotService(visionScheduleParserService, visionLocationResolutionService, visionSemanticMapper);
        VisionClarificationService visionClarificationService = new VisionClarificationService();
        VisionCanvasAssembler visionCanvasAssembler = new VisionCanvasAssembler(visionProperties);
        VisionExecutionPlanner visionExecutionPlanner = new VisionExecutionPlanner(visionClarificationService, visionSemanticRouteCatalogService, visionProperties);
        VisionQuestDiscoveryService visionQuestDiscoveryService = new VisionQuestDiscoveryService(questReadService, semanticAliasRegistry);
        VisionSearchDiscoveryService visionSearchDiscoveryService = new VisionSearchDiscoveryService(
                questReadService,
                circleReadService,
                thingSharingService,
                questApplicationService,
                appUserRepository,
                semanticAliasRegistry
        );
        VisionSemanticOrchestrationContextService visionSemanticOrchestrationContextService =
                new VisionSemanticOrchestrationContextService(new com.themuffinman.app.config.VoiceProperties(), visionConversationRepository, visionTurnRepository);
        visionConversationReadModelAssembler = new VisionConversationReadModelAssembler(
                visionConversationRepository,
                visionTurnRepository,
                visionSemanticOrchestrationContextService,
                visionClarificationService
        );
        VisionSurfacePolicy visionSurfacePolicy = new VisionSurfacePolicy(visionProperties);
        lenient().when(visionCreateQuestExecutionAdapter.capabilityId()).thenReturn("create_quest");
        VisionCreateCircleExecutionAdapter visionCreateCircleExecutionAdapter =
                new VisionCreateCircleExecutionAdapter(visionCapabilityPreviewService);
        VisionExecutionService visionExecutionService = new VisionExecutionService(
                visionSurfacePolicy,
                List.of(visionCreateQuestExecutionAdapter, visionCreateCircleExecutionAdapter)
        );
        currentUser = TestFixtures.user(7L, "vision-user");

        conversationIds = new AtomicLong(100);
        turnIds = new AtomicLong(200);

        lenient().when(visionConversationRepository.save(any(VisionConversation.class))).thenAnswer(invocation -> {
            VisionConversation conversation = invocation.getArgument(0);
            if (conversation.getId() == null) {
                conversation.setId(conversationIds.incrementAndGet());
            }
            if (conversation.getSlotData() == null) {
                conversation.setSlotData(new LinkedHashMap<>());
            }
            savedConversations.put(conversation.getId(), conversation);
            return conversation;
        });

        lenient().when(visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(currentUser))
                .thenAnswer(invocation -> savedConversations.values().stream()
                        .sorted((left, right) -> {
                            if (left.getUpdatedAt() == null && right.getUpdatedAt() == null) {
                                return 0;
                            }
                            if (left.getUpdatedAt() == null) {
                                return 1;
                            }
                            if (right.getUpdatedAt() == null) {
                                return -1;
                            }
                            return right.getUpdatedAt().compareTo(left.getUpdatedAt());
                        })
                        .toList());

        lenient().when(visionConversationRepository.findByIdAndOwner(any(), any(AppUser.class)))
                .thenAnswer(invocation -> {
                    Long conversationId = invocation.getArgument(0);
                    AppUser owner = invocation.getArgument(1);
                    VisionConversation conversation = savedConversations.get(conversationId);
                    if (conversation == null || owner == null || conversation.getOwner() == null
                            || !owner.getId().equals(conversation.getOwner().getId())) {
                        return Optional.empty();
                    }
                    return Optional.of(conversation);
                });

        lenient().when(visionTurnRepository.save(any(VisionTurn.class))).thenAnswer(invocation -> {
            VisionTurn turn = invocation.getArgument(0);
            if (turn.getId() == null) {
                turn.setId(turnIds.incrementAndGet());
            }
            if (turn.getConversation() != null && turn.getConversation().getId() != null) {
                lastSavedTurns.put(turn.getConversation().getId(), turn);
            }
            return turn;
        });

        lenient().when(visionTurnRepository.findTopByConversationOrderByTurnIndexDesc(any(VisionConversation.class)))
                .thenAnswer(invocation -> Optional.ofNullable(lastSavedTurns.get(((VisionConversation) invocation.getArgument(0)).getId())));

        visionConversationService = new VisionConversationService(
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
                visionLearningService
        );

        lenient().when(questReadService.getQuestListPreset(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(QuestListResponseDTO.builder().items(List.of()).page(0).size(5).totalItems(0).totalPages(0).build());

        lenient().when(circleReadService.getCircles(any())).thenReturn(List.of());
        lenient().when(thingSharingService.getAvailableListings(any())).thenReturn(ThingListingListResponseDTO.builder().items(List.of()).build());
        lenient().when(questApplicationService.getApplicationsForApplicant(any())).thenReturn(List.of());
        lenient().when(appUserRepository.searchByUsernameOrEmail(any())).thenReturn(List.of());

        lenient().when(visionPromptUnderstandingService.understandPrompt(any(), any(), any(), any()))
                .thenAnswer(invocation -> understanding(invocation.getArgument(0), invocation.getArgument(1)));
    }

    @Test
    void rejectsBlankPrompt() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> visionConversationService.processTurn(VisionConversationTurnRequestDTO.builder()
                        .prompt(" ")
                        .build(), currentUser));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void profilePromptRoutesIntoReadOnlyProfilePreview() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.previewProfile(currentUser)).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("view_profile")
                        .title("Profile")
                        .summary("Profile.")
                        .items(List.of(
                                com.themuffinman.app.vision.dto.VisionSlotSummaryDTO.builder()
                                        .slotId("profile_username")
                                        .label("Username")
                                        .value("vision-user")
                                        .build()
                        ))
                        .tone("info")
                        .build()
        );
        when(visionLearningService.buildLearningMemory(currentUser)).thenReturn(
                VisionLearningMemoryDTO.builder()
                        .summaryText("Top preferences: preferred_input_type=voice")
                        .recentFeedbackTypes(List.of("CORRECTION"))
                        .preferenceSignals(List.of(
                                VisionLearningPreferenceDTO.builder()
                                        .preferenceKey("preferred_input_type")
                                        .preferenceValue("voice")
                                        .sourceType("runtime")
                                        .observationCount(4)
                                        .confidenceScore(0.88d)
                                        .build()
                        ))
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("show my profile")
                        .build(),
                currentUser
        );

        assertEquals("VIEW_PROFILE", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertEquals("results", response.getCanvasMode());
        assertEquals("Profile.", response.getMessage());
        assertTrue(response.getBlocks().stream().anyMatch(block ->
                "result_summary".equals(block.getType())
                        && "Profile".equals(block.getTitle())));
        assertNotNull(response.getLearningMemory());
        assertEquals("Top preferences: preferred_input_type=voice", response.getLearningMemory().getSummaryText());
        assertEquals(1, response.getLearningMemory().getPreferenceSignals().size());
        assertNotNull(response.getMemoryTrail());
        assertNotNull(response.getMemoryTrail().getSessionSummary());
        assertNotNull(response.getMemoryTrail().getOpenQuestions());
        assertTrue(savedConversations.values().stream()
                .anyMatch(conversation -> conversation.getSessionMemorySnapshot() != null
                        && conversation.getSessionMemorySnapshot().contains("\"currentIntent\":\"VIEW_PROFILE\"")));
    }

    @Test
    void circlesPromptRoutesIntoReadOnlyCirclesPreviewWithWorkspaceGuidance() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.previewCircles(currentUser)).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("view_circles")
                        .title("Circles")
                        .summary("2 circles.")
                        .items(List.of(
                                com.themuffinman.app.vision.dto.VisionSlotSummaryDTO.builder()
                                        .slotId("circle_1")
                                        .label("Friends")
                                        .value("1 members · Nikol")
                                        .build()
                        ))
                        .tone("info")
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("show circles")
                        .build(),
                currentUser
        );

        assertEquals("VIEW_CIRCLES", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertEquals("Showing your circles.", response.getMessage());
    }

    @Test
    void memoryTrailShowsEntityFamilySwitchWhenPromptMovesFromQuestToCircle() {
        VisionConversation priorQuestConversation = VisionConversationTestBuilder.createQuest(21L, currentUser)
                .slot("quest_title", "Move a sofa")
                .build();
        priorQuestConversation.setUpdatedAt(java.time.Instant.parse("2026-07-03T10:00:00Z"));
        savedConversations.put(priorQuestConversation.getId(), priorQuestConversation);

        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("create circle Neighbours")
                        .build(),
                currentUser
        );

        assertEquals("CREATE_CIRCLE", response.getIntent());
        assertEquals("quests", response.getMemoryTrail().getPreviousEntityFamily());
        assertEquals("Switched from quests to circles.", response.getMemoryTrail().getTopicSwitchHint());
    }

    @Test
    void applicationsPromptRoutesIntoReadOnlyApplicationsPreviewWithWorkspaceGuidance() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.previewApplications(currentUser)).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("view_applications")
                        .title("Applications")
                        .summary("1 application.")
                        .items(List.of(
                                com.themuffinman.app.vision.dto.VisionSlotSummaryDTO.builder()
                                        .slotId("applications_count")
                                        .label("Applications")
                                        .value("3")
                                        .build()
                        ))
                        .tone("info")
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("show applications")
                        .build(),
                currentUser
        );

        assertEquals("VIEW_APPLICATIONS", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertEquals("Applications.", response.getMessage());
    }

    @Test
    void newsPromptRoutesIntoReadOnlyQuestNewsPreview() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.previewQuestNews(currentUser)).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("view_quest_news")
                        .title("Quest news")
                        .summary("Quest news.")
                        .items(List.of(
                                com.themuffinman.app.vision.dto.VisionSlotSummaryDTO.builder()
                                        .slotId("news_count")
                                        .label("Updates")
                                        .value("2")
                                        .build()
                        ))
                        .tone("info")
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("show my news")
                        .build(),
                currentUser
        );

        assertEquals("VIEW_QUEST_NEWS", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertTrue(response.getBlocks().stream().anyMatch(block ->
                "result_summary".equals(block.getType())
                        && "Quest news".equals(block.getTitle())));
    }

    @Test
    void notificationsPromptRoutesIntoReadOnlyNotificationsPreview() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.previewNotifications(currentUser)).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("view_notifications")
                        .title("Notifications")
                        .summary("Notifications.")
                        .items(List.of(
                                com.themuffinman.app.vision.dto.VisionSlotSummaryDTO.builder()
                                        .slotId("notifications_unread")
                                        .label("Unread")
                                        .value("2")
                                        .build()
                        ))
                        .tone("info")
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("show notifications")
                        .build(),
                currentUser
        );

        assertEquals("VIEW_NOTIFICATIONS", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertTrue(response.getBlocks().stream().anyMatch(block ->
                "result_summary".equals(block.getType())
                        && "Notifications".equals(block.getTitle())));
    }

    @Test
    void chatWorkspacePromptRoutesIntoReadOnlyChatPreview() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.previewChatWorkspace(currentUser)).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("view_chat_workspace")
                        .title("Chat")
                        .summary("Chat.")
                        .items(List.of())
                        .tone("info")
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("show chat")
                        .build(),
                currentUser
        );

        assertEquals("VIEW_CHAT_WORKSPACE", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertTrue(response.getBlocks().stream().anyMatch(block ->
                "result_summary".equals(block.getType())
                        && "Chat".equals(block.getTitle())));
    }

    @Test
    void settingsPromptRoutesIntoReadOnlySettingsPreview() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.previewSettings(currentUser)).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("view_settings")
                        .title("Settings")
                        .summary("Settings.")
                        .items(List.of())
                        .tone("info")
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("show settings")
                        .build(),
                currentUser
        );

        assertEquals("VIEW_SETTINGS", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertTrue(response.getBlocks().stream().anyMatch(block ->
                "result_summary".equals(block.getType())
                        && "Settings".equals(block.getTitle())));
    }

    @Test
    void userProfilePromptRoutesIntoReadOnlyUserProfilePreview() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.resolveUserProfileTarget(currentUser, "Josip"))
                .thenReturn(VisionResolvedUserTarget.resolved(12L, "Josip"));
        when(visionCapabilityPreviewService.previewUserProfile(currentUser, 12L)).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("view_user_profile")
                        .title("User profile")
                        .summary("User profile.")
                        .items(List.of())
                        .tone("info")
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("show user Josip")
                        .build(),
                currentUser
        );

        assertEquals("VIEW_USER_PROFILE", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertTrue(response.getBlocks().stream().anyMatch(block ->
                "result_summary".equals(block.getType())
                        && "User profile".equals(block.getTitle())));
    }

    @Test
    void circleDetailPromptRoutesIntoReadOnlyCirclePreview() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.resolveOwnedCircle(currentUser, "Family"))
                .thenReturn(VisionResolvedCircleTarget.resolved(14L, "Family", "3"));
        when(visionCapabilityPreviewService.previewCircleDetail(currentUser, 14L)).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("view_circle_detail")
                        .title("Circle")
                        .summary("Circle.")
                        .items(List.of())
                        .tone("info")
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("open circle Family")
                        .build(),
                currentUser
        );

        assertEquals("VIEW_CIRCLE_DETAIL", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertTrue(response.getBlocks().stream().anyMatch(block ->
                "result_summary".equals(block.getType())
                        && "Circle".equals(block.getTitle())));
    }

    @Test
    void questDetailPromptRoutesIntoReadOnlyQuestPreview() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.resolveVisibleQuest(currentUser, "quest #42"))
                .thenReturn(VisionResolvedQuestTarget.resolved(42L, "Move sofa", "quest-owner", false, "Free"));
        when(visionCapabilityPreviewService.previewQuestDetail(currentUser, 42L)).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("view_quest_detail")
                        .title("Quest")
                        .summary("Quest.")
                        .items(List.of())
                        .tone("info")
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("show quest #42")
                        .build(),
                currentUser
        );

        assertEquals("VIEW_QUEST_DETAIL", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertTrue(response.getBlocks().stream().anyMatch(block ->
                "result_summary".equals(block.getType())
                        && "Quest".equals(block.getTitle())));
    }

    @Test
    void applicationDetailPromptRoutesIntoReadOnlyApplicationPreview() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.resolveMyApplicationDetail(currentUser, "application #42"))
                .thenReturn(VisionResolvedApplicationTarget.resolved(91L, "Move sofa", "quest-owner", false, "Free", "I can help", null, 42L));
        when(visionCapabilityPreviewService.previewApplicationDetail(currentUser, 42L)).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("view_application_detail")
                        .title("Application")
                        .summary("Application.")
                        .items(List.of())
                        .tone("info")
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("show application #42")
                        .build(),
                currentUser
        );

        assertEquals("VIEW_APPLICATION_DETAIL", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertTrue(response.getBlocks().stream().anyMatch(block ->
                "result_summary".equals(block.getType())
                        && "Application".equals(block.getTitle())));
    }

    @Test
    void createCirclePromptReachesReviewAndConfirmCreatesCircle() {
        visionProperties.setExecutionEnabled(true);
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L, 2L, 3L);
        when(visionCapabilityPreviewService.previewCircleDraft("Neighbours")).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("create_circle")
                        .title("Circle draft")
                        .summary("Review the new circle before confirmation.")
                        .items(List.of(
                                com.themuffinman.app.vision.dto.VisionSlotSummaryDTO.builder()
                                        .slotId("circle_name")
                                        .label("Circle name")
                                        .value("Neighbours")
                                        .build()
                        ))
                        .tone("info")
                        .build()
        );
        when(visionCapabilityPreviewService.createCircle("Neighbours", currentUser)).thenReturn(
                CircleGroupResponseDTO.builder().id(88L).name("Neighbours").memberCount(0).build()
        );

        VisionConversationTurnResponseDTO reviewResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("create circle Neighbours")
                        .build(),
                currentUser
        );

        assertEquals("CREATE_CIRCLE", reviewResponse.getIntent());
        assertEquals("SHOW_REVIEW", reviewResponse.getNextAction());
        assertEquals("review", reviewResponse.getCanvasMode());

        VisionConversationTurnResponseDTO completeResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(reviewResponse.getConversationId())
                        .prompt("confirm")
                        .build(),
                currentUser
        );

        assertEquals("CREATE_CIRCLE", completeResponse.getIntent());
        assertEquals("COMPLETE", completeResponse.getNextAction());
        assertEquals("complete", completeResponse.getCanvasMode());
        assertTrue(completeResponse.getBlocks().stream().anyMatch(block ->
                "success".equals(block.getType()) && "Circle created".equals(block.getTitle())));
    }

    @Test
    void createNewCirclePromptUsesStandardPrefixExtractionForDraftName() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.previewCircleDraft("Lover")).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("create_circle")
                        .title("Circle draft")
                        .summary("Review the new circle before confirmation.")
                        .items(List.of(
                                com.themuffinman.app.vision.dto.VisionSlotSummaryDTO.builder()
                                        .slotId("circle_name")
                                        .label("Circle name")
                                        .value("Lover")
                                        .build()
                        ))
                        .tone("info")
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("create new circle Lover")
                        .build(),
                currentUser
        );

        assertEquals("CREATE_CIRCLE", response.getIntent());
        assertEquals("SHOW_REVIEW", response.getNextAction());
        assertTrue(response.getBlocks().stream().anyMatch(block ->
                "result_summary".equals(block.getType())
                        && "Circle draft".equals(block.getTitle())));
    }

    @Test
    void createCircleRequestFlowReachesReviewAndConfirmCreatesRequest() {
        visionProperties.setExecutionEnabled(true);
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L, 2L);
        when(visionCapabilityPreviewService.resolveCircleRequestRecipient(currentUser, "Josip"))
                .thenReturn(VisionResolvedUserTarget.resolved(12L, "Josip"));
        when(visionCapabilityPreviewService.previewCreateCircleRequestDraft("Josip")).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("create_circle_request")
                        .title("Circle request draft")
                        .summary("Review the connection invite before confirmation.")
                        .items(List.of())
                        .tone("info")
                        .build()
        );
        when(visionCapabilityPreviewService.createCircleRequest(currentUser, 12L)).thenReturn(
                com.themuffinman.app.social.dto.CircleRequestResponseDTO.builder()
                        .id(51L)
                        .counterpartUserId(12L)
                        .counterpartUsername("Josip")
                        .build()
        );

        VisionConversationTurnResponseDTO reviewTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("send circle request to Josip")
                        .build(),
                currentUser
        );
        assertEquals("CREATE_CIRCLE_REQUEST", reviewTurn.getIntent());
        assertEquals("SHOW_REVIEW", reviewTurn.getNextAction());

        VisionConversationTurnResponseDTO completeTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(reviewTurn.getConversationId())
                        .action("CONFIRM_REVIEW")
                        .build(),
                currentUser
        );
        assertEquals("CREATE_CIRCLE_REQUEST", completeTurn.getIntent());
        assertEquals("COMPLETE", completeTurn.getNextAction());
        assertTrue(completeTurn.getBlocks().stream().anyMatch(block ->
                "success".equals(block.getType()) && "Circle request sent".equals(block.getTitle())));
    }

    @Test
    void acceptCircleRequestFlowReachesReviewAndConfirmAcceptsRequest() {
        visionProperties.setExecutionEnabled(true);
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L, 2L);
        when(visionCapabilityPreviewService.resolveIncomingCircleRequest(currentUser, "Josip"))
                .thenReturn(VisionResolvedCircleRequestTarget.resolved(51L, 12L, "Josip", true));
        when(visionCapabilityPreviewService.previewAcceptCircleRequestDraft("Josip")).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("accept_circle_request")
                        .title("Circle request acceptance review")
                        .summary("Review the incoming circle request you are about to accept.")
                        .items(List.of())
                        .tone("info")
                        .build()
        );
        when(visionCapabilityPreviewService.acceptCircleRequest(currentUser, 51L)).thenReturn(
                com.themuffinman.app.social.dto.CircleRequestResponseDTO.builder()
                        .id(51L)
                        .counterpartUserId(12L)
                        .counterpartUsername("Josip")
                        .build()
        );

        VisionConversationTurnResponseDTO reviewTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("accept circle request from Josip")
                        .build(),
                currentUser
        );
        assertEquals("ACCEPT_CIRCLE_REQUEST", reviewTurn.getIntent());
        assertEquals("SHOW_REVIEW", reviewTurn.getNextAction());

        VisionConversationTurnResponseDTO completeTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(reviewTurn.getConversationId())
                        .action("CONFIRM_REVIEW")
                        .build(),
                currentUser
        );
        assertEquals("ACCEPT_CIRCLE_REQUEST", completeTurn.getIntent());
        assertEquals("COMPLETE", completeTurn.getNextAction());
        assertTrue(completeTurn.getBlocks().stream().anyMatch(block ->
                "success".equals(block.getType()) && "Circle request accepted".equals(block.getTitle())));
    }

    @Test
    void deleteCircleRequestFlowReachesReviewAndConfirmRemovesRequest() {
        visionProperties.setExecutionEnabled(true);
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L, 2L);
        when(visionCapabilityPreviewService.resolveAccessiblePendingCircleRequest(currentUser, "Josip"))
                .thenReturn(VisionResolvedCircleRequestTarget.resolved(51L, 12L, "Josip", true));
        when(visionCapabilityPreviewService.previewDeleteCircleRequestDraft("Josip", true)).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("delete_circle_request")
                        .title("Circle request decline review")
                        .summary("Review the incoming circle request you are about to decline.")
                        .items(List.of())
                        .tone("warning")
                        .build()
        );

        VisionConversationTurnResponseDTO reviewTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("decline circle request from Josip")
                        .build(),
                currentUser
        );
        assertEquals("DELETE_CIRCLE_REQUEST", reviewTurn.getIntent());
        assertEquals("SHOW_REVIEW", reviewTurn.getNextAction());

        VisionConversationTurnResponseDTO completeTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(reviewTurn.getConversationId())
                        .action("CONFIRM_REVIEW")
                        .build(),
                currentUser
        );
        assertEquals("DELETE_CIRCLE_REQUEST", completeTurn.getIntent());
        assertEquals("COMPLETE", completeTurn.getNextAction());
        assertTrue(completeTurn.getBlocks().stream().anyMatch(block ->
                "success".equals(block.getType()) && "Circle request declined".equals(block.getTitle())));
    }

    @Test
    void updateCircleFlowReachesReviewAndConfirmRenamesCircle() {
        visionProperties.setExecutionEnabled(true);
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L, 2L, 3L);
        when(visionCapabilityPreviewService.resolveOwnedCircle(currentUser, "Neighbours"))
                .thenReturn(VisionResolvedCircleTarget.resolved(41L, "Neighbours", "5"));
        when(visionCapabilityPreviewService.previewUpdateCircleDraft("Neighbours", "Core Team")).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("update_circle")
                        .title("Circle update draft")
                        .summary("Review the circle rename before confirmation.")
                        .items(List.of())
                        .tone("info")
                        .build()
        );
        when(visionCapabilityPreviewService.updateCircle(currentUser, 41L, "Core Team")).thenReturn(
                CircleGroupResponseDTO.builder().id(41L).name("Core Team").memberCount(5).build()
        );

        VisionConversationTurnResponseDTO targetTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("rename circle Neighbours")
                        .build(),
                currentUser
        );
        assertEquals("UPDATE_CIRCLE", targetTurn.getIntent());
        assertEquals("ASK_FOR_SLOT", targetTurn.getNextAction());
        assertEquals("circle_name", targetTurn.getRequestedSlot());

        VisionConversationTurnResponseDTO reviewTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(targetTurn.getConversationId())
                        .prompt("Core Team")
                        .build(),
                currentUser
        );
        assertEquals("SHOW_REVIEW", reviewTurn.getNextAction());

        VisionConversationTurnResponseDTO completeTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(targetTurn.getConversationId())
                        .action("CONFIRM_REVIEW")
                        .build(),
                currentUser
        );
        assertEquals("UPDATE_CIRCLE", completeTurn.getIntent());
        assertEquals("COMPLETE", completeTurn.getNextAction());
        assertTrue(completeTurn.getBlocks().stream().anyMatch(block ->
                "success".equals(block.getType()) && "Circle updated".equals(block.getTitle())));
    }

    @Test
    void deleteCircleFlowReachesReviewAndConfirmDeletesCircle() {
        visionProperties.setExecutionEnabled(true);
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L, 2L);
        when(visionCapabilityPreviewService.resolveOwnedCircle(currentUser, "Neighbours"))
                .thenReturn(VisionResolvedCircleTarget.resolved(41L, "Neighbours", "5"));
        when(visionCapabilityPreviewService.previewDeleteCircleDraft("Neighbours", "5")).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("delete_circle")
                        .title("Circle deletion review")
                        .summary("Review the circle you are about to delete.")
                        .items(List.of())
                        .tone("warning")
                        .build()
        );

        VisionConversationTurnResponseDTO reviewTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("delete circle Neighbours")
                        .build(),
                currentUser
        );
        assertEquals("DELETE_CIRCLE", reviewTurn.getIntent());
        assertEquals("SHOW_REVIEW", reviewTurn.getNextAction());

        VisionConversationTurnResponseDTO completeTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(reviewTurn.getConversationId())
                        .action("CONFIRM_REVIEW")
                        .build(),
                currentUser
        );
        assertEquals("DELETE_CIRCLE", completeTurn.getIntent());
        assertEquals("COMPLETE", completeTurn.getNextAction());
        assertTrue(completeTurn.getBlocks().stream().anyMatch(block ->
                "success".equals(block.getType()) && "Circle deleted".equals(block.getTitle())));
    }

    @Test
    void createApplicationFlowReachesReviewAndConfirmCreatesApplication() {
        visionProperties.setExecutionEnabled(true);
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L, 2L, 3L, 4L);
        when(visionCapabilityPreviewService.resolveApplicationQuest(currentUser, "Move a sofa"))
                .thenReturn(VisionResolvedQuestTarget.resolved(55L, "Move a sofa", "anna", true, "20"));
        when(visionCapabilityPreviewService.previewApplicationDraft(
                "Move a sofa",
                "anna",
                "20",
                true,
                "I can do it tomorrow",
                "20"
        )).thenReturn(com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                .capabilityId("create_application")
                .title("Application draft")
                .summary("Review the quest target, message, and proposed price before confirmation.")
                .items(List.of(
                        com.themuffinman.app.vision.dto.VisionSlotSummaryDTO.builder()
                                .slotId("target_quest_query")
                                .label("Quest")
                                .value("Move a sofa")
                                .build()
                ))
                .tone("info")
                .build());
        when(visionCapabilityPreviewService.createApplication(currentUser, 55L, "I can do it tomorrow", "20"))
                .thenReturn(QuestApplicationResponseDTO.builder()
                        .id(91L)
                        .questId(55L)
                        .questTitle("Move a sofa")
                        .message("I can do it tomorrow")
                        .build());

        VisionConversationTurnResponseDTO questTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("apply to Move a sofa")
                        .build(),
                currentUser
        );
        assertEquals("CREATE_APPLICATION", questTurn.getIntent());
        assertEquals("ASK_FOR_SLOT", questTurn.getNextAction());

        VisionConversationTurnResponseDTO messageTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(questTurn.getConversationId())
                        .prompt("message I can do it tomorrow")
                        .build(),
                currentUser
        );
        assertEquals("ASK_FOR_SLOT", messageTurn.getNextAction());
        assertEquals("application_proposed_price", messageTurn.getRequestedSlot());

        VisionConversationTurnResponseDTO reviewTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(questTurn.getConversationId())
                        .prompt("20")
                        .build(),
                currentUser
        );
        assertEquals("SHOW_REVIEW", reviewTurn.getNextAction());
        assertEquals("review", reviewTurn.getCanvasMode());

        VisionConversationTurnResponseDTO completeTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(questTurn.getConversationId())
                        .action("CONFIRM_REVIEW")
                        .build(),
                currentUser
        );
        assertEquals("CREATE_APPLICATION", completeTurn.getIntent());
        assertEquals("COMPLETE", completeTurn.getNextAction());
        assertTrue(completeTurn.getBlocks().stream().anyMatch(block ->
                "success".equals(block.getType()) && "Application sent".equals(block.getTitle())));
    }

    @Test
    void updateApplicationFlowReachesReviewAndConfirmSavesChanges() {
        visionProperties.setExecutionEnabled(true);
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L, 2L, 3L);
        when(visionCapabilityPreviewService.resolveMyPendingApplication(currentUser, "Move a sofa", com.themuffinman.app.vision.dto.ApplicationAllowedActionDTO.EDIT))
                .thenReturn(VisionResolvedApplicationTarget.resolved(55L, "Move a sofa", "anna", true, "20", "Old message", "20", 91L));
        when(visionCapabilityPreviewService.previewUpdateApplicationDraft(
                "Move a sofa",
                "anna",
                "20",
                true,
                "Old message",
                "20",
                "I can come earlier",
                "25"
        )).thenReturn(com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                .capabilityId("update_application")
                .title("Application update draft")
                .summary("Review the application changes before confirmation. Unchanged values will be kept.")
                .items(List.of())
                .tone("info")
                .build());
        when(visionCapabilityPreviewService.updateApplication(currentUser, 55L, "I can come earlier", "25"))
                .thenReturn(QuestApplicationResponseDTO.builder()
                        .id(91L)
                        .questId(55L)
                        .questTitle("Move a sofa")
                        .message("I can come earlier")
                        .proposedPrice(new java.math.BigDecimal("25"))
                        .build());

        VisionConversationTurnResponseDTO targetTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("update my application for Move a sofa")
                        .build(),
                currentUser
        );
        assertEquals("UPDATE_APPLICATION", targetTurn.getIntent());
        assertEquals("ASK_FOR_SLOT", targetTurn.getNextAction());
        assertEquals("application_message", targetTurn.getRequestedSlot());

        VisionConversationTurnResponseDTO reviewTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(targetTurn.getConversationId())
                        .prompt("message I can come earlier and price 25")
                        .build(),
                currentUser
        );
        assertEquals("SHOW_REVIEW", reviewTurn.getNextAction());

        VisionConversationTurnResponseDTO completeTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(targetTurn.getConversationId())
                        .action("CONFIRM_REVIEW")
                        .build(),
                currentUser
        );
        assertEquals("UPDATE_APPLICATION", completeTurn.getIntent());
        assertEquals("COMPLETE", completeTurn.getNextAction());
        assertTrue(completeTurn.getBlocks().stream().anyMatch(block ->
                "success".equals(block.getType()) && "Application updated".equals(block.getTitle())));
    }

    @Test
    void withdrawApplicationFlowReachesReviewAndConfirmWithdraws() {
        visionProperties.setExecutionEnabled(true);
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L, 2L);
        when(visionCapabilityPreviewService.resolveMyPendingApplication(currentUser, "Move a sofa", com.themuffinman.app.vision.dto.ApplicationAllowedActionDTO.WITHDRAW))
                .thenReturn(VisionResolvedApplicationTarget.resolved(55L, "Move a sofa", "anna", true, "20", "Old message", "20", 91L));
        when(visionCapabilityPreviewService.previewWithdrawApplicationDraft(
                "Move a sofa",
                "anna",
                "20",
                "Old message",
                "20"
        )).thenReturn(com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                .capabilityId("withdraw_application")
                .title("Application withdrawal review")
                .summary("Review the pending application you are about to withdraw.")
                .items(List.of())
                .tone("warning")
                .build());
        when(visionCapabilityPreviewService.withdrawApplication(currentUser, 55L))
                .thenReturn(QuestApplicationResponseDTO.builder()
                        .id(91L)
                        .questId(55L)
                        .questTitle("Move a sofa")
                        .build());

        VisionConversationTurnResponseDTO reviewTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("withdraw my application for Move a sofa")
                        .build(),
                currentUser
        );
        assertEquals("WITHDRAW_APPLICATION", reviewTurn.getIntent());
        assertEquals("SHOW_REVIEW", reviewTurn.getNextAction());

        VisionConversationTurnResponseDTO completeTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(reviewTurn.getConversationId())
                        .action("CONFIRM_REVIEW")
                        .build(),
                currentUser
        );
        assertEquals("WITHDRAW_APPLICATION", completeTurn.getIntent());
        assertEquals("COMPLETE", completeTurn.getNextAction());
        assertTrue(completeTurn.getBlocks().stream().anyMatch(block ->
                "success".equals(block.getType()) && "Application withdrawn".equals(block.getTitle())));
    }

    @Test
    void approveApplicationFlowReachesReviewAndConfirmApproves() {
        visionProperties.setExecutionEnabled(true);
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L, 2L, 3L);
        when(visionCapabilityPreviewService.resolveManagedPendingApplication(
                any(AppUser.class),
                any(String.class),
                any(),
                org.mockito.ArgumentMatchers.eq(com.themuffinman.app.vision.dto.ApplicationAllowedActionDTO.APPROVE)
        )).thenAnswer(invocation -> {
            String applicant = invocation.getArgument(2);
            if (applicant == null || applicant.isBlank()) {
                return VisionResolvedManagedApplicationTarget.unresolved("Who is the applicant? Say the applicant username.");
            }
            return VisionResolvedManagedApplicationTarget.resolved(55L, "Move a sofa", "Josip", "I can help", "20", 91L);
        });
        when(visionCapabilityPreviewService.previewManagedApplicationDecisionDraft(
                "approve_application",
                "Application approval review",
                "Review the pending application you are about to approve.",
                "Move a sofa",
                "Josip",
                "I can help",
                "20"
        )).thenReturn(com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                .capabilityId("approve_application")
                .title("Application approval review")
                .summary("Review the pending application you are about to approve.")
                .items(List.of())
                .tone("info")
                .build());
        when(visionCapabilityPreviewService.approveManagedApplication(currentUser, 55L, 91L))
                .thenReturn(QuestApplicationResponseDTO.builder()
                        .id(91L)
                        .questId(55L)
                        .questTitle("Move a sofa")
                        .applicantUsername("Josip")
                        .build());

        VisionConversationTurnResponseDTO reviewTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("approve application for Move a sofa")
                        .build(),
                currentUser
        );
        assertEquals("APPROVE_APPLICATION", reviewTurn.getIntent());
        assertEquals("SHOW_REVIEW", reviewTurn.getNextAction());

        VisionConversationTurnResponseDTO completeTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(reviewTurn.getConversationId())
                        .action("CONFIRM_REVIEW")
                        .build(),
                currentUser
        );
        assertEquals("APPROVE_APPLICATION", completeTurn.getIntent());
        assertEquals("COMPLETE", completeTurn.getNextAction());
        assertTrue(completeTurn.getBlocks().stream().anyMatch(block ->
                "success".equals(block.getType()) && "Application approved".equals(block.getTitle())));
    }

    @Test
    void declineApplicationFlowReachesReviewAndConfirmDeclines() {
        visionProperties.setExecutionEnabled(true);
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L, 2L, 3L);
        when(visionCapabilityPreviewService.resolveManagedPendingApplication(
                any(AppUser.class),
                any(String.class),
                any(),
                org.mockito.ArgumentMatchers.eq(com.themuffinman.app.vision.dto.ApplicationAllowedActionDTO.DECLINE)
        )).thenAnswer(invocation -> {
            String applicant = invocation.getArgument(2);
            if (applicant == null || applicant.isBlank()) {
                return VisionResolvedManagedApplicationTarget.unresolved("Who is the applicant? Say the applicant username.");
            }
            return VisionResolvedManagedApplicationTarget.resolved(55L, "Move a sofa", "Josip", "I can help", "20", 91L);
        });
        when(visionCapabilityPreviewService.previewManagedApplicationDecisionDraft(
                "decline_application",
                "Application decline review",
                "Review the pending application you are about to decline.",
                "Move a sofa",
                "Josip",
                "I can help",
                "20"
        )).thenReturn(com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                .capabilityId("decline_application")
                .title("Application decline review")
                .summary("Review the pending application you are about to decline.")
                .items(List.of())
                .tone("info")
                .build());
        when(visionCapabilityPreviewService.declineManagedApplication(currentUser, 55L, 91L))
                .thenReturn(QuestApplicationResponseDTO.builder()
                        .id(91L)
                        .questId(55L)
                        .questTitle("Move a sofa")
                        .applicantUsername("Josip")
                        .build());

        VisionConversationTurnResponseDTO reviewTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("decline application for Move a sofa")
                        .build(),
                currentUser
        );
        assertEquals("DECLINE_APPLICATION", reviewTurn.getIntent());
        assertEquals("SHOW_REVIEW", reviewTurn.getNextAction());

        VisionConversationTurnResponseDTO completeTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(reviewTurn.getConversationId())
                        .action("CONFIRM_REVIEW")
                        .build(),
                currentUser
        );
        assertEquals("DECLINE_APPLICATION", completeTurn.getIntent());
        assertEquals("COMPLETE", completeTurn.getNextAction());
        assertTrue(completeTurn.getBlocks().stream().anyMatch(block ->
                "success".equals(block.getType()) && "Application declined".equals(block.getTitle())));
    }

    @Test
    void updateProfilePromptReachesReviewAndTypedConfirmSavesProfile() {
        visionProperties.setExecutionEnabled(true);
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L, 2L, 3L);
        when(visionCapabilityPreviewService.previewProfileDraft(currentUser, "jsak", "Reliable mover"))
                .thenReturn(com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("update_profile")
                        .title("Profile draft")
                        .summary("Review 2 profile changes before confirmation.")
                        .items(List.of(
                                com.themuffinman.app.vision.dto.VisionSlotSummaryDTO.builder()
                                        .slotId("profile_username")
                                        .label("Username")
                                        .value("jsak")
                                        .build(),
                                com.themuffinman.app.vision.dto.VisionSlotSummaryDTO.builder()
                                        .slotId("profile_description")
                                        .label("Profile description")
                                        .value("Reliable mover")
                                        .build()
                        ))
                        .tone("info")
                        .build());
        when(visionCapabilityPreviewService.updateProfile(currentUser, "jsak", "Reliable mover"))
                .thenReturn(AppUserResponseDTO.builder()
                        .id(currentUser.getId())
                        .username("jsak")
                        .email("vision-user@example.com")
                        .profileDescription("Reliable mover")
                        .build());

        VisionConversationTurnResponseDTO reviewResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("update my username to jsak")
                        .build(),
                currentUser
        );

        VisionConversationTurnResponseDTO revisedReviewResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(reviewResponse.getConversationId())
                        .prompt("set description to Reliable mover")
                        .build(),
                currentUser
        );

        assertEquals("UPDATE_PROFILE", revisedReviewResponse.getIntent());
        assertEquals("SHOW_REVIEW", revisedReviewResponse.getNextAction());
        assertEquals("review", revisedReviewResponse.getCanvasMode());

        VisionConversationTurnResponseDTO completeResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(reviewResponse.getConversationId())
                        .action("CONFIRM_REVIEW")
                        .build(),
                currentUser
        );

        assertEquals("UPDATE_PROFILE", completeResponse.getIntent());
        assertEquals("COMPLETE", completeResponse.getNextAction());
        assertTrue(completeResponse.getBlocks().stream().anyMatch(block ->
                "success".equals(block.getType()) && "Profile updated".equals(block.getTitle())));
    }

    @Test
    void updateProfileLocationPromptReachesReviewAndConfirmSavesLocation() {
        visionProperties.setExecutionEnabled(true);
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L, 2L);
        when(visionCapabilityPreviewService.previewProfileLocationDraft(currentUser, "EXACT", "Zurich, Switzerland"))
                .thenReturn(com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("update_profile_location")
                        .title("Profile location draft")
                        .summary("Review the profile location changes before confirmation.")
                        .items(List.of())
                        .tone("info")
                        .build());
        when(visionCapabilityPreviewService.updateProfileLocation(currentUser, "EXACT", "Zurich, Switzerland"))
                .thenReturn(AppUserResponseDTO.builder()
                        .id(currentUser.getId())
                        .username("vision-user")
                        .email("vision-user@example.com")
                        .locationSettings(com.themuffinman.app.location.dto.UserLocationSettingsDTO.builder()
                                .mode(com.themuffinman.app.location.model.UserLocationMode.EXACT)
                                .label("Zurich, Switzerland")
                                .build())
                        .build());

        VisionConversationTurnResponseDTO reviewTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("set my profile location to Zurich, Switzerland")
                        .build(),
                currentUser
        );

        assertEquals("UPDATE_PROFILE_LOCATION", reviewTurn.getIntent());
        assertEquals("SHOW_REVIEW", reviewTurn.getNextAction());

        VisionConversationTurnResponseDTO completeTurn = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(reviewTurn.getConversationId())
                        .action("CONFIRM_REVIEW")
                        .build(),
                currentUser
        );

        assertEquals("UPDATE_PROFILE_LOCATION", completeTurn.getIntent());
        assertEquals("COMPLETE", completeTurn.getNextAction());
        assertTrue(completeTurn.getBlocks().stream().anyMatch(block ->
                "success".equals(block.getType()) && "Profile location updated".equals(block.getTitle())));
    }

    @Test
    void semanticExtractionCanPopulateMultipleQuestFieldsInOneTurn() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("Create a structured quest")
                        .build(),
                currentUser
        );

        assertEquals("CREATE_QUEST", response.getIntent());
        assertEquals("SHOW_REVIEW", response.getNextAction());
        assertEquals("review", response.getCanvasMode());
        assertEquals("Move a sofa", response.getReview().getTitle());
        assertEquals("25", response.getReview().getRewardLabel());
        assertEquals("PUBLIC", response.getReview().getVisibility());
        assertNotNull(response.getReview().getSchedule());
        assertTrue(response.getReview().getSchedule().contains("2026"));
        assertEquals("Use profile location", response.getReview().getLocation());
        assertFalse(response.getAppliedSlotSummaries().isEmpty());
        assertTrue(response.getAppliedSlotSummaries().stream().anyMatch(slot -> "quest_title".equals(slot.getSlotId())));
        assertTrue(response.getBlocks().stream().anyMatch(block -> "review_summary".equals(block.getType())));
    }

    @Test
    void discoveryTurnReturnsQuestDiscoveryCanvas() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("show me open quests for moving help")
                        .build(),
                currentUser
        );

        assertEquals("DISCOVER_QUESTS", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertEquals("results", response.getCanvasMode());
        assertNotNull(response.getQuestDiscovery());
        assertEquals("discover_quests", response.getQuestDiscovery().getCapabilityId());
        assertTrue(response.getBlocks().stream().anyMatch(block -> "quest_discovery".equals(block.getType())));
    }

    @Test
    void searchTurnReturnsCrossEntitySearchCanvas() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("find people who can help move sofa")
                        .build(),
                currentUser
        );

        assertEquals("SEARCH", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertEquals("results", response.getCanvasMode());
        assertNotNull(response.getSearchDiscovery());
        assertEquals("search", response.getSearchDiscovery().getCapabilityId());
        assertTrue(response.getBlocks().stream().anyMatch(block -> "search_discovery".equals(block.getType())));
    }

    @Test
    void thingsTurnReturnsReadOnlyThingsCanvas() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L);
        when(thingSharingService.getAvailableListings(currentUser)).thenReturn(
                ThingListingListResponseDTO.builder()
                        .items(List.of(
                                ThingListingResponseDTO.builder()
                                        .id(22L)
                                        .title("Sofa trolley")
                                        .description("Moves a sofa")
                                        .ownerUsername("alex")
                                        .available(true)
                                        .build()
                        ))
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("show available listings")
                        .build(),
                currentUser
        );

        assertEquals("VIEW_THINGS", response.getIntent());
        assertEquals("Things.", response.getMessage());
    }

    @Test
    void switchesConversationIntentWhenPromptChangesTaskType() {
        VisionConversation conversation = createQuestConversation(88L, "quest_title");
        when(visionConversationRepository.findByIdAndOwner(88L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(88L)
                        .prompt("show me open quests for moving help")
                        .build(),
                currentUser
        );

        assertEquals("DISCOVER_QUESTS", response.getIntent());
        assertEquals("SHOW_RESULTS", response.getNextAction());
        assertNotEquals(88L, response.getConversationId());
    }

    @Test
    void keepsExistingConversationWhenDetectedIntentIsAWeakAmbiguousFollowUp() {
        VisionConversation conversation = createQuestConversation(94L, "quest_title");
        when(visionConversationRepository.findByIdAndOwner(94L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionPromptUnderstandingService.understandPrompt(
                org.mockito.ArgumentMatchers.eq("and"),
                any(),
                any(),
                any()
        )).thenReturn(VisionPromptUnderstandingResult.builder()
                .sourceLanguage("en")
                .originalPrompt("and")
                .normalizedPrompt("and")
                .translationProvider("mock")
                .translationApplied(false)
                .translationReliable(true)
                .semanticPlan(VisionSemanticPlan.viewCircles(0.4d, "weak ambiguous follow-up"))
                .slots(new VisionPromptUnderstandingSlots())
                .build());

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(94L)
                        .prompt("and")
                        .build(),
                currentUser
        );

        assertEquals(94L, response.getConversationId());
        assertEquals("CREATE_QUEST", response.getIntent());
    }

    @Test
    void keepsSameConversationWhenProfileWorkspaceSwitchesFromViewToMutation() {
        VisionConversation conversation = new VisionConversation();
        conversation.setId(91L);
        conversation.setOwner(currentUser);
        conversation.setIntent(com.themuffinman.app.vision.model.VisionIntent.VIEW_PROFILE);
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        conversation.setSlotData(new LinkedHashMap<>());
        when(visionConversationRepository.findByIdAndOwner(91L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.previewProfileDraft(any(), any(), any())).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("update_profile")
                        .title("Profile draft")
                        .summary("Review the profile draft.")
                        .items(List.of())
                        .tone("info")
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(91L)
                        .prompt("change username to vision-josip")
                        .build(),
                currentUser
        );

        assertEquals(91L, response.getConversationId());
        assertEquals("UPDATE_PROFILE", response.getIntent());
    }

    @Test
    void keepsSameConversationWhenCirclesWorkspaceSwitchesFromViewToMutation() {
        VisionConversation conversation = new VisionConversation();
        conversation.setId(92L);
        conversation.setOwner(currentUser);
        conversation.setIntent(com.themuffinman.app.vision.model.VisionIntent.VIEW_CIRCLES);
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        conversation.setSlotData(new LinkedHashMap<>());
        when(visionConversationRepository.findByIdAndOwner(92L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.previewCircleDraft(any())).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("create_circle")
                        .title("Circle draft")
                        .summary("Review the circle draft.")
                        .items(List.of())
                        .tone("info")
                        .build()
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(92L)
                        .prompt("create circle Neighbours")
                        .build(),
                currentUser
        );

        assertEquals(92L, response.getConversationId());
        assertEquals("CREATE_CIRCLE", response.getIntent());
    }

    @Test
    void keepsSameConversationWhenApplicationsWorkspaceSwitchesFromViewToMutation() {
        VisionConversation conversation = new VisionConversation();
        conversation.setId(93L);
        conversation.setOwner(currentUser);
        conversation.setIntent(com.themuffinman.app.vision.model.VisionIntent.VIEW_APPLICATIONS);
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        conversation.setSlotData(new LinkedHashMap<>());
        when(visionConversationRepository.findByIdAndOwner(93L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L, 1L);
        when(visionCapabilityPreviewService.previewApplicationDraft(any(), any(), any(), anyBoolean(), any(), any())).thenReturn(
                com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO.builder()
                        .capabilityId("create_application")
                        .title("Application draft")
                        .summary("Review the application draft.")
                        .items(List.of())
                        .tone("info")
                        .build()
        );
        when(visionCapabilityPreviewService.resolveApplicationQuest(any(), any())).thenReturn(
                VisionResolvedQuestTarget.resolved(501L, "Move sofa", "quest-owner", true, "20 EUR")
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(93L)
                        .prompt("apply to Move sofa")
                        .build(),
                currentUser
        );

        assertEquals(93L, response.getConversationId());
        assertEquals("CREATE_APPLICATION", response.getIntent());
    }

    @Test
    void recentConversationSummariesIncludeAppliedSlotBadges() {
        VisionConversation conversation = createQuestConversation(
                61L,
                "location_mode",
                VisionSlotStatePresets.createQuestReviewReadyProfileLocation()
        );
        when(visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(currentUser)).thenReturn(List.of(conversation));

        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setTurnIndex(1);
        turn.setAppliedSlotIds(List.of("location_mode"));
        when(visionTurnRepository.findTopByConversationOrderByTurnIndexDesc(conversation)).thenReturn(Optional.of(turn));

        VisionConversationListResponseDTO response = visionConversationService.listRecentConversations(currentUser);

        assertEquals(1, response.getItems().size());
        assertFalse(response.getItems().get(0).getAppliedSlotSummaries().isEmpty());
        assertTrue(response.getItems().get(0).getAppliedSlotSummaries().stream().anyMatch(slot -> "location_mode".equals(slot.getSlotId())));
    }

    @Test
    void startsCreateQuestConversationAndAsksForMissingSlot() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("Create a quest")
                        .source("text")
                        .build(),
                currentUser
        );

        assertNotNull(response.getConversationId());
        assertEquals("CREATE_QUEST", response.getIntent());
        assertEquals("ASK_FOR_SLOT", response.getNextAction());
        assertEquals("quest_title", response.getRequestedSlot());
        assertEquals("What should the quest be called?", response.getMessage());
        assertEquals("clarification", response.getCanvasMode());
        assertEquals("field_request", response.getBlocks().get(response.getBlocks().size() - 1).getType());
    }

    @Test
    void lowConfidenceCreateQuestDraftAsksForClarificationInsteadOfReviewing() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L);
        when(visionPromptUnderstandingService.understandPrompt(
                org.mockito.ArgumentMatchers.eq("Create a quest to move a sofa"),
                any(),
                any(),
                any()
        )).thenReturn(VisionPromptUnderstandingResult.builder()
                .sourceLanguage("en")
                .originalPrompt("Create a quest to move a sofa")
                .normalizedPrompt("Create a quest to move a sofa")
                .translationProvider("mock")
                .translationApplied(false)
                .translationReliable(true)
                .semanticPlan(VisionSemanticPlan.createQuest(0.42d, "low confidence create quest"))
                .slots(VisionPromptUnderstandingSlots.builder()
                        .questTitle("Move a sofa")
                        .questTitleConfidence(1.0d)
                        .questDescription("Move a sofa from one room to another.")
                        .questDescriptionConfidence(1.0d)
                        .visibility("PUBLIC")
                        .visibilityConfidence(1.0d)
                        .reward(VisionPromptUnderstandingRewardSlots.builder()
                                .amount("0")
                                .amountConfidence(1.0d)
                                .build())
                        .schedule(VisionPromptUnderstandingScheduleSlots.builder()
                                .mode("agreement")
                                .modeConfidence(1.0d)
                                .build())
                        .location(VisionPromptUnderstandingLocationSlots.builder()
                                .mode("off")
                                .modeConfidence(1.0d)
                                .build())
                        .build())
                .build());

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("Create a quest to move a sofa")
                        .source("text")
                        .build(),
                currentUser
        );

        assertEquals("ASK_FOR_SLOT", response.getNextAction());
        assertEquals("quest_title", response.getRequestedSlot());
        assertEquals("I can draft the quest, but I need a clearer title or task before I can review it.", response.getMessage());
        assertEquals("clarification", response.getCanvasMode());
    }

    @Test
    void continuesConversationStepByStepUntilReview() {
        VisionConversation conversation = createQuestConversation(42L, "quest_title");
        when(visionConversationRepository.findByIdAndOwner(42L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(
                1L, 1L,
                2L, 2L,
                3L, 3L,
                4L, 4L,
                5L, 5L,
                6L, 6L
        );

        VisionConversationTurnResponseDTO titleResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(42L)
                        .prompt("Help move a sofa")
                        .build(),
                currentUser
        );

        assertEquals("quest_description", titleResponse.getRequestedSlot());
        assertFalse(titleResponse.getAppliedSlotSummaries().isEmpty());
        assertTrue(titleResponse.getAppliedSlotSummaries().stream().anyMatch(slot -> "quest_title".equals(slot.getSlotId())));

        VisionConversationTurnResponseDTO descriptionResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(42L)
                        .prompt("I need someone to help carry a sofa to a third-floor flat")
                        .build(),
                currentUser
        );

        assertEquals("reward_amount", descriptionResponse.getRequestedSlot());

        VisionConversationTurnResponseDTO rewardResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(42L)
                        .prompt("20 euros")
                        .build(),
                currentUser
        );

        assertEquals("visibility", rewardResponse.getRequestedSlot());

        VisionConversationTurnResponseDTO visibilityResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(42L)
                        .prompt("public")
                        .build(),
                currentUser
        );

        assertEquals("schedule_mode", visibilityResponse.getRequestedSlot());

        VisionConversationTurnResponseDTO scheduleResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(42L)
                        .prompt("by agreement")
                        .build(),
                currentUser
        );

        assertEquals("location_mode", scheduleResponse.getRequestedSlot());

        VisionConversationTurnResponseDTO reviewResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(42L)
                        .prompt("use profile location")
                        .build(),
                currentUser
        );

        assertEquals("SHOW_REVIEW", reviewResponse.getNextAction());
        assertNotNull(reviewResponse.getReview());
        assertFalse(reviewResponse.getAppliedSlotSummaries().isEmpty());
        assertEquals("Help move a sofa", reviewResponse.getReview().getTitle());
        assertEquals("20", reviewResponse.getReview().getRewardLabel());
        assertEquals("PUBLIC", reviewResponse.getReview().getVisibility());
        assertEquals("By agreement", reviewResponse.getReview().getSchedule());
        assertEquals("Use profile location", reviewResponse.getReview().getLocation());
        assertEquals("review", reviewResponse.getCanvasMode());
        assertEquals("review_summary", reviewResponse.getBlocks().get(reviewResponse.getBlocks().size() - 1).getType());
    }

    @Test
    void passesExistingConversationContextIntoPromptUnderstanding() {
        VisionConversation conversation = createQuestConversation(77L, "location_mode");
        when(visionConversationRepository.findByIdAndOwner(77L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(1L, 1L);

        visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(77L)
                        .prompt("use profile location")
                        .source("text")
                        .inputType("text")
                        .clientLocale("de-CH")
                        .clientTimezone("Europe/Zurich")
                        .build(),
                currentUser
        );

        verify(visionPromptUnderstandingService).understandPrompt(
                org.mockito.ArgumentMatchers.eq("use profile location"),
                org.mockito.ArgumentMatchers.eq(conversation),
                org.mockito.ArgumentMatchers.eq(currentUser),
                org.mockito.ArgumentMatchers.argThat(runtimeHints ->
                        runtimeHints != null
                                && "text".equals(runtimeHints.getInputType())
                                && "de-CH".equals(runtimeHints.getClientLocale())
                                && "Europe/Zurich".equals(runtimeHints.getClientTimezone()))
        );
    }

    @Test
    void fixedScheduleAndCustomLocationReachReview() {
        VisionConversation conversation = createQuestConversation(
                52L,
                "schedule_mode",
                VisionSlotStatePresets.createQuestBaseDetails()
        );

        when(visionConversationRepository.findByIdAndOwner(52L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(4L, 4L, 5L, 5L, 6L, 6L);
        VisionConversationTurnResponseDTO fixedModeResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(52L)
                        .prompt("fixed time")
                        .build(),
                currentUser
        );

        assertEquals("scheduled_date", fixedModeResponse.getRequestedSlot());

        VisionConversationTurnResponseDTO fixedTimeResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(52L)
                        .prompt(VisionSchedulePhrasePresets.ISO_DATE_TIME)
                        .build(),
                currentUser
        );

        assertEquals("location_mode", fixedTimeResponse.getRequestedSlot());

        VisionConversationTurnResponseDTO customLocationResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(52L)
                        .prompt("custom place")
                        .build(),
                currentUser
        );

        assertEquals("location_label", customLocationResponse.getRequestedSlot());

        VisionConversationTurnResponseDTO reviewResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(52L)
                        .prompt(VisionLocationCandidatePresets.BAN_JELACIC)
                        .build(),
                currentUser
        );

        assertEquals("SHOW_REVIEW", reviewResponse.getNextAction());
        assertEquals(VisionLocationCandidatePresets.BAN_JELACIC, reviewResponse.getReview().getLocation());
    }

    @Test
    void fixedRelativeDateWithoutExplicitTimeAdvancesPastScheduledAt() {
        VisionConversation conversation = createQuestConversation(
                521L,
                "schedule_mode",
                VisionSlotStatePresets.createQuestBaseDetails()
        );

        when(visionConversationRepository.findByIdAndOwner(521L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(4L, 4L, 5L, 5L);

        VisionConversationTurnResponseDTO fixedModeResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(521L)
                        .prompt(VisionSchedulePhrasePresets.FIXED_NEXT_TUESDAY)
                        .build(),
                currentUser
        );

        assertEquals("scheduled_time", fixedModeResponse.getRequestedSlot());
        assertEquals("fixed", conversation.getSlotData().get("schedule_mode"));
        assertEquals("2026-07-07", conversation.getSlotData().get("scheduled_date"));
        assertNull(conversation.getSlotData().get("scheduled_time"));
    }

    @Test
    void structuredCustomLocationIsSummarizedInReview() {
        VisionConversation conversation = createQuestConversation(
                53L,
                "location_label",
                VisionSlotStatePresets.createQuestAgreementCustomLocation()
        );

        when(visionConversationRepository.findByIdAndOwner(53L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(6L, 6L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(53L)
                        .prompt(VisionLocationCandidatePresets.ILICA_RESOLVED)
                        .build(),
                currentUser
        );

        assertEquals("SHOW_REVIEW", response.getNextAction());
        assertEquals(VisionLocationCandidatePresets.ILICA_RESOLVED, response.getReview().getLocation());
    }

    @Test
    void customLocationShowsParsedInfoBlockBeforeReview() {
        VisionConversation conversation = createQuestConversation(
                54L,
                "location_label",
                VisionSlotStatePresets.createQuestAgreementCustomLocation()
        );

        when(visionConversationRepository.findByIdAndOwner(54L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(6L, 6L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(54L)
                        .prompt(VisionLocationCandidatePresets.BAN_JELACIC)
                        .build(),
                currentUser
        );

        assertEquals("SHOW_REVIEW", response.getNextAction());
        assertTrue(response.getBlocks().stream().anyMatch(block -> "info".equals(block.getType())));
    }

    @Test
    void customLocationLookupRequiresExplicitConfirmationBeforeReview() {
        VisionConversation conversation = createQuestConversation(
                55L,
                "location_label",
                VisionSlotStatePresets.createQuestAgreementCustomLocation()
        );

        when(visionConversationRepository.findByIdAndOwner(55L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(6L, 6L, 7L, 7L);
        when(locationLookupService.lookupTopCandidates(VisionLocationCandidatePresets.ILICA_TYPED, "vision:user:7", 3))
                .thenReturn(List.of(VisionLocationCandidatePresets.ilicaResolvedCandidate()));

        VisionConversationTurnResponseDTO candidateResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(55L)
                        .prompt(VisionLocationCandidatePresets.ILICA_TYPED)
                        .build(),
                currentUser
        );

        assertEquals("ASK_FOR_SLOT", candidateResponse.getNextAction());
        assertEquals("location_candidate_confirmation", candidateResponse.getRequestedSlot());
        assertTrue(candidateResponse.getBlocks().stream().anyMatch(block -> "Location match found".equals(block.getTitle())));

        VisionConversationTurnResponseDTO reviewResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(55L)
                        .prompt("yes")
                        .build(),
                currentUser
        );

        assertEquals("SHOW_REVIEW", reviewResponse.getNextAction());
        assertEquals(VisionLocationCandidatePresets.ILICA_RESOLVED, reviewResponse.getReview().getLocation());
    }

    @Test
    void locationCandidateOptionsSupportSelectingSecondCandidate() {
        VisionConversation conversation = createQuestConversation(
                551L,
                "location_label",
                VisionSlotStatePresets.createQuestAgreementCustomLocation()
        );

        when(visionConversationRepository.findByIdAndOwner(551L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(6L, 6L, 7L, 7L);
        when(locationLookupService.lookupTopCandidates(VisionLocationCandidatePresets.ILICA_TYPED, "vision:user:7", 3))
                .thenReturn(VisionLocationCandidatePresets.dualIlicaCandidates());

        VisionConversationTurnResponseDTO candidateResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(551L)
                        .prompt(VisionLocationCandidatePresets.ILICA_TYPED)
                        .build(),
                currentUser
        );

        assertEquals("location_candidate_confirmation", candidateResponse.getRequestedSlot());
        assertEquals(3, candidateResponse.getBlocks().get(candidateResponse.getBlocks().size() - 1).getOptions().size());
        assertTrue(candidateResponse.getBlocks().stream().anyMatch(block ->
                block.getBody() != null && block.getBody().contains("You can also keep the typed location if none of the candidates is correct.")));
        assertTrue(candidateResponse.getBlocks().get(candidateResponse.getBlocks().size() - 1).getOptions().get(0).getLabel().contains("Candidate 1:"));
        assertTrue(candidateResponse.getBlocks().get(candidateResponse.getBlocks().size() - 1).getOptions().get(2).getLabel().contains("Keep typed location exactly as entered"));

        VisionConversationTurnResponseDTO reviewResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(551L)
                        .prompt("candidate 2")
                        .build(),
                currentUser
        );

        assertEquals("SHOW_REVIEW", reviewResponse.getNextAction());
        assertEquals(VisionLocationCandidatePresets.ILICA_RESOLVED, reviewResponse.getReview().getLocation());
    }

    @Test
    void ambiguousLocationCandidateAnswerReturnsRetryHintForSameSlot() {
        VisionConversation conversation = new VisionConversation();
        conversation.setId(57L);
        conversation.setOwner(currentUser);
        conversation.setIntent(com.themuffinman.app.vision.model.VisionIntent.CREATE_QUEST);
        conversation.setRequestedSlot("location_candidate_confirmation");
        conversation.setSlotData(new LinkedHashMap<>());
        conversation.getSlotData().put("quest_title", "Garden cleanup");
        conversation.getSlotData().put("quest_description", "Need help cleaning up the garden.");
        conversation.getSlotData().put("reward_amount", "30");
        conversation.getSlotData().put("visibility", "CIRCLES");
        conversation.getSlotData().put("schedule_mode", "agreement");
        conversation.getSlotData().put("location_mode", "custom");
        conversation.getSlotData().put("location_label", "Ilica 10, Zagreb");
        conversation.getSlotData().put("location_resolution_status", "candidate_pending");
        conversation.getSlotData().put("pending_location_label", "Ilica 10, 10000 Zagreb, Croatia");

        when(visionConversationRepository.findByIdAndOwner(57L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(7L, 7L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(57L)
                        .prompt("maybe")
                        .build(),
                currentUser
        );

        assertEquals("ASK_FOR_SLOT", response.getNextAction());
        assertEquals("location_candidate_confirmation", response.getRequestedSlot());
        assertEquals("Choose one: use resolved place, or keep typed location.", response.getMessage());
    }

    @Test
    void customLocationCandidateCanKeepTypedLocationBeforeReview() {
        VisionConversation conversation = createQuestConversation(
                56L,
                "location_label",
                VisionSlotStatePresets.createQuestAgreementCustomLocation()
        );

        when(visionConversationRepository.findByIdAndOwner(56L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(6L, 6L, 7L, 7L);
        when(locationLookupService.lookupTopCandidates(VisionLocationCandidatePresets.ILICA_TYPED, "vision:user:7", 3))
                .thenReturn(List.of(VisionLocationCandidatePresets.ilicaResolvedCandidate()));

        VisionConversationTurnResponseDTO candidateResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(56L)
                        .prompt(VisionLocationCandidatePresets.ILICA_TYPED)
                        .build(),
                currentUser
        );

        assertEquals("location_candidate_confirmation", candidateResponse.getRequestedSlot());

        VisionConversationTurnResponseDTO reviewResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(56L)
                        .prompt("keep typed location")
                        .build(),
                currentUser
        );

        assertEquals("SHOW_REVIEW", reviewResponse.getNextAction());
        assertEquals(VisionLocationCandidatePresets.ILICA_TYPED, reviewResponse.getReview().getLocation());
    }

    @Test
    void spokenSchedulePhraseAdvancesToLocationStep() {
        VisionConversation conversation = createQuestConversation(
                58L,
                "scheduled_time",
                VisionSlotStatePresets.createQuestFixedSchedule()
        );

        when(visionConversationRepository.findByIdAndOwner(58L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(5L, 5L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(58L)
                        .prompt(VisionSchedulePhrasePresets.TOMORROW_2_PM)
                        .build(),
                currentUser
        );

        assertEquals("ASK_FOR_SLOT", response.getNextAction());
        assertEquals("location_mode", response.getRequestedSlot());
    }

    @Test
    void typedReviewEditRequestRetargetsOneFieldAtATime() {
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(61L, currentUser)
                .status(VisionConversationStatus.REVIEW_READY)
                .slots(VisionSlotStatePresets.createQuestReviewReadyProfileLocation())
                .build();

        when(visionConversationRepository.findByIdAndOwner(61L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(6L, 6L, 7L, 7L);

        VisionConversationTurnResponseDTO editRequestResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(61L)
                        .action("REQUEST_REVIEW_EDIT")
                        .reviewTarget("REWARD")
                        .build(),
                currentUser
        );

        assertEquals("ASK_FOR_SLOT", editRequestResponse.getNextAction());
        assertEquals("reward_amount", editRequestResponse.getRequestedSlot());

        VisionConversationTurnResponseDTO reviewResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(61L)
                        .prompt("35 euros")
                        .build(),
                currentUser
        );

        assertEquals("SHOW_REVIEW", reviewResponse.getNextAction());
        assertEquals("35", reviewResponse.getReview().getRewardLabel());
    }

    @Test
    void naturalLanguageReviewEditNoLongerRetargetsReviewState() {
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(62L, currentUser)
                .status(VisionConversationStatus.REVIEW_READY)
                .slots(VisionSlotStatePresets.createQuestReviewReadyProfileLocation())
                .build();

        when(visionConversationRepository.findByIdAndOwner(62L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(6L, 6L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(62L)
                        .prompt("change schedule")
                        .build(),
                currentUser
        );

        assertEquals("SHOW_REVIEW", response.getNextAction());
        assertEquals("review", response.getCanvasMode());
        assertEquals("The review is ready, but execution is still disabled. Use the explicit review controls if you want to revise one field.", response.getMessage());
    }

    @Test
    void invalidScheduledAtAnswerReturnsRetryHintForSameSlot() {
        VisionConversation conversation = createQuestConversation(
                71L,
                "scheduled_time",
                VisionSlotStatePresets.createQuestFixedSchedule()
        );
        conversation.getSlotData().put("scheduled_date", "2026-07-03");

        when(visionConversationRepository.findByIdAndOwner(71L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(5L, 5L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(71L)
                        .prompt("sometime later")
                        .build(),
                currentUser
        );

        assertEquals("ASK_FOR_SLOT", response.getNextAction());
        assertEquals("scheduled_time", response.getRequestedSlot());
        assertEquals("I still need the time. Use a format like 14:30, 2 pm, noon, or this evening.", response.getMessage());
    }

    @Test
    void genericCustomLocationAnswerReturnsRetryHintForSameSlot() {
        VisionConversation conversation = createQuestConversation(
                72L,
                "location_label",
                VisionSlotStatePresets.createQuestAgreementCustomLocation()
        );

        when(visionConversationRepository.findByIdAndOwner(72L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(6L, 6L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(72L)
                        .prompt("custom place")
                        .build(),
                currentUser
        );

        assertEquals("ASK_FOR_SLOT", response.getNextAction());
        assertEquals("location_label", response.getRequestedSlot());
        assertEquals("I still need a real custom place or address, not just 'custom place'.", response.getMessage());
    }

    @Test
    void unsupportedPromptReturnsBlockedState() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("What is the weather today?")
                        .build(),
                currentUser
        );

        assertEquals("UNSUPPORTED", response.getIntent());
        assertEquals("BLOCKED", response.getNextAction());
    }

    @Test
    void openChatPromptOpensConversationThroughChatBoundary() {
        when(visionTurnRepository.countByConversation(any(VisionConversation.class))).thenReturn(0L);
        when(visionChatExecutionService.openChat(any(AppUser.class), any(String.class), any(VisionSemanticPlan.class))).thenReturn(
                VisionChatExecutionResult.executed(ChatConversationSummaryDTO.builder()
                        .conversationId(301L)
                        .otherUserId(8L)
                        .otherUsername("Josip")
                        .resolutionKey("conversation:301")
                        .resolutionLabel("Chat with Josip")
                        .exactResolutionEligible(true)
                        .build())
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .prompt("chat with Josip")
                        .build(),
                currentUser
        );

        assertEquals("OPEN_CHAT", response.getIntent());
        assertEquals("COMPLETE", response.getNextAction());
        assertEquals("complete", response.getCanvasMode());
        assertEquals("Chat opened with Josip.", response.getMessage());
        assertTrue(response.getBlocks().stream().anyMatch(block -> "success".equals(block.getType())));
    }

    @Test
    void confirmedReviewExecutesQuestWhenExecutionIsEnabled() {
        visionProperties.setExecutionEnabled(true);

        VisionConversation conversation = VisionConversationTestBuilder.createQuest(77L, currentUser)
                .status(VisionConversationStatus.REVIEW_READY)
                .slots(VisionSlotStatePresets.createQuestReviewReadyProfileLocation())
                .build();

        Quest createdQuest = new Quest();
        createdQuest.setId(501L);

        when(visionConversationRepository.findByIdAndOwner(77L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(4L, 4L);
        when(visionCreateQuestExecutionAdapter.execute(conversation)).thenReturn(
                VisionExecutionResult.executed("create_quest", createdQuest)
        );

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(77L)
                        .action("CONFIRM_REVIEW")
                        .build(),
                currentUser
        );

        assertEquals("COMPLETE", response.getAgentState());
        assertEquals("COMPLETE", response.getNextAction());
        assertEquals("complete", response.getCanvasMode());
        assertNotNull(response.getExecutionCandidate());
        assertEquals("Conversation is already complete.", response.getExecutionCandidate().getBlockingReason());
        assertFalse(response.getExecutionCandidate().isExecutionReady());
        assertEquals("success", response.getBlocks().get(response.getBlocks().size() - 1).getType());
    }

    @Test
    void resetConversationClearsConversationAndRestartsSlotFlow() {
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(88L, currentUser)
                .status(VisionConversationStatus.REVIEW_READY)
                .slot("quest_title", "Old title")
                .build();

        when(visionConversationRepository.findByIdAndOwner(88L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(1L, 1L);

        VisionConversationTurnResponseDTO response = visionConversationService.resetConversation(88L, currentUser);

        assertEquals("ASK_FOR_SLOT", response.getNextAction());
        assertEquals("quest_title", response.getRequestedSlot());
        assertEquals("The current vision task was reset. What should the new quest be called?", response.getMessage());
    }

    @Test
    void cancelConversationCompletesWithoutSuccessExecutionBlock() {
        VisionConversation conversation = createQuestConversation(89L, "reward_amount");

        when(visionConversationRepository.findByIdAndOwner(89L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(2L, 2L);

        VisionConversationTurnResponseDTO response = visionConversationService.cancelConversation(89L, currentUser);

        assertEquals("COMPLETE", response.getNextAction());
        assertEquals("warning", response.getBlocks().get(response.getBlocks().size() - 1).getType());
    }

    @Test
    void cancelPromptShortCircuitsActiveQuestConversation() {
        VisionConversation conversation = createQuestConversation(90L, "visibility");

        when(visionConversationRepository.findByIdAndOwner(90L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(2L, 2L);

        VisionConversationTurnResponseDTO response = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(90L)
                        .prompt("cancel")
                        .build(),
                currentUser
        );

        assertEquals("COMPLETE", response.getNextAction());
        assertEquals("cancelled", savedConversations.get(response.getConversationId()).getSlotData().get("conversation_outcome"));
        assertEquals("The current vision task was cancelled. Start a new task when you want to continue.", response.getMessage());
        verify(visionPromptUnderstandingService, never()).understandPrompt(any(), any(), any(), any());
    }

    @Test
    void loadConversationReturnsLatestStateAndRecentSummaries() {
        VisionConversation conversation = createQuestConversation(99L, "reward_amount");
        conversation.setLastAssistantMessage("What is the reward amount, or should this quest be free?");
        conversation.setUpdatedAt(java.time.Instant.now());
        conversation.getSlotData().put("quest_title", "Help move a sofa");

        VisionTurn latestTurn = new VisionTurn();
        latestTurn.setId(901L);
        latestTurn.setConversation(conversation);
        latestTurn.setTurnIndex(3);
        latestTurn.setSource(com.themuffinman.app.vision.model.VisionTurnSource.TEXT);
        latestTurn.setPrompt("public");
        latestTurn.setNormalizedPrompt("public");
        latestTurn.setDetectedIntent(com.themuffinman.app.vision.model.VisionIntent.CREATE_QUEST);
        latestTurn.setAgentState(com.themuffinman.app.vision.model.VisionAgentState.ASKING);
        latestTurn.setNextAction(com.themuffinman.app.vision.model.VisionNextAction.ASK_FOR_SLOT);
        latestTurn.setRequestedSlot("reward_amount");
        latestTurn.setAssistantMessage("What is the reward amount, or should this quest be free?");

        when(visionConversationRepository.findByIdAndOwner(99L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(currentUser)).thenReturn(List.of(conversation));
        when(visionTurnRepository.findTopByConversationOrderByTurnIndexDesc(conversation)).thenReturn(Optional.of(latestTurn));

        VisionConversationTurnResponseDTO response = visionConversationService.loadConversation(99L, currentUser);

        assertEquals(99L, response.getConversationId());
        assertEquals("reward_amount", response.getRequestedSlot());
        assertEquals(1, response.getRecentConversations().size());
        assertEquals("Help move a sofa", response.getRecentConversations().getFirst().getTitle());
        assertEquals("Needs input", response.getRecentConversations().getFirst().getStageLabel());
        assertEquals("Next step: What is the reward amount, or should this quest be free?",
                response.getRecentConversations().getFirst().getProgressLabel());
        assertEquals("active", response.getRecentConversations().getFirst().getGroupKey());
        assertTrue(response.getRecentConversations().getFirst().isResumable());
        assertTrue(!response.getRecentConversations().getFirst().isCompleted());
        assertTrue(!response.getRecentConversations().getFirst().isStale());
    }

    @Test
    void recentConversationSummaryMarksCompletedTaskAsNotResumable() {
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(109L, currentUser)
                .status(VisionConversationStatus.COMPLETED)
                .slot("quest_title", "Finished quest")
                .slot("conversation_outcome", "cancelled")
                .build();
        conversation.setLastAssistantMessage("The current vision task was cancelled. Start a new task when you want to continue.");
        conversation.setUpdatedAt(java.time.Instant.parse("2026-06-30T19:00:00Z"));

        when(visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(currentUser)).thenReturn(List.of(conversation));

        VisionConversationListResponseDTO response = visionConversationService.listRecentConversations(currentUser);

        assertEquals(1, response.getItems().size());
        assertEquals("Complete", response.getItems().getFirst().getStageLabel());
        assertEquals("Task finished.", response.getItems().getFirst().getProgressLabel());
        assertEquals("completed", response.getItems().getFirst().getGroupKey());
        assertTrue(!response.getItems().getFirst().isResumable());
        assertTrue(response.getItems().getFirst().isCompleted());
    }

    @Test
    void recentConversationSummaryMarksOldTaskAsStale() {
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(119L, currentUser)
                .requestedSlot("quest_description")
                .slot("quest_title", "Old task")
                .build();
        conversation.setUpdatedAt(java.time.Instant.parse("2026-06-28T10:00:00Z"));
        conversation.setLastAssistantMessage("Describe the task in one or two clear sentences.");

        when(visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(currentUser)).thenReturn(List.of(conversation));

        VisionConversationListResponseDTO response = visionConversationService.listRecentConversations(currentUser);

        assertEquals(1, response.getItems().size());
        assertTrue(response.getItems().getFirst().isStale());
        assertEquals("active", response.getItems().getFirst().getGroupKey());
    }

    @Test
    void recentConversationSummaryShowsTopicSwitchHintsAcrossFamilies() {
        VisionConversation circlesConversation = VisionConversationTestBuilder.createQuest(130L, currentUser)
                .build();
        circlesConversation.setIntent(com.themuffinman.app.vision.model.VisionIntent.CREATE_CIRCLE);
        circlesConversation.setUpdatedAt(java.time.Instant.parse("2026-07-03T11:00:00Z"));
        circlesConversation.setSlotData(new LinkedHashMap<>(java.util.Map.of("circle_name", "Neighbours")));

        VisionConversation questConversation = VisionConversationTestBuilder.createQuest(129L, currentUser)
                .slot("quest_title", "Move a sofa")
                .build();
        questConversation.setUpdatedAt(java.time.Instant.parse("2026-07-03T10:00:00Z"));

        when(visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(currentUser))
                .thenReturn(List.of(circlesConversation, questConversation));

        VisionConversationListResponseDTO response = visionConversationService.listRecentConversations(currentUser);

        assertEquals(2, response.getItems().size());
        assertEquals("circles", response.getItems().getFirst().getEntityFamily());
        assertNull(response.getItems().getFirst().getPreviousEntityFamily());
        assertNull(response.getItems().getFirst().getTopicSwitchHint());
        assertEquals("quests", response.getItems().get(1).getEntityFamily());
        assertEquals("circles", response.getItems().get(1).getPreviousEntityFamily());
        assertEquals("Switched from circles to quests.", response.getItems().get(1).getTopicSwitchHint());
    }

    @Test
    void lowConfidenceExtractedFieldsAreIgnoredBeforeMerge() {
        VisionPromptUnderstandingResult result = VisionPromptUnderstandingResult.builder()
                .sourceLanguage("en")
                .originalPrompt("Move a sofa")
                .normalizedPrompt("Move a sofa")
                .translationProvider("mock")
                .translationApplied(false)
                .translationReliable(true)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .questTitle("Move a sofa")
                        .questTitleConfidence(0.6)
                        .questDescription("Need help moving a sofa")
                        .questDescriptionConfidence(0.92)
                        .reward(VisionPromptUnderstandingRewardSlots.builder()
                                .freeQuest(false)
                                .freeQuestConfidence(0.4)
                                .amount("25")
                                .amountConfidence(0.9)
                                .build())
                        .location(VisionPromptUnderstandingLocationSlots.builder()
                                .mode("custom")
                                .modeConfidence(0.9)
                                .label("Ban Jelacic Square")
                                .labelConfidence(0.95)
                                .build())
                        .build())
                .build();

        var extracted = result.toExtractedSlotMap();

        assertFalse(extracted.containsKey("quest_title"));
        assertTrue(extracted.containsKey("quest_description"));
        assertFalse(extracted.containsKey("free_quest"));
        assertTrue(extracted.containsKey("reward_amount"));
        assertTrue(extracted.containsKey("location_mode"));
        assertTrue(extracted.containsKey("location_label"));
    }

    private VisionPromptUnderstandingResult understanding(String prompt, VisionConversation conversation) {
        String normalizedPrompt = prompt == null ? "" : prompt.trim();
        VisionSemanticPlan semanticPlan = normalizedPrompt.equalsIgnoreCase("show me open quests for moving help")
                ? VisionSemanticPlan.discoverQuests(0.9d, "browse available quests", "moving help")
                : (normalizedPrompt.toLowerCase().contains("show me open quests")
                || normalizedPrompt.toLowerCase().contains("open quests")
                || normalizedPrompt.toLowerCase().contains("find quests")
                ? VisionSemanticPlan.discoverQuests(0.85d, "browse available quests", "moving help")
                : VisionSemanticPlan.empty());
        if (normalizedPrompt.toLowerCase().contains("create new circle")
                || normalizedPrompt.toLowerCase().contains("create circle")) {
            semanticPlan = VisionSemanticPlan.createCircle(0.95d, "mock create circle");
        }
        if (normalizedPrompt.toLowerCase().contains("send circle request")) {
            semanticPlan = VisionSemanticPlan.createCircleRequest(0.95d, "mock create circle request", "Josip");
        }
        if (normalizedPrompt.toLowerCase().contains("accept circle request")) {
            semanticPlan = VisionSemanticPlan.acceptCircleRequest(0.95d, "mock accept circle request", "Josip");
        }
        if (normalizedPrompt.toLowerCase().contains("decline circle request")) {
            semanticPlan = VisionSemanticPlan.deleteCircleRequest(0.95d, "mock delete circle request", "Josip");
        }
        if (normalizedPrompt.toLowerCase().contains("rename circle")) {
            semanticPlan = VisionSemanticPlan.updateCircle(0.95d, "mock update circle");
        }
        if (normalizedPrompt.toLowerCase().contains("delete circle")) {
            semanticPlan = VisionSemanticPlan.deleteCircle(0.95d, "mock delete circle");
        }
        if (normalizedPrompt.toLowerCase().contains("open circle")) {
            semanticPlan = VisionSemanticPlan.viewCircleDetail(0.95d, "mock view circle detail");
        }
        if (normalizedPrompt.toLowerCase().contains("withdraw my application")) {
            semanticPlan = VisionSemanticPlan.withdrawApplication(0.95d, "mock withdraw application");
        }
        if (normalizedPrompt.toLowerCase().contains("update my application")) {
            semanticPlan = VisionSemanticPlan.updateApplication(0.95d, "mock update application");
        }
        if (normalizedPrompt.toLowerCase().contains("approve application")) {
            semanticPlan = VisionSemanticPlan.approveApplication(0.95d, "mock approve application", "");
        }
        if (normalizedPrompt.toLowerCase().contains("decline application")) {
            semanticPlan = VisionSemanticPlan.declineApplication(0.95d, "mock decline application", "");
        }
        if (normalizedPrompt.toLowerCase().contains("apply to")) {
            semanticPlan = VisionSemanticPlan.createApplication(0.95d, "mock create application");
        }
        if (normalizedPrompt.toLowerCase().contains("update my profile")
                || normalizedPrompt.toLowerCase().contains("update my username")
                || normalizedPrompt.toLowerCase().contains("change username")
                || normalizedPrompt.toLowerCase().contains("set description")) {
            semanticPlan = VisionSemanticPlan.updateProfile(0.95d, "mock update profile");
        }
        if (normalizedPrompt.toLowerCase().contains("set my profile location")) {
            semanticPlan = VisionSemanticPlan.updateProfileLocation(0.95d, "mock update profile location");
        }
        if (normalizedPrompt.toLowerCase().contains("show settings")) {
            semanticPlan = VisionSemanticPlan.viewSettings(0.95d, "mock view settings");
        }
        if (normalizedPrompt.toLowerCase().contains("show chat")) {
            semanticPlan = VisionSemanticPlan.viewChatWorkspace(0.95d, "mock view chat workspace");
        }
        if (normalizedPrompt.toLowerCase().contains("show circles")) {
            semanticPlan = VisionSemanticPlan.viewCircles(0.95d, "mock view circles");
        }
        if (normalizedPrompt.toLowerCase().contains("show applications")) {
            semanticPlan = VisionSemanticPlan.viewApplications(0.95d, "mock view applications");
        }
        if (normalizedPrompt.toLowerCase().contains("show user")) {
            semanticPlan = VisionSemanticPlan.viewUserProfile(0.95d, "mock view user profile", "Josip");
        }
        if (normalizedPrompt.toLowerCase().contains("show quest")) {
            semanticPlan = VisionSemanticPlan.viewQuestDetail(0.95d, "mock view quest detail");
        }
        if (normalizedPrompt.toLowerCase().contains("show application ")) {
            semanticPlan = VisionSemanticPlan.viewApplicationDetail(0.95d, "mock view application detail");
        }
        if (normalizedPrompt.toLowerCase().contains("show my profile")) {
            semanticPlan = VisionSemanticPlan.viewProfile(0.95d, "mock view profile");
        }
        if (normalizedPrompt.equals("Create a structured quest")
                || normalizedPrompt.toLowerCase().contains("create a quest")
                || normalizedPrompt.toLowerCase().contains("i need someone to help carry a sofa")) {
            semanticPlan = VisionSemanticPlan.createQuest(0.95d, "mock create quest");
        }
        if ("Josip".equalsIgnoreCase(normalizedPrompt)
                && conversation != null
                && "target_user".equals(conversation.getRequestedSlot())) {
            semanticPlan.setTargetUserQuery("Josip");
        }
        return VisionPromptUnderstandingResult.builder()
                .sourceLanguage("en")
                .originalPrompt(normalizedPrompt)
                .normalizedPrompt(normalizedPrompt)
                .translationProvider("mock")
                .translationApplied(false)
                .translationReliable(true)
                .semanticPlan(semanticPlan)
                .slots(extractedSlotsFor(normalizedPrompt, conversation))
                .build();
    }

    private VisionPromptUnderstandingSlots extractedSlotsFor(String prompt, VisionConversation conversation) {
        if ("create circle Neighbours".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .circleName("Neighbours")
                    .circleNameConfidence(1.0)
                    .build();
        }
        if ("create new circle Lover".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder().build();
        }
        if ("send circle request to Josip".equalsIgnoreCase(prompt)
                || "accept circle request from Josip".equalsIgnoreCase(prompt)
                || "decline circle request from Josip".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder().build();
        }
        if ("apply to Move a sofa".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .applicationQuestQuery("Move a sofa")
                    .applicationQuestQueryConfidence(1.0d)
                    .build();
        }
        if ("rename circle Neighbours".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .targetCircleQuery("Neighbours")
                    .targetCircleQueryConfidence(1.0d)
                    .build();
        }
        if ("delete circle Neighbours".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .targetCircleQuery("Neighbours")
                    .targetCircleQueryConfidence(1.0d)
                    .build();
        }
        if ("open circle Family".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .targetCircleQuery("Family")
                    .targetCircleQueryConfidence(1.0d)
                    .build();
        }
        if ("update my application for Move a sofa".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .applicationQuestQuery("Move a sofa")
                    .applicationQuestQueryConfidence(1.0d)
                    .build();
        }
        if ("show application #42".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .applicationTargetQuery("application #42")
                    .applicationTargetQueryConfidence(1.0d)
                    .build();
        }
        if ("show quest #42".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .applicationQuestQuery("quest #42")
                    .applicationQuestQueryConfidence(1.0d)
                    .build();
        }
        if ("approve application for Move a sofa".equalsIgnoreCase(prompt)
                || "decline application for Move a sofa".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .applicationQuestQuery("Move a sofa")
                    .applicationQuestQueryConfidence(1.0d)
                    .build();
        }
        if ("withdraw my application for Move a sofa".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .applicationQuestQuery("Move a sofa")
                    .applicationQuestQueryConfidence(1.0d)
                    .build();
        }
        if ("Core Team".equalsIgnoreCase(prompt) && conversation != null && "circle_name".equals(conversation.getRequestedSlot())) {
            return VisionPromptUnderstandingSlots.builder()
                    .circleName("Core Team")
                    .circleNameConfidence(1.0d)
                    .build();
        }
        if ("message I can do it tomorrow".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .applicationMessage("I can do it tomorrow")
                    .applicationMessageConfidence(1.0d)
                    .build();
        }
        if ("message I can come earlier and price 25".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .applicationMessage("I can come earlier")
                    .applicationMessageConfidence(1.0d)
                    .applicationProposedPrice("25")
                    .applicationProposedPriceConfidence(1.0d)
                    .build();
        }
        if ("20".equals(prompt) && conversation != null && "application_proposed_price".equals(conversation.getRequestedSlot())) {
            return VisionPromptUnderstandingSlots.builder()
                    .applicationProposedPrice("20")
                    .applicationProposedPriceConfidence(1.0d)
                    .build();
        }
        if ("update my username to jsak".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .profileUsername("jsak")
                    .profileUsernameConfidence(1.0)
                    .build();
        }
        if ("set description to Reliable mover".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .profileDescription("Reliable mover")
                    .profileDescriptionConfidence(1.0)
                    .build();
        }
        if ("set my profile location to Zurich, Switzerland".equalsIgnoreCase(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .profileLocationMode("EXACT")
                    .profileLocationModeConfidence(1.0d)
                    .profileLocationLabel("Zurich, Switzerland")
                    .profileLocationLabelConfidence(1.0d)
                    .build();
        }
        if ("Create a structured quest".equals(prompt)) {
            return VisionPromptUnderstandingSlots.builder()
                    .questTitle("Move a sofa")
                    .questTitleConfidence(1.0)
                    .questDescription("Need help moving a sofa")
                    .questDescriptionConfidence(1.0)
                    .visibility("PUBLIC")
                    .visibilityConfidence(1.0)
                    .reward(VisionPromptUnderstandingRewardSlots.builder()
                            .freeQuest(false)
                            .freeQuestConfidence(1.0)
                            .amount("25")
                            .amountConfidence(1.0)
                            .build())
                    .schedule(VisionPromptUnderstandingScheduleSlots.builder()
                            .mode("fixed")
                            .modeConfidence(1.0)
                            .scheduledDate("2026-07-03")
                            .scheduledDateConfidence(1.0)
                            .scheduledTime("14:30")
                            .scheduledTimeConfidence(1.0)
                            .build())
                    .location(VisionPromptUnderstandingLocationSlots.builder()
                            .mode("profile")
                            .modeConfidence(1.0)
                            .build())
                    .build();
        }
        return new VisionPromptUnderstandingSlots();
    }

    private VisionConversation createQuestConversation(Long id, String requestedSlot) {
        return VisionConversationTestBuilder.createQuest(id, currentUser)
                .requestedSlot(requestedSlot)
                .build();
    }

    private VisionConversation createQuestConversation(Long id, String requestedSlot, java.util.Map<String, String> slotData) {
        return VisionConversationTestBuilder.createQuest(id, currentUser)
                .requestedSlot(requestedSlot)
                .slots(slotData)
                .build();
    }
}
