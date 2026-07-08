package com.themuffinman.app.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationSyncDTO {
    private ChatConversationSummaryDTO conversation;
    private List<ChatMessageDTO> messages;
    private Long afterMessageId;
    private Long latestMessageId;
    private List<Long> activeTypingUserIds;
    private int typingTimeoutSeconds;
}
