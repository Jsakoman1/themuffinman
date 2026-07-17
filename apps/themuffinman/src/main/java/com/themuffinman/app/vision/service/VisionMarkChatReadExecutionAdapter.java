package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.dto.ChatMarkReadRequestDTO;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionMarkChatReadExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "mark_chat_read";

    private final ChatService chatService;

    public VisionMarkChatReadExecutionAdapter(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public String capabilityId() {
        return CAPABILITY_ID;
    }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return VisionExecutionResult.blocked("Conversation owner is required to mark chat as read.");
        }
        String rawConversationId = conversation.getSlotData().get("opened_chat_conversation_id");
        if (rawConversationId == null || rawConversationId.isBlank()) {
            return VisionExecutionResult.blocked("Open a chat before marking it as read.");
        }
        final long chatConversationId;
        try {
            chatConversationId = Long.parseLong(rawConversationId);
        } catch (NumberFormatException exception) {
            return VisionExecutionResult.blocked("The opened chat reference is invalid.");
        }

        chatService.markConversationRead(chatConversationId, null, conversation.getOwner());
        return VisionExecutionResult.executedAction(CAPABILITY_ID);
    }
}
