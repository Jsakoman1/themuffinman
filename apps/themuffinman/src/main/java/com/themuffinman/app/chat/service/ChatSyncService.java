package com.themuffinman.app.chat.service;

import com.themuffinman.app.chat.dto.ChatRefreshHintDTO;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatSyncService {
    private final ChatService chatService;

    public ChatRefreshHintDTO getRefreshHint(Long conversationId, Long knownLatestMessageId, AppUser currentUser) {
        if (conversationId == null || conversationId <= 0) throw new IllegalArgumentException("Conversation id must be positive");
        var sync = chatService.getConversationSync(conversationId, knownLatestMessageId, 1, currentUser);
        Long latestMessageId = sync.getLatestMessageId();
        boolean refreshRequired = latestMessageId != null && !latestMessageId.equals(knownLatestMessageId);
        return ChatRefreshHintDTO.builder()
                .conversationId(conversationId)
                .latestMessageId(latestMessageId)
                .refreshRequired(refreshRequired)
                .reason(refreshRequired ? "server_sync_required" : "server_sync_current")
                .build();
    }
}
