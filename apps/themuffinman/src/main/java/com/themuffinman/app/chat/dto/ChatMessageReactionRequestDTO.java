package com.themuffinman.app.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageReactionRequestDTO {
    @NotBlank(message = "Emoji is required")
    @Size(max = 32, message = "Emoji must be 32 characters or less")
    private String emoji;
}
