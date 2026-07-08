package com.themuffinman.app.chat.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAuditEventListDTO {
    private List<ChatAuditEventDTO> events;
    private int limit;
    @Nullable
    private String eventType;
    @Nullable
    private Long userId;
    @Nullable
    private Long conversationId;
}
