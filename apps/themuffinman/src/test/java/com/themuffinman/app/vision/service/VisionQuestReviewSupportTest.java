package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.vision.repository.VisionTurnRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class VisionQuestReviewSupportTest {

    @Test
    void confirmReviewDelegatesToExecutionFlowWhenPromptConfirms() {
        VisionExecutionService executionService = Mockito.mock(VisionExecutionService.class);
        VisionSurfacePolicy surfacePolicy = Mockito.mock(VisionSurfacePolicy.class);
        VisionSlotService slotService = Mockito.mock(VisionSlotService.class);
        VisionClarificationService clarificationService = Mockito.mock(VisionClarificationService.class);
        VisionSemanticMapper semanticMapper = Mockito.mock(VisionSemanticMapper.class);
        VisionConversationRepository conversationRepository = Mockito.mock(VisionConversationRepository.class);
        VisionTurnRepository turnRepository = Mockito.mock(VisionTurnRepository.class);
        VisionConversationMutationSupport mutationSupport = new VisionConversationMutationSupport(
                conversationRepository,
                turnRepository,
                Mockito.mock(VisionSemanticOrchestrationContextService.class),
                new com.themuffinman.app.config.VisionProperties()
        );

        VisionConversation conversation = new VisionConversation();
        conversation.setIntent(VisionIntent.CREATE_QUEST);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        conversation.setSlotData(new LinkedHashMap<>());
        conversation.setRequestedSlot("quest_title");

        when(executionService.execute(conversation)).thenReturn(VisionExecutionResult.blocked("blocked"));
        when(surfacePolicy.canExecuteCapability("create_quest")).thenReturn(true);
        when(turnRepository.countByConversation(conversation)).thenReturn(1L);
        when(turnRepository.save(any(com.themuffinman.app.vision.model.VisionTurn.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(conversationRepository.save(any(VisionConversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VisionQuestReviewSupport support = new VisionQuestReviewSupport(
                executionService,
                surfacePolicy,
                slotService,
                clarificationService,
                semanticMapper,
                conversationRepository,
                mutationSupport
        );

        var turn = support.handleCreateQuestReviewTurn(
                conversation,
                "confirm",
                "confirm",
                VisionPromptUnderstandingResult.empty(""),
                "system"
        );

        assertEquals(VisionConversationStatus.REVIEW_READY, conversation.getStatus());
        assertNull(conversation.getRequestedSlot());
        assertEquals(com.themuffinman.app.vision.model.VisionTurn.class, turn.getClass());
    }
}
