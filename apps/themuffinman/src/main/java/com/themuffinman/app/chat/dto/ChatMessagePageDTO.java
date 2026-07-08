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
public class ChatMessagePageDTO {
    private List<ChatMessageDTO> messages;
    private int limit;
    private boolean hasMore;
    private Long nextBeforeMessageId;
}
