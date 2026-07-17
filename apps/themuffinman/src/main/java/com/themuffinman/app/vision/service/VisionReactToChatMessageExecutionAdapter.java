package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.dto.ChatMessageReactionRequestDTO;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionReactToChatMessageExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final ChatService chatService;
    public VisionReactToChatMessageExecutionAdapter(ChatService chatService) { this.chatService = chatService; }
    @Override public String capabilityId() { return "react_to_chat_message"; }
    @Override public VisionExecutionResult execute(VisionConversation conversation) {
        try {
            Long conversationId = Long.valueOf(conversation.getSlotData().get("opened_chat_conversation_id"));
            Long messageId = Long.valueOf(conversation.getSlotData().get("message_id"));
            String emoji = conversation.getSlotData().get("reaction_emoji");
            if (emoji == null || emoji.isBlank()) return VisionExecutionResult.blocked("Reaction emoji is required.");
            chatService.addReaction(conversationId, messageId, ChatMessageReactionRequestDTO.builder().emoji(emoji).build(), conversation.getOwner());
            return VisionExecutionResult.executedAction(capabilityId());
        } catch (RuntimeException exception) { return VisionExecutionResult.blocked("The chat reaction could not be added."); }
    }
}
