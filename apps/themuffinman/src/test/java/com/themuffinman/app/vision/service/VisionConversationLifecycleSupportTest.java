package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionAgentState;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionNextAction;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionConversationLifecycleSupportTest {

    @Test
    void isCancelCommandRecognizesCommonVariants() {
        VisionConversationLifecycleSupport support = new VisionConversationLifecycleSupport(
                Mockito.mock(VisionConversationRepository.class),
                Mockito.mock(VisionSemanticOrchestrationContextService.class),
                null
        );

        assertTrue(support.isCancelCommand("cancel conversation"));
        assertTrue(support.isCancelCommand("Stop!"));
        assertTrue(support.isCancelCommand("poništi"));
        assertFalse(support.isCancelCommand("continue"));
    }

    @Test
    void snapshotTurnReflectsConversationState() {
        VisionConversationLifecycleSupport support = new VisionConversationLifecycleSupport(
                Mockito.mock(VisionConversationRepository.class),
                Mockito.mock(VisionSemanticOrchestrationContextService.class),
                null
        );
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(new AppUser());
        conversation.setIntent(VisionIntent.DISCOVER_QUESTS);
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        conversation.setLastUserPrompt("show me something");
        conversation.setLastNormalizedPrompt("show me something");
        conversation.setLastAssistantMessage(null);

        var turn = support.snapshotTurn(conversation);

        assertEquals(VisionAgentState.RECOMMENDING, turn.getAgentState());
        assertEquals(VisionNextAction.SHOW_RESULTS, turn.getNextAction());
        assertEquals("Conversation resumed.", turn.getAssistantMessage());
    }

    @Test
    void resetMessageSupportsBusinessViews() {
        assertEquals(
                "The current view was reset. Business.",
                VisionConversationSnapshotSupport.resetReadOnlySnapshotMessage(VisionIntent.VIEW_BUSINESS)
        );
    }
}
