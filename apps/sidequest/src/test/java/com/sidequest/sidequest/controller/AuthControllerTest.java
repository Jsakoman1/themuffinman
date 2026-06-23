package com.sidequest.sidequest.controller;

import com.sidequest.sidequest.dto.auth.LoginRequest;
import com.sidequest.sidequest.dto.auth.RegisterRequest;
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

import com.sidequest.sidequest.dto.auth.AuthResponse;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.repository.AppUserRepository;
import com.sidequest.sidequest.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private void assertHasViolation(Set<? extends ConstraintViolation<?>> violations, String propertyPath) {
        assertTrue(
                violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals(propertyPath)),
                "Expected violation for property: " + propertyPath
        );
    }
}
