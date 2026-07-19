package com.themuffinman.app.chat.service;

import com.themuffinman.app.chat.model.ChatConversation;
import com.themuffinman.app.config.ChatProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.repository.CircleRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatRealtimeServiceTest {

    @Mock
    private ChatPresenceService presenceService;
    @Mock
    private CircleRequestRepository circleRequestRepository;
    @Mock
    private ChatProperties chatProperties;
    @Mock
    private WebSocketSession session;

    @Test
    void registersSocketWithExplicitConnectedAndResyncContract() throws Exception {
        AppUser user = new AppUser();
        user.setId(7L);
        when(session.isOpen()).thenReturn(true);
        when(circleRequestRepository.findAcceptedByUserId(7L)).thenReturn(List.of());

        ChatRealtimeService service = new ChatRealtimeService(presenceService, circleRequestRepository, chatProperties);
        service.register(user, session);

        var payload = org.mockito.ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeastOnce()).sendMessage(payload.capture());
        assertTrue(payload.getAllValues().stream().anyMatch(message ->
                message.getPayload().contains("\"type\":\"chat.connection\"")
                        && message.getPayload().contains("\"connectionState\":\"CONNECTED\"")
                        && message.getPayload().contains("\"resyncRequired\":false")));
    }
}
