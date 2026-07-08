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
public class ChatConversationTitleRequestDTO {

    @NotBlank(message = "Conversation title is required")
    @Size(max = 160, message = "Conversation title must be 160 characters or less")
    private String title;
}
