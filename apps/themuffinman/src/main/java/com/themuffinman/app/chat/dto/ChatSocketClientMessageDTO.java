package com.themuffinman.app.chat.dto;

import com.themuffinman.app.common.contract.ContractOptional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSocketClientMessageDTO {
    private String type;
    @ContractOptional
    private Long conversationId;
}
