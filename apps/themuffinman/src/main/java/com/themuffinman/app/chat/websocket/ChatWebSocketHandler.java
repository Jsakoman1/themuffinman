package com.themuffinman.app.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.chat.dto.ChatSocketClientMessageDTO;
import com.themuffinman.app.chat.service.ChatRealtimeService;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatRealtimeService chatRealtimeService;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        AppUser currentUser = currentUser(session);
        if (currentUser == null) {
            return;
        }
        chatRealtimeService.register(currentUser, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        AppUser currentUser = currentUser(session);
        if (currentUser == null) {
            return;
        }

        ChatSocketClientMessageDTO payload = objectMapper.readValue(message.getPayload(), ChatSocketClientMessageDTO.class);
        if ("chat.presence.ping".equals(payload.getType())) {
            chatRealtimeService.refreshPresence(currentUser);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        AppUser currentUser = currentUser(session);
        if (currentUser == null) {
            return;
        }
        chatRealtimeService.unregister(currentUser, session);
    }

    private AppUser currentUser(WebSocketSession session) {
        Object value = session.getAttributes().get(ChatWebSocketAuthInterceptor.CHAT_SOCKET_USER_ATTRIBUTE);
        return value instanceof AppUser user ? user : null;
    }
}
