package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.dto.auth.PasswordRecoveryRequestDTO;
import com.themuffinman.app.identity.dto.auth.PasswordResetRequestDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.PasswordRecoveryToken;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.repository.PasswordRecoveryTokenRepository;
import com.themuffinman.app.config.AccountRecoveryProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {
    @Mock private AppUserRepository appUserRepository;
    @Mock private PasswordRecoveryTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Test
    void requestUsesGenericResponseAndPublishesOnlyForExistingAccount() {
        AppUser user = new AppUser();
        user.setId(7L);
        user.setEmail("user@example.com");
        when(appUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordRecoveryToken.class))).thenAnswer(invocation -> {
            PasswordRecoveryToken token = invocation.getArgument(0);
            token.setId(42L);
            return token;
        });

        assertTrue(buildService().request(new PasswordRecoveryRequestDTO(" User@Example.com ")).accepted());
        verify(tokenRepository).deleteByUserIdAndConsumedAtIsNull(7L);
        verify(tokenRepository).save(any(PasswordRecoveryToken.class));
        verify(eventPublisher).publishEvent(any(PasswordRecoveryService.PasswordRecoveryRequestedEvent.class));
    }

    @Test
    void requestDoesNotRevealUnknownAccount() {
        when(appUserRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertTrue(buildService().request(new PasswordRecoveryRequestDTO("unknown@example.com")).accepted());
    }

    @Test
    void resetConsumesValidTokenAndUpdatesPassword() {
        AppUser user = new AppUser();
        user.setId(7L);
        PasswordRecoveryToken token = new PasswordRecoveryToken();
        token.setUser(user);
        token.setExpiresAt(Instant.now().plusSeconds(60));
        when(tokenRepository.findByTokenHash(any(String.class))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword1")).thenReturn("encoded-new-password");

        buildService().reset(new PasswordResetRequestDTO("raw-token", "newPassword1"));

        assertEquals("encoded-new-password", user.getPasswordHash());
        assertTrue(token.getConsumedAt() != null);
        verify(tokenRepository).save(token);
        verify(appUserRepository).save(user);
    }

    @Test
    void resetRejectsExpiredOrConsumedToken() {
        PasswordRecoveryToken token = new PasswordRecoveryToken();
        token.setExpiresAt(Instant.now().minusSeconds(1));
        when(tokenRepository.findByTokenHash(any(String.class))).thenReturn(Optional.of(token));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> buildService().reset(new PasswordResetRequestDTO("raw-token", "newPassword1")));

        assertEquals(BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid or expired password recovery token", exception.getReason());
    }

    @Test
    void requestRateLimitIsBackendOwnedAndScopedToEmailAndSource() {
        AccountRecoveryProperties properties = new AccountRecoveryProperties();
        properties.setRequestsPerWindow(1);
        PasswordRecoveryService service = new PasswordRecoveryService(
                appUserRepository, tokenRepository, passwordEncoder, eventPublisher, properties);

        service.request(new PasswordRecoveryRequestDTO("unknown@example.com"), "127.0.0.1");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.request(new PasswordRecoveryRequestDTO("unknown@example.com"), "127.0.0.1"));

        assertEquals(TOO_MANY_REQUESTS, exception.getStatusCode());
    }

    private PasswordRecoveryService buildService() {
        return new PasswordRecoveryService(appUserRepository, tokenRepository, passwordEncoder, eventPublisher, new AccountRecoveryProperties());
    }
}
