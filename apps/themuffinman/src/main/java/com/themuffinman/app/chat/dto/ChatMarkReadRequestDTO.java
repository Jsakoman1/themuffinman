package com.themuffinman.app.chat.dto;

import com.themuffinman.app.common.contract.ContractOptional;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMarkReadRequestDTO {
    @ContractOptional
    @Positive(message = "Read marker message id is invalid")
    private Long upToMessageId;
}
