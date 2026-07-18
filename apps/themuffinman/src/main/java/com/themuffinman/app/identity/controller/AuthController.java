package com.themuffinman.app.identity.controller;

import com.themuffinman.app.identity.dto.auth.AuthResponseDTO;
import com.themuffinman.app.identity.dto.auth.LoginRequestDTO;
import com.themuffinman.app.identity.dto.auth.RegisterRequestDTO;
import com.themuffinman.app.identity.dto.auth.PasswordRecoveryRequestDTO;
import com.themuffinman.app.identity.dto.auth.PasswordRecoveryResponseDTO;
import com.themuffinman.app.identity.dto.auth.PasswordResetRequestDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AuthService;
import com.themuffinman.app.identity.service.PasswordRecoveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/register")
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/password-recovery")
    public PasswordRecoveryResponseDTO requestPasswordRecovery(
            @Valid @RequestBody PasswordRecoveryRequestDTO request,
            HttpServletRequest httpRequest) {
        return passwordRecoveryService.request(request, httpRequest.getRemoteAddr());
    }

    @PostMapping("/password-reset")
    public void resetPassword(@Valid @RequestBody PasswordResetRequestDTO request) {
        passwordRecoveryService.reset(request);
    }

    @GetMapping("/me")
    public AuthResponseDTO me(Authentication authentication) {
        AppUser appUser = (AppUser) authentication.getPrincipal();
        return authService.me(appUser);
    }
}
