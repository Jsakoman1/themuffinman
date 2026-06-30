package com.themuffinman.app.vision.service;

import com.themuffinman.app.agent.dto.AdminAgentPlaygroundResponseDTO;
import com.themuffinman.app.agent.service.AdminAgentPlaygroundService;
import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionConversationTurnRequestDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.vision.repository.VisionTurnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private VisionConversationService visionConversationService;
    private AtomicLong conversationIds;
    private AtomicLong turnIds;
    private AppUser currentUser;

    @BeforeEach
    void setUp() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter visionIntentRouter = new VisionIntentRouter(visionProperties);
        VisionSlotService visionSlotService = new VisionSlotService();
        VisionClarificationService visionClarificationService = new VisionClarificationService();
        VisionCanvasAssembler visionCanvasAssembler = new VisionCanvasAssembler(visionProperties);
        currentUser = new AppUser();
        currentUser.setId(7L);

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
    }

    @Test
    void continuesConversationStepByStepUntilReview() {
        VisionConversation conversation = new VisionConversation();
        conversation.setId(42L);
        conversation.setOwner(currentUser);
        conversation.setIntent(com.themuffinman.app.vision.model.VisionIntent.CREATE_QUEST);
        conversation.setRequestedSlot("quest_title");
        conversation.setSlotData(new LinkedHashMap<>());

        when(adminAgentPlaygroundService.analyzePrompt("Help move a sofa"))
                .thenReturn(agentResponse("Help move a sofa"));
        when(visionConversationRepository.findByIdAndOwner(42L, currentUser)).thenReturn(Optional.of(conversation));
        when(visionTurnRepository.countByConversation(conversation)).thenReturn(1L, 1L, 2L, 2L, 3L, 3L, 4L, 4L);

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
        VisionConversationTurnResponseDTO reviewResponse = visionConversationService.processTurn(
                VisionConversationTurnRequestDTO.builder()
                        .conversationId(42L)
                        .prompt("public")
                        .build(),
                currentUser
        );

        assertEquals("SHOW_REVIEW", reviewResponse.getNextAction());
        assertNotNull(reviewResponse.getReview());
        assertEquals("Help move a sofa", reviewResponse.getReview().getTitle());
        assertEquals("20", reviewResponse.getReview().getRewardLabel());
        assertEquals("PUBLIC", reviewResponse.getReview().getVisibility());
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

    private AdminAgentPlaygroundResponseDTO agentResponse(String translatedPrompt) {
        return AdminAgentPlaygroundResponseDTO.builder()
                .translatedPrompt(translatedPrompt)
                .promptTranslationApplied(false)
                .promptTranslationReliable(true)
                .build();
    }
}
