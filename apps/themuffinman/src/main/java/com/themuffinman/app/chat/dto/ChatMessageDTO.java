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
public class ChatMessageDTO {
    private Long id;
    private Long conversationId;
    private String conversationType;
    private Long senderUserId;
    private String senderUsername;
    @Nullable
    private String senderAvatarDataUrl;
    @Nullable
    private String messageBody;
    @Nullable
    private String imageDataUrl;
    @Nullable
    private String attachmentName;
    @Nullable
    private String attachmentMimeType;
    @Nullable
    private String attachmentStorageProvider;
    @Nullable
    private String attachmentStorageKey;
    @Nullable
    private String attachmentUrl;
    @Nullable
    private String attachmentUrlExpiresAt;
    @Nullable
    private Integer attachmentSizeBytes;
    @Nullable
    private Long replyToMessageId;
    @Nullable
    private String clientMessageId;
    private String createdAt;
    private String updatedAt;
    @Nullable
    private String editedAt;
    @Nullable
    private String deletedAt;
    @Nullable
    private String readAt;
    @Nullable
    private String deliveredAt;
    @Nullable
    private String seenAt;
    private List<ChatMessageReactionDTO> reactions;
    private boolean edited;
    private boolean deleted;
    private boolean ownMessage;
}
