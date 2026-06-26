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
public class ChatWorkspaceDTO {
    private List<ChatConversationSummaryDTO> conversations;
    private List<ChatContactDTO> contacts;
    private List<ChatCircleOptionDTO> circles;
    private long unreadConversationCount;
    private long onlineContactCount;
}
