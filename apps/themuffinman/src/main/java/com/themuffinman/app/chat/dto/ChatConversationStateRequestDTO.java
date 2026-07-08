package com.themuffinman.app.chat.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationStateRequestDTO {

    @Nullable
    private Boolean archived;

    @Nullable
    private String mutedUntil;
}
