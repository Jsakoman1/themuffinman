package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionConversationSummaryDTO;
import com.themuffinman.app.vision.dto.VisionMemoryTrailDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.vision.repository.VisionTurnRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisionConversationReadModelAssemblerTest {

    @Mock
    private VisionConversationRepository visionConversationRepository;

    @Mock
    private VisionTurnRepository visionTurnRepository;

    @Mock
    private VisionSemanticOrchestrationContextService visionSemanticOrchestrationContextService;

    @Mock
    private VisionClarificationService visionClarificationService;

    @Test
    void buildsRecentSummariesAndMemoryTrail() {
        VisionConversationReadModelAssembler assembler = new VisionConversationReadModelAssembler(
                visionConversationRepository,
                visionTurnRepository,
                visionSemanticOrchestrationContextService,
                visionClarificationService
        );

        AppUser currentUser = new AppUser();
        currentUser.setId(7L);

        VisionConversation conversation = new VisionConversation();
        conversation.setId(10L);
        conversation.setOwner(currentUser);
        conversation.setIntent(VisionIntent.VIEW_PROFILE);
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        conversation.setRequestedSlot("profile_description");
        conversation.setLastAssistantMessage("Describe your profile");
        conversation.setUpdatedAt(Instant.now().minusSeconds(3600));
        conversation.setSlotData(Map.of("profile_username", "Alex"));

        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setAppliedSlotIds(List.of("profile_username"));

        when(visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(currentUser)).thenReturn(List.of(conversation));
        when(visionTurnRepository.findTopByConversationOrderByTurnIndexDesc(conversation)).thenReturn(Optional.of(turn));
        when(visionClarificationService.buildQuestion("profile_description")).thenReturn("What description?");
        when(visionSemanticOrchestrationContextService.buildMemoryContext(currentUser, conversation)).thenReturn(
                VisionSemanticMemoryContext.builder()
                        .sessionMemory(VisionSemanticSessionMemoryContext.builder()
                                .conversationId(10L)
                                .currentIntent("VIEW_PROFILE")
                                .currentEntityFamily("profile")
                                .requestedSlot("profile_description")
                                .status("active")
                                .sessionSummary("session summary")
                                .lastUserPrompt("show profile")
                                .lastNormalizedPrompt("show profile")
                                .lastAssistantMessage("Describe your profile")
                                .sessionMemorySnapshot("{}")
                                .openQuestions(List.of("What description?"))
                                .recentActions(List.of("prompt"))
                                .build())
                        .userMemory(VisionSemanticUserMemoryContext.builder()
                                .recentEntityFamilies(List.of("applications", "profile"))
                                .recentIntentTypes(List.of("VIEW_PROFILE"))
                                .build())
                        .build()
        );

        List<VisionConversationSummaryDTO> summaries = assembler.recentConversationSummaries(currentUser);
        VisionConversationSummaryDTO summary = summaries.getFirst();
        VisionSlotSummaryDTO slotSummary = summary.getAppliedSlotSummaries().getFirst();
        VisionMemoryTrailDTO memoryTrail = assembler.buildMemoryTrail(currentUser, conversation);

        assertEquals("Alex", summary.getTitle());
        assertEquals("Username", slotSummary.getLabel());
        assertEquals("Alex", slotSummary.getValue());
        assertEquals("profile", memoryTrail.getActiveEntityFamily());
        assertEquals("applications", memoryTrail.getPreviousEntityFamily());
        assertEquals("Switched from applications to profile.", memoryTrail.getTopicSwitchHint());
        assertNull(summary.getTopicSwitchHint());
    }
}
