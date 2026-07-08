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
public class ChatAdminConversationSupportViewDTO {
    private ChatConversationSummaryDTO conversation;
    private List<ChatMessageDTO> recentMessages;
    private List<ChatAuditEventDTO> recentAuditEvents;
    private int recentMessagesLimit;
    private int recentAuditLimit;
}
