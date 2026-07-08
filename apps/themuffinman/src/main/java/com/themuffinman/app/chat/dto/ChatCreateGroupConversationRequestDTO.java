package com.themuffinman.app.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCreateGroupConversationRequestDTO {
    @NotBlank
    private String title;

    @NotEmpty
    private List<Long> participantUserIds;
}
