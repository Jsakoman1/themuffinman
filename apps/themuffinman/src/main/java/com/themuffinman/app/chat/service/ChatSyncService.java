package com.themuffinman.app.chat.service;

import com.themuffinman.app.chat.dto.ChatRefreshHintDTO;
import com.themuffinman.app.common.errors.ServiceErrors;
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
        if (conversationId == null || conversationId <= 0) throw ServiceErrors.badRequest("Conversation id must be positive");
        if (knownLatestMessageId != null && knownLatestMessageId <= 0) throw ServiceErrors.badRequest("Known latest message id must be positive");
        if (knownLatestMessageId != null && knownLatestMessageId == Long.MAX_VALUE) throw ServiceErrors.badRequest("Known latest message id is out of range");
        var sync = chatService.getConversationSync(conversationId, knownLatestMessageId, 1, currentUser);
        Long latestMessageId = sync.getLatestMessageId();
        boolean refreshRequired = !java.util.Objects.equals(latestMessageId, knownLatestMessageId);
        return ChatRefreshHintDTO.builder()
                .conversationId(conversationId)
                .latestMessageId(latestMessageId)
                .refreshRequired(refreshRequired)
                .reason(refreshRequired ? "server_sync_required" : "server_sync_current")
                .build();
    }
}
