package com.themuffinman.app.chat.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationListDTO {
    private List<ChatConversationSummaryDTO> conversations;
    private long filteredCount;
    private int limit;
    private int page;
    private boolean hasMore;
    private boolean includeArchived;
    @Nullable
    private String conversationType;
    @Nullable
    private String contextType;
    @Nullable
    private Long contextId;
    @Nullable
    private String query;
    @Nullable
    private String nextBeforeLastMessageAt;
    @Nullable
    private Long nextBeforeConversationId;
}
