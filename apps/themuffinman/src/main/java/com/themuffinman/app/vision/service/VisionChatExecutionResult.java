package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
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
public class VisionChatExecutionResult {
    private boolean executed;
    private String blockingReason;
    private ChatConversationSummaryDTO conversation;

    public static VisionChatExecutionResult executed(ChatConversationSummaryDTO conversation) {
        return VisionChatExecutionResult.builder()
                .executed(true)
                .conversation(conversation)
                .build();
    }

    public static VisionChatExecutionResult blocked(String blockingReason) {
        return VisionChatExecutionResult.builder()
                .executed(false)
                .blockingReason(blockingReason)
                .build();
    }
}
