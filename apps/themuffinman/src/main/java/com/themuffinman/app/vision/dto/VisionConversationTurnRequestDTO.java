package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionConversationTurnRequestDTO {
    private Long conversationId;
    private String prompt;
    private String source;
    private String action;
    private String reviewTarget;
}
