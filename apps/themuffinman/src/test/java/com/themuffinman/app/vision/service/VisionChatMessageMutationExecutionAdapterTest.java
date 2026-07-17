package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VisionChatMessageMutationExecutionAdapterTest {
    @Test
    void editsOwnMessageThroughChatService() {
        ChatService service = mock(ChatService.class);
        VisionConversation conversation = conversation(Map.of("opened_chat_conversation_id", "7", "message_id", "42", "message_text", "updated"));
        assertTrue(new VisionEditChatMessageExecutionAdapter(service).execute(conversation).isExecuted());
        verify(service).updateMessage(eq(7L), eq(42L), any(), eq(conversation.getOwner()));
    }

    @Test
    void repliesThroughChatService() {
        ChatService service = mock(ChatService.class);
        VisionConversation conversation = conversation(Map.of("opened_chat_conversation_id", "7", "message_id", "42", "message_text", "thanks"));
        assertTrue(new VisionReplyToChatMessageExecutionAdapter(service).execute(conversation).isExecuted());
        verify(service).sendMessage(eq(7L), any(), eq(conversation.getOwner()));
    }

    @Test
    void reactsThroughChatService() {
        ChatService service = mock(ChatService.class);
        VisionConversation conversation = conversation(Map.of("opened_chat_conversation_id", "7", "message_id", "42", "reaction_emoji", "👍"));
        assertTrue(new VisionReactToChatMessageExecutionAdapter(service).execute(conversation).isExecuted());
        verify(service).addReaction(eq(7L), eq(42L), any(), eq(conversation.getOwner()));
    }

    private VisionConversation conversation(Map<String, String> slots) {
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(new AppUser());
        conversation.setSlotData(new LinkedHashMap<>(slots));
        return conversation;
    }
}
