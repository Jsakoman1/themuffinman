package com.themuffinman.app.chat.dto;

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
public class ChatConversationParticipantsRequestDTO {

    @NotEmpty(message = "Participant user ids are required")
    private List<Long> participantUserIds;
}
