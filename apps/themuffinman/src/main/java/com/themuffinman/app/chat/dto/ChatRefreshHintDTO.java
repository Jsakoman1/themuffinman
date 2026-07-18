package com.themuffinman.app.chat.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChatRefreshHintDTO {
    Long conversationId;
    Long latestMessageId;
    boolean refreshRequired;
    String reason;
}
