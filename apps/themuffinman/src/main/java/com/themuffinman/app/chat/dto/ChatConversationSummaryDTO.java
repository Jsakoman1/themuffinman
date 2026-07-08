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
    private String conversationType;
    @Nullable
    private String contextType;
    @Nullable
    private Long contextId;
    @Nullable
    private String title;
    @Nullable
    private Long ownerUserId;
    private int participantCount;
    private boolean canManageParticipants;
    private java.util.List<ChatConversationParticipantDTO> participants;
    private Long otherUserId;
    private String otherUsername;
    private String resolutionKey;
    private String resolutionLabel;
    private boolean exactResolutionEligible;
    @Nullable
    private String otherUserProfileDescription;
    @Nullable
    private String otherUserAvatarDataUrl;
    private boolean otherUserOnline;
    @Nullable
    private String otherUserLastActiveAt;
    @Nullable
    private Long lastMessageId;
    @Nullable
    private String lastMessagePreview;
    @Nullable
    private String lastMessageAt;
    private boolean lastMessageFromCurrentUser;
    private boolean lastMessageHasImage;
    private boolean lastMessageDeleted;
    private boolean muted;
    @Nullable
    private String mutedUntil;
    private boolean archived;
    @Nullable
    private String archivedAt;
    @Nullable
    private Long lastDeliveredMessageId;
    @Nullable
    private Long lastSeenMessageId;
    private long unreadCount;
}
