package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.dto.ChatMessageRequestDTO;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionReplyToChatMessageExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final ChatService chatService;
    public VisionReplyToChatMessageExecutionAdapter(ChatService chatService) { this.chatService = chatService; }
    @Override public String capabilityId() { return "reply_to_chat_message"; }
    @Override public VisionExecutionResult execute(VisionConversation conversation) {
        try {
            Long conversationId = Long.valueOf(conversation.getSlotData().get("opened_chat_conversation_id"));
            Long messageId = Long.valueOf(conversation.getSlotData().get("message_id"));
            String body = conversation.getSlotData().get("message_text");
            if (body == null || body.isBlank()) return VisionExecutionResult.blocked("Reply text is required.");
            chatService.sendMessage(conversationId, ChatMessageRequestDTO.builder().messageBody(body).replyToMessageId(messageId).build(), conversation.getOwner());
            return VisionExecutionResult.executedAction(capabilityId());
        } catch (RuntimeException exception) { return VisionExecutionResult.blocked("The chat reply could not be sent."); }
    }
}
