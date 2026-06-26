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
}
