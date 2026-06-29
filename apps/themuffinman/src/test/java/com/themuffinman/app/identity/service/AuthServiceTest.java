package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.dto.auth.AuthResponseDTO;
import com.themuffinman.app.identity.dto.auth.LoginRequestDTO;
import com.themuffinman.app.identity.dto.auth.RegisterRequestDTO;
import com.themuffinman.app.identity.mapper.AuthMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private final AuthMgr authMgr = new AuthMgr();

    @Test
    void registerNormalizesEmailAndReturnsTokenizedResponse() {
        RegisterRequestDTO request = new RegisterRequestDTO(" User@Example.com ", "new-user", "strongPassword1");
        AppUser savedUser = new AppUser();
        savedUser.setId(3L);
        savedUser.setEmail("user@example.com");
        savedUser.setUsername("new-user");
        savedUser.setCreatedAt(Instant.parse("2026-01-01T12:00:00Z"));

        when(passwordEncoder.encode("strongPassword1")).thenReturn("encoded-password");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");

        AuthResponseDTO response = buildService().register(request);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        AppUser persisted = captor.getValue();
        assertEquals("user@example.com", persisted.getEmail());
        assertEquals("new-user", persisted.getUsername());
        assertEquals("encoded-password", persisted.getPasswordHash());
        assertEquals(AppUserRole.USER, persisted.getRole());
        assertEquals("jwt-token", response.token());
        assertEquals("user@example.com", response.email());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(appUserRepository.existsByEmail("user@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> buildService().register(new RegisterRequestDTO("user@example.com", "new-user", "strongPassword1"))
        );

        assertEquals(CONFLICT, exception.getStatusCode());
        assertEquals("Email already exists", exception.getReason());
    }

    @Test
    void loginNormalizesEmailBeforeLookup() {
        AppUser appUser = new AppUser();
        appUser.setId(7L);
        appUser.setEmail("user@example.com");
        appUser.setUsername("user");
        appUser.setPasswordHash("encoded-password");
        appUser.setCreatedAt(Instant.parse("2026-01-01T12:00:00Z"));

        when(appUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(appUser));
        when(passwordEncoder.matches("strongPassword1", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn("jwt-token");

        AuthResponseDTO response = buildService().login(new LoginRequestDTO(" User@Example.com ", "strongPassword1"));

        verify(appUserRepository).findByEmail("user@example.com");
        assertEquals("jwt-token", response.token());
        assertEquals("user@example.com", response.email());
    }

    @Test
    void loginUsesSameUnauthorizedMessageForMissingEmail() {
        when(appUserRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> buildService().login(new LoginRequestDTO("user@example.com", "strongPassword1"))
        );

        assertEquals(UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid email or password", exception.getReason());
    }

    @Test
    void loginUsesSameUnauthorizedMessageForWrongPassword() {
        AppUser appUser = new AppUser();
        appUser.setEmail("user@example.com");
        appUser.setPasswordHash("encoded-password");

        when(appUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(appUser));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> buildService().login(new LoginRequestDTO("user@example.com", "wrong-password"))
        );

        assertEquals(UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid email or password", exception.getReason());
    }

    @Test
    void meReturnsCurrentUserWithoutToken() {
        AppUser appUser = new AppUser();
        appUser.setId(7L);
        appUser.setEmail("user@example.com");
        appUser.setUsername("user");
        appUser.setRole(AppUserRole.ADMIN);
        appUser.setCreatedAt(Instant.parse("2026-01-01T12:00:00Z"));

        AuthResponseDTO response = buildService().me(appUser);

        assertEquals(7L, response.id());
        assertEquals("ADMIN", response.role());
        assertNull(response.token());
    }

    private AuthService buildService() {
        return new AuthService(appUserRepository, passwordEncoder, jwtService, authMgr);
    }
}
