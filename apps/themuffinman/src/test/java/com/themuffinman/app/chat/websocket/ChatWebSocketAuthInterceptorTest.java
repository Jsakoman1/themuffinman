package com.themuffinman.app.chat.websocket;

import com.themuffinman.app.chat.service.ChatAuditService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatWebSocketAuthInterceptorTest {

    private static final String SEC_WEBSOCKET_PROTOCOL_HEADER = "Sec-WebSocket-Protocol";

    private final JwtService jwtService = mock(JwtService.class);
    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
    private final ChatAuditService chatAuditService = mock(ChatAuditService.class);
    private ChatWebSocketAuthInterceptor interceptor;
    private AppUser currentUser;

    @BeforeEach
    void setUp() {
        interceptor = new ChatWebSocketAuthInterceptor(jwtService, appUserRepository, chatAuditService);
        currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setEmail("mia@sidequest.test");
        currentUser.setUsername("mia");
        currentUser.setPasswordHash("hash");
    }

    @Test
    void beforeHandshakeAcceptsAuthorizationBearerToken() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-123");
        ServletServerHttpRequest request = new ServletServerHttpRequest(servletRequest);
        ServletServerHttpResponse response = new ServletServerHttpResponse(new MockHttpServletResponse());
        Map<String, Object> attributes = new HashMap<>();

        when(jwtService.extractEmail("token-123")).thenReturn(currentUser.getEmail());
        when(appUserRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));

        boolean allowed = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), attributes);

        assertTrue(allowed);
        assertEquals(currentUser, attributes.get(ChatWebSocketAuthInterceptor.CHAT_SOCKET_USER_ATTRIBUTE));
    }

    @Test
    void beforeHandshakeAcceptsBearerSubprotocolFallback() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(SEC_WEBSOCKET_PROTOCOL_HEADER, "chat, bearer, token-456");
        ServletServerHttpRequest request = new ServletServerHttpRequest(servletRequest);
        ServletServerHttpResponse response = new ServletServerHttpResponse(new MockHttpServletResponse());
        Map<String, Object> attributes = new HashMap<>();

        when(jwtService.extractEmail("token-456")).thenReturn(currentUser.getEmail());
        when(appUserRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));

        boolean allowed = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), attributes);

        assertTrue(allowed);
        assertEquals(currentUser, attributes.get(ChatWebSocketAuthInterceptor.CHAT_SOCKET_USER_ATTRIBUTE));
    }

    @Test
    void beforeHandshakeAcceptsTokenPrefixedSubprotocolFallback() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(SEC_WEBSOCKET_PROTOCOL_HEADER, "chat, token.token-999");
        ServletServerHttpRequest request = new ServletServerHttpRequest(servletRequest);
        ServletServerHttpResponse response = new ServletServerHttpResponse(new MockHttpServletResponse());
        Map<String, Object> attributes = new HashMap<>();

        when(jwtService.extractEmail("token-999")).thenReturn(currentUser.getEmail());
        when(appUserRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));

        boolean allowed = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), attributes);

        assertTrue(allowed);
        assertEquals(currentUser, attributes.get(ChatWebSocketAuthInterceptor.CHAT_SOCKET_USER_ATTRIBUTE));
    }

    @Test
    void beforeHandshakeFallsBackToQueryToken() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setParameter("token", "token-789");
        ServletServerHttpRequest request = new ServletServerHttpRequest(servletRequest);
        ServletServerHttpResponse response = new ServletServerHttpResponse(new MockHttpServletResponse());
        Map<String, Object> attributes = new HashMap<>();

        when(jwtService.extractEmail("token-789")).thenReturn(currentUser.getEmail());
        when(appUserRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));

        boolean allowed = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), attributes);

        assertTrue(allowed);
        assertEquals(currentUser, attributes.get(ChatWebSocketAuthInterceptor.CHAT_SOCKET_USER_ATTRIBUTE));
    }

    @Test
    void beforeHandshakeRejectsMissingToken() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        ServletServerHttpRequest request = new ServletServerHttpRequest(servletRequest);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletServerHttpResponse response = new ServletServerHttpResponse(servletResponse);

        boolean allowed = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), new HashMap<>());

        assertEquals(false, allowed);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), servletResponse.getStatus());
    }
}
