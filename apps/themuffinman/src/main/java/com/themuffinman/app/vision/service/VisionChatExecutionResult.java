package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.dto.ChatMessageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionChatExecutionResult {
    private boolean executed;
    private String blockingReason;
    private ChatConversationSummaryDTO conversation;
    private ChatMessageDTO message;
    @Builder.Default
    private List<VisionChatTargetCandidate> candidates = List.of();

    public static VisionChatExecutionResult executed(ChatConversationSummaryDTO conversation) {
        return VisionChatExecutionResult.builder()
                .executed(true)
                .conversation(conversation)
                .build();
    }

    public static VisionChatExecutionResult messageSent(ChatConversationSummaryDTO conversation, ChatMessageDTO message) {
        return VisionChatExecutionResult.builder().executed(true).conversation(conversation).message(message).build();
    }

    public static VisionChatExecutionResult blocked(String blockingReason) {
        return VisionChatExecutionResult.builder()
                .executed(false)
                .blockingReason(blockingReason)
                .build();
    }

    public static VisionChatExecutionResult blocked(String blockingReason, List<VisionChatTargetCandidate> candidates) {
        return VisionChatExecutionResult.builder()
                .executed(false)
                .blockingReason(blockingReason)
                .candidates(candidates == null ? List.of() : List.copyOf(candidates))
                .build();
    }
}
