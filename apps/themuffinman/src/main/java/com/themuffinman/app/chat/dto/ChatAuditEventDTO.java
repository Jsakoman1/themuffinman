package com.themuffinman.app.chat.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAuditEventDTO {
    private Long id;
    private String eventType;
    @Nullable
    private Long userId;
    @Nullable
    private String username;
    @Nullable
    private Long conversationId;
    @Nullable
    private String conversationType;
    @Nullable
    private String sessionId;
    @Nullable
    private String remoteAddress;
    @Nullable
    private String userAgent;
    @Nullable
    private String detailsJson;
    private String createdAt;
}
