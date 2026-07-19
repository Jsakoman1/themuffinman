package com.themuffinman.app.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMembershipTransitionDTO {
    private String action;
    private String transitionState;
    private Long conversationId;
    private Long replacementOwnerUserId;
    private String message;
}
