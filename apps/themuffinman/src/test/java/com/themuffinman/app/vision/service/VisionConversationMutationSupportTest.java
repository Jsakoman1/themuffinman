package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.model.VisionAgentState;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionNextAction;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.vision.repository.VisionTurnRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class VisionConversationMutationSupportTest {

    @Test
    void ensureTurnCapacityRejectsWhenConversationIsFull() {
        VisionTurnRepository turnRepository = Mockito.mock(VisionTurnRepository.class);
        VisionProperties properties = new VisionProperties();
        properties.setMaxTurnsPerConversation(2);
        when(turnRepository.countByConversation(any())).thenReturn(2L);

        VisionConversationMutationSupport support = new VisionConversationMutationSupport(
                Mockito.mock(VisionConversationRepository.class),
                turnRepository,
                Mockito.mock(VisionSemanticOrchestrationContextService.class),
                properties
        );

        assertThrows(RuntimeException.class, () -> support.ensureTurnCapacity(new VisionConversation()));
    }

    @Test
    void createTurnPopulatesTheSavedTurn() {
        VisionTurnRepository turnRepository = Mockito.mock(VisionTurnRepository.class);
        VisionConversation conversation = new VisionConversation();
        conversation.setIntent(VisionIntent.CREATE_QUEST);
        conversation.setStatus(com.themuffinman.app.vision.model.VisionConversationStatus.ACTIVE);
        when(turnRepository.countByConversation(conversation)).thenReturn(3L);
        when(turnRepository.save(any(VisionTurn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VisionConversationMutationSupport support = new VisionConversationMutationSupport(
                Mockito.mock(VisionConversationRepository.class),
                turnRepository,
                Mockito.mock(VisionSemanticOrchestrationContextService.class),
                new VisionProperties()
        );

        VisionTurn turn = support.createTurn(
                conversation,
                "system",
                "hello",
                "hello",
                VisionIntent.CREATE_QUEST,
                VisionAgentState.ASKING,
                VisionNextAction.ASK_FOR_SLOT,
                "quest_title",
                false,
                true,
                "Need a title"
        );

        assertEquals(4, turn.getTurnIndex());
        assertEquals("hello", turn.getPrompt());
        assertEquals("quest_title", turn.getRequestedSlot());
        assertEquals(VisionAgentState.ASKING, turn.getAgentState());
        assertEquals(VisionNextAction.ASK_FOR_SLOT, turn.getNextAction());
        assertTrue(turnRepository.countByConversation(conversation) >= 0);
    }
}
