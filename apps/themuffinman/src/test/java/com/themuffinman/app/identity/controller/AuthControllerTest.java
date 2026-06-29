package com.themuffinman.app.identity.controller;

import com.themuffinman.app.identity.dto.auth.AuthResponseDTO;
import com.themuffinman.app.identity.dto.auth.LoginRequestDTO;
import com.themuffinman.app.identity.dto.auth.RegisterRequestDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AuthService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @AfterAll
    static void tearDown() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void registerAndLoginEndpointsRequireValidPayloads() throws Exception {
        Method registerMethod = AuthController.class.getDeclaredMethod("register", RegisterRequestDTO.class);
        Method loginMethod = AuthController.class.getDeclaredMethod("login", LoginRequestDTO.class);

        assertTrue(registerMethod.getParameters()[0].isAnnotationPresent(Valid.class));
        assertTrue(loginMethod.getParameters()[0].isAnnotationPresent(Valid.class));
    }

    @Test
    void registerDelegatesToAuthService() {
        RegisterRequestDTO request = new RegisterRequestDTO("user@example.com", "new-user", "strongPassword1");
        AuthResponseDTO expected = new AuthResponseDTO(1L, "user@example.com", "new-user", null, null, Instant.now(), "USER", "jwt-token");
        when(authService.register(request)).thenReturn(expected);

        AuthResponseDTO response = authController.register(request);

        assertEquals(expected, response);
        verify(authService).register(request);
    }

    @Test
    void loginDelegatesToAuthService() {
        LoginRequestDTO request = new LoginRequestDTO("user@example.com", "strongPassword1");
        AuthResponseDTO expected = new AuthResponseDTO(1L, "user@example.com", "new-user", null, null, Instant.now(), "USER", "jwt-token");
        when(authService.login(request)).thenReturn(expected);

        AuthResponseDTO response = authController.login(request);

        assertEquals(expected, response);
        verify(authService).login(request);
    }

    @Test
    void meDelegatesAuthenticatedPrincipalToAuthService() {
        AppUser appUser = new AppUser();
        appUser.setId(7L);
        Authentication authentication = new UsernamePasswordAuthenticationToken(appUser, null);
        AuthResponseDTO expected = new AuthResponseDTO(7L, "user@example.com", "user", null, null, Instant.now(), "USER", null);
        when(authService.me(appUser)).thenReturn(expected);

        AuthResponseDTO response = authController.me(authentication);

        assertEquals(expected, response);
        verify(authService).me(appUser);
    }

    @Test
    void registerRequestRejectsInvalidValues() {
        RegisterRequestDTO request = new RegisterRequestDTO("not-an-email", "ab", "short");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = VALIDATOR.validate(request);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "email");
        assertHasViolation(violations, "username");
        assertHasViolation(violations, "password");
    }

    @Test
    void loginRequestRejectsInvalidValues() {
        LoginRequestDTO request = new LoginRequestDTO("not-an-email", "");

        Set<ConstraintViolation<LoginRequestDTO>> violations = VALIDATOR.validate(request);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "email");
        assertHasViolation(violations, "password");
    }

    @Test
    void validAuthRequestsPassValidation() {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO("user@example.com", "new-user", "strongPassword1");
        LoginRequestDTO loginRequest = new LoginRequestDTO("user@example.com", "strongPassword1");

        assertTrue(VALIDATOR.validate(registerRequest).isEmpty());
        assertTrue(VALIDATOR.validate(loginRequest).isEmpty());
    }

    private void assertHasViolation(Set<? extends ConstraintViolation<?>> violations, String propertyPath) {
        assertTrue(
                violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().equals(propertyPath)),
                "Expected violation for property: " + propertyPath
        );
    }
}
