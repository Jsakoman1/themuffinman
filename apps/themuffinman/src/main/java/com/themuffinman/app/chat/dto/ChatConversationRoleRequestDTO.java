package com.themuffinman.app.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationRoleRequestDTO {

    @NotBlank(message = "Participant role is required")
    private String role;
}
