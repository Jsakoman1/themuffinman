package com.themuffinman.app.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.chat.dto.ChatMarkReadRequestDTO;
import com.themuffinman.app.chat.dto.ChatReceiptRequestDTO;
import com.themuffinman.app.chat.dto.ChatSocketClientMessageDTO;
import com.themuffinman.app.chat.model.ChatAuditEventType;
import com.themuffinman.app.chat.service.ChatAuditService;
import com.themuffinman.app.chat.service.ChatRealtimeService;
import com.themuffinman.app.chat.service.ChatService;
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
    private final ChatService chatService;
    private final ChatAuditService chatAuditService;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        AppUser currentUser = currentUser(session);
        if (currentUser == null) {
            return;
        }
        chatRealtimeService.register(currentUser, session);
        chatAuditService.record(ChatAuditEventType.WEBSOCKET_CONNECTED, currentUser, null, session.getId(), remoteAddress(session), userAgent(session), null);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        AppUser currentUser = currentUser(session);
        if (currentUser == null) {
            return;
        }

        ChatSocketClientMessageDTO payload;
        try {
            payload = objectMapper.readValue(message.getPayload(), ChatSocketClientMessageDTO.class);
        } catch (Exception exception) {
            recordInvalidPayload(currentUser, session, message.getPayload(), "parse_error");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        if ("chat.presence.ping".equals(payload.getType())) {
            chatRealtimeService.refreshPresence(currentUser);
            chatAuditService.record(ChatAuditEventType.WEBSOCKET_PING, currentUser, null, session.getId(), remoteAddress(session), userAgent(session), null);
            return;
        }
        if ("chat.typing".equals(payload.getType()) && payload.getConversationId() != null && payload.getTyping() != null) {
            chatService.updateTypingState(payload.getConversationId(), payload.getTyping(), currentUser);
            chatAuditService.record(
                    ChatAuditEventType.WEBSOCKET_TYPING_UPDATED,
                    currentUser,
                    null,
                    session.getId(),
                    remoteAddress(session),
                    userAgent(session),
                    java.util.Map.of("conversationId", payload.getConversationId(), "typing", payload.getTyping())
            );
            return;
        }
        if ("chat.receipts.delivered".equals(payload.getType()) && payload.getConversationId() != null) {
            chatService.markConversationDelivered(
                    payload.getConversationId(),
                    ChatReceiptRequestDTO.builder().upToMessageId(payload.getUpToMessageId()).build(),
                    currentUser
            );
            return;
        }
        if ("chat.receipts.seen".equals(payload.getType()) && payload.getConversationId() != null) {
            chatService.markConversationRead(
                    payload.getConversationId(),
                    ChatMarkReadRequestDTO.builder().upToMessageId(payload.getUpToMessageId()).build(),
                    currentUser
            );
            return;
        }

        recordInvalidPayload(currentUser, session, message.getPayload(), "unsupported_type");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        AppUser currentUser = currentUser(session);
        if (currentUser == null) {
            return;
        }
        chatRealtimeService.unregister(currentUser, session);
        chatAuditService.record(
                ChatAuditEventType.WEBSOCKET_DISCONNECTED,
                currentUser,
                null,
                session.getId(),
                remoteAddress(session),
                userAgent(session),
                java.util.Map.of("status", status.getCode(), "reason", status.getReason() == null ? "" : status.getReason())
        );
    }

    private AppUser currentUser(WebSocketSession session) {
        Object value = session.getAttributes().get(ChatWebSocketAuthInterceptor.CHAT_SOCKET_USER_ATTRIBUTE);
        return value instanceof AppUser user ? user : null;
    }

    private void recordInvalidPayload(AppUser currentUser, WebSocketSession session, String payload, String reason) {
        chatAuditService.record(
                ChatAuditEventType.WEBSOCKET_INVALID_PAYLOAD,
                currentUser,
                null,
                session.getId(),
                remoteAddress(session),
                userAgent(session),
                java.util.Map.of("reason", reason, "payload", payload == null ? "" : payload)
        );
    }

    private String remoteAddress(WebSocketSession session) {
        Object value = session.getAttributes().get(ChatWebSocketAuthInterceptor.CHAT_SOCKET_REMOTE_ADDRESS_ATTRIBUTE);
        return value instanceof String stringValue ? stringValue : null;
    }

    private String userAgent(WebSocketSession session) {
        Object value = session.getAttributes().get(ChatWebSocketAuthInterceptor.CHAT_SOCKET_USER_AGENT_ATTRIBUTE);
        return value instanceof String stringValue ? stringValue : null;
    }
}
