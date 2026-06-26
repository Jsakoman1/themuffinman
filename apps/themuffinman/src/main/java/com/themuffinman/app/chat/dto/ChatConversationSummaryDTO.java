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
public class ChatConversationSummaryDTO {
    private Long conversationId;
    private Long otherUserId;
    private String otherUsername;
    @Nullable
    private String otherUserProfileDescription;
    @Nullable
    private String otherUserAvatarDataUrl;
    private boolean otherUserOnline;
    @Nullable
    private String otherUserLastActiveAt;
    @Nullable
    private String lastMessagePreview;
    @Nullable
    private String lastMessageAt;
    private boolean lastMessageFromCurrentUser;
    private boolean lastMessageHasImage;
    private long unreadCount;
}
