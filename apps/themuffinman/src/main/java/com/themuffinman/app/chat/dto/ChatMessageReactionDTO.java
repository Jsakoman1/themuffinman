package com.themuffinman.app.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageReactionDTO {
    private Long id;
    private Long userId;
    private String username;
    private String emoji;
    private String createdAt;
    private boolean ownReaction;
}
