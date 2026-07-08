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
public class ChatMessageUpdateRequestDTO {

    @NotBlank(message = "Updated chat message is required")
    @Size(max = 2000, message = "Chat message must be 2000 characters or less")
    private String messageBody;
}
