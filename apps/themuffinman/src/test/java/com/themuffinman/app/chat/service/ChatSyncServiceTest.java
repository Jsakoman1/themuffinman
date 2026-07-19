package com.themuffinman.app.chat.service;

import com.themuffinman.app.chat.dto.ChatConversationSyncDTO;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.web.server.ResponseStatusException;

class ChatSyncServiceTest {
    @Test void rejectsInvalidConversationIdBeforeSync() { assertEquals(400, assertThrows(ResponseStatusException.class, () -> new ChatSyncService(mock(ChatService.class)).getRefreshHint(0L, null, new AppUser())).getStatusCode().value()); }

    @Test void rejectsInvalidKnownCursorBeforeSync() { assertEquals(400, assertThrows(ResponseStatusException.class, () -> new ChatSyncService(mock(ChatService.class)).getRefreshHint(9L, 0L, new AppUser())).getStatusCode().value()); }

    @Test void derivesRefreshNeedFromAuthorizedServerSyncRatherThanClientState() {
        var chatService = mock(ChatService.class); var user = new AppUser(); user.setId(4L);
        when(chatService.getConversationSync(9L, 4L, 1, user)).thenReturn(ChatConversationSyncDTO.builder().latestMessageId(5L).build());
        var hint = new ChatSyncService(chatService).getRefreshHint(9L, 4L, user);
        assertTrue(hint.isRefreshRequired()); assertEquals("server_sync_required", hint.getReason()); verify(chatService).getConversationSync(9L, 4L, 1, user);
    }

    @Test void detectsServerEmptyStateWhenClientStillHasMessages() {
        var chatService = mock(ChatService.class); var user = new AppUser(); user.setId(4L);
        when(chatService.getConversationSync(9L, 4L, 1, user)).thenReturn(ChatConversationSyncDTO.builder().latestMessageId(null).build());
        var hint = new ChatSyncService(chatService).getRefreshHint(9L, 4L, user);
        assertTrue(hint.isRefreshRequired());
    }
}
