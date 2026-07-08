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
public class ChatConversationParticipantDTO {
    private Long userId;
    private String username;
    private String role;
    @Nullable
    private String avatarDataUrl;
    private boolean online;
    @Nullable
    private String lastActiveAt;
}
