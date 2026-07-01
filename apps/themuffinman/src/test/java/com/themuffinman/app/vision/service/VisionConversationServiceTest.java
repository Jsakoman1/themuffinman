package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.service.LocationLookupService;
import com.themuffinman.app.testing.TestFixtures;
import com.themuffinman.app.vision.dto.VisionConversationListResponseDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnRequestDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.vision.repository.VisionTurnRepository;
import com.themuffinman.app.vision.testing.VisionConversationTestBuilder;
import com.themuffinman.app.vision.testing.VisionLocationCandidatePresets;
import com.themuffinman.app.vision.testing.VisionSchedulePhrasePresets;
import com.themuffinman.app.vision.testing.VisionSlotStatePresets;
import com.themuffinman.app.workmarket.model.Quest;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
    private LocationLookupService locationLookupService;

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
        VisionIntentRouter visionIntentRouter = new VisionIntentRouter(visionProperties);
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
                visionCreateQuestExecutionAdapter,
                visionPromptUnderstandingService,
                visionSemanticMapper,
                visionProperties
        );

        lenient().when(visionPromptUnderstandingService.understandPrompt(any(), any()))
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

        assertEquals("scheduled_at", fixedModeResponse.getRequestedSlot());

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
                "scheduled_at",
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
                "scheduled_at",
                VisionSlotStatePresets.createQuestFixedSchedule()
        );

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
        assertEquals("scheduled_at", response.getRequestedSlot());
        assertEquals("I could not read the time yet. Use a format like 2026-07-03 14:30, 03.07.2026 14:30, or tomorrow at 14:30.", response.getMessage());
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
        when(visionCreateQuestExecutionAdapter.execute(conversation.getSlotData(), currentUser)).thenReturn(createdQuest);

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
    void loadConversationReturnsLatestStateAndRecentSummaries() {
        VisionConversation conversation = createQuestConversation(99L, "reward_amount");
        conversation.setLastAssistantMessage("What is the reward amount, or should this quest be free?");
        conversation.setUpdatedAt(java.time.Instant.parse("2026-06-30T18:00:00Z"));
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
        return VisionPromptUnderstandingResult.builder()
                .sourceLanguage("en")
                .originalPrompt(normalizedPrompt)
                .normalizedPrompt(normalizedPrompt)
                .translationProvider("mock")
                .translationApplied(false)
                .translationReliable(true)
                .slots(extractedSlotsFor(normalizedPrompt, conversation))
                .build();
    }

    private VisionPromptUnderstandingSlots extractedSlotsFor(String prompt, VisionConversation conversation) {
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
                            .scheduledAt("2026-07-03T14:30:00Z")
                            .scheduledAtConfidence(1.0)
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
