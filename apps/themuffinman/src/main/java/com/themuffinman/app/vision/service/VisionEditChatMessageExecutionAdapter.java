package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.dto.ChatMessageUpdateRequestDTO;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionEditChatMessageExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final ChatService chatService;
    public VisionEditChatMessageExecutionAdapter(ChatService chatService) { this.chatService = chatService; }
    @Override public String capabilityId() { return "edit_chat_message"; }
    @Override public VisionExecutionResult execute(VisionConversation conversation) {
        try {
            Long conversationId = Long.valueOf(conversation.getSlotData().get("opened_chat_conversation_id"));
            Long messageId = Long.valueOf(conversation.getSlotData().get("message_id"));
            String body = conversation.getSlotData().get("message_text");
            if (body == null || body.isBlank()) return VisionExecutionResult.blocked("Updated chat message is required.");
            chatService.updateMessage(conversationId, messageId, ChatMessageUpdateRequestDTO.builder().messageBody(body).build(), conversation.getOwner());
            return VisionExecutionResult.executedAction(capabilityId());
        } catch (RuntimeException exception) { return VisionExecutionResult.blocked("The chat message could not be edited."); }
    }
}
