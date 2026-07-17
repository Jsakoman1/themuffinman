package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VisionMarkChatReadExecutionAdapterTest {

    @Test
    void marksTheOpenedChatThroughTheBackendService() {
        ChatService chatService = mock(ChatService.class);
        VisionMarkChatReadExecutionAdapter adapter = new VisionMarkChatReadExecutionAdapter(chatService);
        AppUser owner = new AppUser();
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(owner);
        conversation.setSlotData(new LinkedHashMap<>(java.util.Map.of("opened_chat_conversation_id", "42")));

        VisionExecutionResult result = adapter.execute(conversation);

        assertTrue(result.isExecuted());
        assertTrue("mark_chat_read".equals(result.getCapabilityId()));
        verify(chatService).markConversationRead(42L, null, owner);
    }

    @Test
    void blocksWhenThereIsNoOpenedChatContext() {
        ChatService chatService = mock(ChatService.class);
        VisionMarkChatReadExecutionAdapter adapter = new VisionMarkChatReadExecutionAdapter(chatService);
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(new AppUser());
        conversation.setSlotData(new LinkedHashMap<>());

        VisionExecutionResult result = adapter.execute(conversation);

        assertFalse(result.isExecuted());
    }
}
