package com.themuffinman.app.chat.dto;

import com.themuffinman.app.common.contract.ContractOptional;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSocketEventDTO {
    private String type;
    @ContractOptional
    @Nullable
    private Long conversationId;
    @ContractOptional
    @Nullable
    private Long actorUserId;
    @ContractOptional
    @Nullable
    private String reason;
    @ContractOptional
    @Nullable
    private ChatConversationSummaryDTO conversation;
    @ContractOptional
    @Nullable
    private ChatMessageDTO message;
    @ContractOptional
    @Nullable
    private Long readUpToMessageId;
    @ContractOptional
    @Nullable
    private Long deliveredUpToMessageId;
    @ContractOptional
    @Nullable
    private Long seenUpToMessageId;
    @ContractOptional
    @Nullable
    private Long unreadNewsCount;
    @ContractOptional
    @Nullable
    private Boolean typing;
}
