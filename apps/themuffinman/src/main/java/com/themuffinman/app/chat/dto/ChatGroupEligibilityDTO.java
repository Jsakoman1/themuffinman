package com.themuffinman.app.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGroupEligibilityDTO {
    private boolean eligible;
    private String reasonCode;
    private String message;
    private List<Long> selectedParticipantUserIds;
    private int minimumParticipantCount;
    private List<String> allowedActions;
}
