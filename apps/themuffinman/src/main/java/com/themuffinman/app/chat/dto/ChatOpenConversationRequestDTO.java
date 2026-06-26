package com.themuffinman.app.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatOpenConversationRequestDTO {
    @NotNull(message = "Other user is required")
    @Positive(message = "Other user is invalid")
    private Long otherUserId;
}
