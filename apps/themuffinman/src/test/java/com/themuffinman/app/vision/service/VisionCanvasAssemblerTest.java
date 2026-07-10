package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionConversationSummaryDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.dto.VisionRuntimeContextDTO;
import com.themuffinman.app.vision.model.VisionAgentState;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionNextAction;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.model.VisionTurnSource;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionCanvasAssemblerTest {

    @Test
    void buildsRuntimeContextFromTurnState() {
        VisionCanvasAssembler assembler = new VisionCanvasAssembler(new VisionProperties());

        AppUser currentUser = new AppUser();
        currentUser.setId(11L);

        VisionConversation conversation = new VisionConversation();
        conversation.setId(42L);
        conversation.setOwner(currentUser);
        conversation.setIntent(VisionIntent.OPEN_CHAT);
        conversation.setStatus(com.themuffinman.app.vision.model.VisionConversationStatus.ACTIVE);
        conversation.setSlotData(Map.of("client_device_role", "mobile"));

        VisionTurn turn = new VisionTurn();
        turn.setId(7L);
        turn.setConversation(conversation);
        turn.setTurnIndex(1);
        turn.setSource(VisionTurnSource.TEXT);
        turn.setPrompt("create quest");
        turn.setNormalizedPrompt("create quest");
        turn.setDetectedIntent(VisionIntent.OPEN_CHAT);
        turn.setAgentState(VisionAgentState.ASKING);
        turn.setNextAction(VisionNextAction.ASK_FOR_SLOT);
        turn.setRequestedSlot("target_user");
        turn.setTranslationApplied(false);
        turn.setTranslationReliable(true);
        turn.setAssistantMessage("Describe the quest");
        turn.setAppliedSlotIds(List.of());

        VisionConversationTurnResponseDTO response = assembler.assemble(
                conversation,
                turn,
                List.<VisionConversationSummaryDTO>of(),
                null,
                null,
                null,
                null,
                null
        );

        VisionRuntimeContextDTO runtimeContext = response.getRuntimeContext();

        assertNotNull(runtimeContext);
        assertEquals("text", runtimeContext.getInputType());
        assertEquals(com.themuffinman.app.vision.dto.VisionDeviceRoleDTO.MOBILE, runtimeContext.getDeviceRole());
        assertEquals(com.themuffinman.app.vision.dto.VisionAttentionStateDTO.FOCUSED, runtimeContext.getAttentionState());
        assertEquals("vision:conversation:42:turn:7", runtimeContext.getSessionAnchor());
        assertTrue(runtimeContext.getActionHints().contains("Fill Person"));
        assertNotNull(runtimeContext.getAudioCue());
        assertNotNull(runtimeContext.getHapticCue());
        assertEquals("prompt", runtimeContext.getAudioCue().getType());
        assertTrue(runtimeContext.isConsentRequired());
        assertEquals("This turn can contact another person.", runtimeContext.getConsentReason());
        assertTrue(runtimeContext.isResumeAvailable());
        assertEquals("Resume by filling Person.", runtimeContext.getResumeHint());
    }
}
