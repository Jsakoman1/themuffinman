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
public class ChatMessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderUserId;
    private String senderUsername;
    @Nullable
    private String senderAvatarDataUrl;
    @Nullable
    private String messageBody;
    @Nullable
    private String imageDataUrl;
    private String createdAt;
    @Nullable
    private String readAt;
    private boolean ownMessage;
}
