package com.themuffinman.app.identity.controller;

import com.themuffinman.app.identity.dto.auth.LoginRequest;
import com.themuffinman.app.identity.dto.auth.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.Valid;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.themuffinman.app.identity.dto.auth.AuthResponse;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @AfterAll
    static void tearDown() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void registerAndLoginEndpointsRequireValidPayloads() throws Exception {
        Method registerMethod = AuthController.class.getDeclaredMethod("register", RegisterRequest.class);
        Method loginMethod = AuthController.class.getDeclaredMethod("login", LoginRequest.class);

        assertTrue(registerMethod.getParameters()[0].isAnnotationPresent(Valid.class));
        assertTrue(loginMethod.getParameters()[0].isAnnotationPresent(Valid.class));
    }

    @Test
    void registerRequestRejectsInvalidValues() {
        RegisterRequest request = new RegisterRequest("not-an-email", "ab", "short");

        Set<ConstraintViolation<RegisterRequest>> violations = VALIDATOR.validate(request);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "email");
        assertHasViolation(violations, "username");
        assertHasViolation(violations, "password");
    }

    @Test
    void loginRequestRejectsInvalidValues() {
        LoginRequest request = new LoginRequest("not-an-email", "");

        Set<ConstraintViolation<LoginRequest>> violations = VALIDATOR.validate(request);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "email");
        assertHasViolation(violations, "password");
    }

    @Test
    void validAuthRequestsPassValidation() {
        RegisterRequest registerRequest = new RegisterRequest("user@example.com", "new-user", "strongPassword1");
        LoginRequest loginRequest = new LoginRequest("user@example.com", "strongPassword1");

        assertTrue(VALIDATOR.validate(registerRequest).isEmpty());
        assertTrue(VALIDATOR.validate(loginRequest).isEmpty());
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

        AuthResponse response = authController.login(new LoginRequest(" User@Example.com ", "strongPassword1"));

        verify(appUserRepository).findByEmail("user@example.com");
        assertEquals("jwt-token", response.token());
        assertEquals("user@example.com", response.email());
    }

    @Test
    void loginUsesSameUnauthorizedMessageForMissingEmail() {
        when(appUserRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authController.login(new LoginRequest("user@example.com", "strongPassword1"))
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
                () -> authController.login(new LoginRequest("user@example.com", "wrong-password"))
        );

        assertEquals(UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid email or password", exception.getReason());
    }

    private void assertHasViolation(Set<? extends ConstraintViolation<?>> violations, String propertyPath) {
        assertTrue(
                violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals(propertyPath)),
                "Expected violation for property: " + propertyPath
        );
    }
}
