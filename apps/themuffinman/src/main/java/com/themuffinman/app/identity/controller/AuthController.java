package com.themuffinman.app.identity.controller;

import com.themuffinman.app.identity.dto.auth.AuthResponse;
import com.themuffinman.app.identity.dto.auth.LoginRequest;
import com.themuffinman.app.identity.dto.auth.RegisterRequest;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        AppUser appUser = (AppUser) authentication.getPrincipal();
        return authService.me(appUser);
    }
}
