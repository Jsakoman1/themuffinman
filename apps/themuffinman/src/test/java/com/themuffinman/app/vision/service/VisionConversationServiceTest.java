package com.themuffinman.app.vision.service;

import com.themuffinman.app.agent.dto.AdminAgentPlaygroundResponseDTO;
import com.themuffinman.app.agent.service.AdminAgentPlaygroundService;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisionConversationServiceTest {

    @Mock
    private VisionConversationRepository visionConversationRepository;

    @Mock
    private VisionTurnRepository visionTurnRepository;

    @Mock
    private AdminAgentPlaygroundService adminAgentPlaygroundService;

    @Mock
    private VisionCreateQuestExecutionAdapter visionCreateQuestExecutionAdapter;

    @Mock
    private LocationLookupService locationLookupService;

    private VisionConversationService visionConversationService;
    private AtomicLong conversationIds;
    private AtomicLong turnIds;
    private AppUser currentUser;
    private VisionProperties visionProperties;

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
        VisionSlotService visionSlotService = new VisionSlotService(visionScheduleParserService, visionLocationResolutionService);
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
            return conversation;
        });

        lenient().when(visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(currentUser)).thenReturn(List.of());

        lenient().when(visionTurnRepository.save(any(VisionTurn.class))).thenAnswer(invocation -> {
            VisionTurn turn = invocation.getArgument(0);
            if (turn.getId() == null) {
                turn.setId(turnIds.incrementAndGet());
            }
            return turn;
        });

        visionConversationService = new VisionConversationService(
                visionConversationRepository,
                visionTurnRepository,
                visionIntentRouter,
                visionSlotService,
                visionClarificationService,
                visionCanvasAssembler,
                visionCreateQuestExecutionAdapter,
                adminAgentPlaygroundService,
                visionProperties
        );
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
    void startsCreateQuestConversationAndAsksForMissingSlot() {
        when(adminAgentPlaygroundService.analyzePrompt("Create a quest"))
                .thenReturn(agentResponse("Create a quest"));
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

        when(adminAgentPlaygroundService.analyzePrompt("Help move a sofa"))
                .thenReturn(agentResponse("Help move a sofa"));
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

        when(adminAgentPlaygroundService.analyzePrompt("I need someone to help carry a sofa to a third-floor flat"))
                .thenReturn(agentResponse("I need someone to help carry a sofa to a third-floor flat"));
        VisionConversationTurnResponseDTO descriptionResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(42L)
                        .prompt("I need someone to help carry a sofa to a third-floor flat")
                        .build(),
                currentUser
        );

        assertEquals("reward_amount", descriptionResponse.getRequestedSlot());

        when(adminAgentPlaygroundService.analyzePrompt("20 euros"))
                .thenReturn(agentResponse("20 euros"));
        VisionConversationTurnResponseDTO rewardResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(42L)
                        .prompt("20 euros")
                        .build(),
                currentUser
        );

        assertEquals("visibility", rewardResponse.getRequestedSlot());

        when(adminAgentPlaygroundService.analyzePrompt("public"))
                .thenReturn(agentResponse("public"));
        VisionConversationTurnResponseDTO visibilityResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(42L)
                        .prompt("public")
                        .build(),
                currentUser
        );

        assertEquals("schedule_mode", visibilityResponse.getRequestedSlot());

        when(adminAgentPlaygroundService.analyzePrompt("by agreement"))
                .thenReturn(agentResponse("by agreement"));
        VisionConversationTurnResponseDTO scheduleResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(42L)
                        .prompt("by agreement")
                        .build(),
                currentUser
        );

        assertEquals("location_mode", scheduleResponse.getRequestedSlot());

        when(adminAgentPlaygroundService.analyzePrompt("use profile location"))
                .thenReturn(agentResponse("use profile location"));
        VisionConversationTurnResponseDTO reviewResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(42L)
                        .prompt("use profile location")
                        .build(),
                currentUser
        );

        assertEquals("SHOW_REVIEW", reviewResponse.getNextAction());
        assertNotNull(reviewResponse.getReview());
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

        when(adminAgentPlaygroundService.analyzePrompt("fixed time"))
                .thenReturn(agentResponse("fixed time"));
        VisionConversationTurnResponseDTO fixedModeResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(52L)
                        .prompt("fixed time")
                        .build(),
                currentUser
        );

        assertEquals("scheduled_at", fixedModeResponse.getRequestedSlot());

        when(adminAgentPlaygroundService.analyzePrompt(VisionSchedulePhrasePresets.ISO_DATE_TIME))
                .thenReturn(agentResponse(VisionSchedulePhrasePresets.ISO_DATE_TIME));
        VisionConversationTurnResponseDTO fixedTimeResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(52L)
                        .prompt(VisionSchedulePhrasePresets.ISO_DATE_TIME)
                        .build(),
                currentUser
        );

        assertEquals("location_mode", fixedTimeResponse.getRequestedSlot());

        when(adminAgentPlaygroundService.analyzePrompt("custom place"))
                .thenReturn(agentResponse("custom place"));
        VisionConversationTurnResponseDTO customLocationResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(52L)
                        .prompt("custom place")
                        .build(),
                currentUser
        );

        assertEquals("location_label", customLocationResponse.getRequestedSlot());

        when(adminAgentPlaygroundService.analyzePrompt(VisionLocationCandidatePresets.BAN_JELACIC))
                .thenReturn(agentResponse(VisionLocationCandidatePresets.BAN_JELACIC));
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
        when(adminAgentPlaygroundService.analyzePrompt(VisionLocationCandidatePresets.ILICA_RESOLVED))
                .thenReturn(agentResponse(VisionLocationCandidatePresets.ILICA_RESOLVED));

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
        when(adminAgentPlaygroundService.analyzePrompt(VisionLocationCandidatePresets.BAN_JELACIC))
                .thenReturn(agentResponse(VisionLocationCandidatePresets.BAN_JELACIC));

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
        when(adminAgentPlaygroundService.analyzePrompt(VisionLocationCandidatePresets.ILICA_TYPED))
                .thenReturn(agentResponse(VisionLocationCandidatePresets.ILICA_TYPED));
        when(adminAgentPlaygroundService.analyzePrompt("yes"))
                .thenReturn(agentResponse("yes"));

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
        when(adminAgentPlaygroundService.analyzePrompt(VisionLocationCandidatePresets.ILICA_TYPED))
                .thenReturn(agentResponse(VisionLocationCandidatePresets.ILICA_TYPED));
        when(adminAgentPlaygroundService.analyzePrompt("candidate 2"))
                .thenReturn(agentResponse("candidate 2"));

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
        when(adminAgentPlaygroundService.analyzePrompt("maybe"))
                .thenReturn(agentResponse("maybe"));

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
        when(adminAgentPlaygroundService.analyzePrompt(VisionLocationCandidatePresets.ILICA_TYPED))
                .thenReturn(agentResponse(VisionLocationCandidatePresets.ILICA_TYPED));
        when(adminAgentPlaygroundService.analyzePrompt("keep typed location"))
                .thenReturn(agentResponse("keep typed location"));

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
        when(adminAgentPlaygroundService.analyzePrompt(VisionSchedulePhrasePresets.TOMORROW_2_PM))
                .thenReturn(agentResponse(VisionSchedulePhrasePresets.TOMORROW_2_PM));

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

        when(adminAgentPlaygroundService.analyzePrompt("35 euros"))
                .thenReturn(agentResponse("35 euros"));
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
        when(adminAgentPlaygroundService.analyzePrompt("change schedule"))
                .thenReturn(agentResponse("change schedule"));

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
        when(adminAgentPlaygroundService.analyzePrompt("sometime later"))
                .thenReturn(agentResponse("sometime later"));

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
        when(adminAgentPlaygroundService.analyzePrompt("custom place"))
                .thenReturn(agentResponse("custom place"));

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
        when(adminAgentPlaygroundService.analyzePrompt("What is the weather today?"))
                .thenReturn(agentResponse("What is the weather today?"));
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

    private AdminAgentPlaygroundResponseDTO agentResponse(String translatedPrompt) {
        return AdminAgentPlaygroundResponseDTO.builder()
                .translatedPrompt(translatedPrompt)
                .promptTranslationApplied(false)
                .promptTranslationReliable(true)
                .build();
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
