package com.themuffinman.app.chat.websocket;

import com.themuffinman.app.chat.model.ChatAuditEventType;
import com.themuffinman.app.chat.service.ChatAuditService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatWebSocketAuthInterceptor implements HandshakeInterceptor {

    public static final String CHAT_SOCKET_USER_ATTRIBUTE = "chatSocketUser";
    public static final String CHAT_SOCKET_REMOTE_ADDRESS_ATTRIBUTE = "chatSocketRemoteAddress";
    public static final String CHAT_SOCKET_USER_AGENT_ATTRIBUTE = "chatSocketUserAgent";
    private static final String SEC_WEBSOCKET_PROTOCOL_HEADER = "Sec-WebSocket-Protocol";

    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;
    private final ChatAuditService chatAuditService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String remoteAddress = servletRequest.getServletRequest().getRemoteAddr();
        String userAgent = request.getHeaders().getFirst(HttpHeaders.USER_AGENT);
        String token = resolveToken(request, servletRequest);
        if (token == null || token.isBlank()) {
            chatAuditService.record(ChatAuditEventType.WEBSOCKET_AUTH_FAILED, null, null, null, remoteAddress, userAgent, Map.of("reason", "missing_token"));
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        try {
            String email = jwtService.extractEmail(token);
            AppUser currentUser = appUserRepository.findByEmail(email).orElse(null);
            if (currentUser == null) {
                chatAuditService.record(ChatAuditEventType.WEBSOCKET_AUTH_FAILED, null, null, null, remoteAddress, userAgent, Map.of("reason", "unknown_user"));
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            attributes.put(CHAT_SOCKET_USER_ATTRIBUTE, currentUser);
            attributes.put(CHAT_SOCKET_REMOTE_ADDRESS_ATTRIBUTE, remoteAddress);
            attributes.put(CHAT_SOCKET_USER_AGENT_ATTRIBUTE, userAgent);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            chatAuditService.record(ChatAuditEventType.WEBSOCKET_AUTH_FAILED, null, null, null, remoteAddress, userAgent, Map.of("reason", "invalid_token"));
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }

    private String resolveToken(ServerHttpRequest request, ServletServerHttpRequest servletRequest) {
        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7).trim();
        }

        String protocolHeader = request.getHeaders().getFirst(SEC_WEBSOCKET_PROTOCOL_HEADER);
        String protocolToken = resolveProtocolToken(protocolHeader);
        if (protocolToken != null) {
            return protocolToken;
        }

        String queryToken = servletRequest.getServletRequest().getParameter("token");
        return queryToken == null || queryToken.isBlank() ? null : queryToken.trim();
    }

    private String resolveProtocolToken(String protocolHeader) {
        if (protocolHeader == null || protocolHeader.isBlank()) {
            return null;
        }

        List<String> values = List.of(protocolHeader.split(",")).stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
        for (int index = 0; index < values.size(); index++) {
            String value = values.get(index);
            if (value.startsWith("Bearer ")) {
                return value.substring(7).trim();
            }
            if ("bearer".equalsIgnoreCase(value) && index + 1 < values.size()) {
                return values.get(index + 1);
            }
            if (value.startsWith("token.")) {
                return value.substring("token.".length()).trim();
            }
        }
        return null;
    }
}
