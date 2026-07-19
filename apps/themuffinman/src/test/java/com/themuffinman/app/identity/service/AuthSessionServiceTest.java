package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.model.RevokedAuthToken;
import com.themuffinman.app.identity.repository.RevokedAuthTokenRepository;
import com.themuffinman.app.identity.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthSessionServiceTest {
    @Mock
    private RevokedAuthTokenRepository revokedAuthTokenRepository;

    @Mock
    private JwtService jwtService;

    @Test
    void revokeStoresOnlyTheTokenHashUntilJwtExpiry() {
        Instant expiry = Instant.parse("2026-07-20T12:00:00Z");
        when(jwtService.extractBearerToken("Bearer jwt-token")).thenReturn("jwt-token");
        when(jwtService.hashToken("jwt-token")).thenReturn("hashed-token");
        when(jwtService.extractExpiration("jwt-token")).thenReturn(expiry);

        var result = new AuthSessionService(revokedAuthTokenRepository, jwtService).revoke("Bearer jwt-token");

        assertEquals("LOGOUT", result.getAction());
        var captured = org.mockito.ArgumentCaptor.forClass(RevokedAuthToken.class);
        verify(revokedAuthTokenRepository).save(captured.capture());
        assertEquals("hashed-token", captured.getValue().getTokenHash());
        assertEquals(expiry, captured.getValue().getExpiresAt());
    }

    @Test
    void revokedCheckHashesTheRawToken() {
        when(jwtService.hashToken("jwt-token")).thenReturn("hashed-token");
        when(revokedAuthTokenRepository.existsByTokenHashAndExpiresAtAfter(any(), any())).thenReturn(true);

        assertTrue(new AuthSessionService(revokedAuthTokenRepository, jwtService).isRevoked("jwt-token"));
        verify(revokedAuthTokenRepository).existsByTokenHashAndExpiresAtAfter(any(), any());
    }
}
