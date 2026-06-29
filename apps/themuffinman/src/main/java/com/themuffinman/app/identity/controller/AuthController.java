package com.themuffinman.app.identity.controller;

import com.themuffinman.app.identity.dto.auth.AuthResponseDTO;
import com.themuffinman.app.identity.dto.auth.LoginRequestDTO;
import com.themuffinman.app.identity.dto.auth.RegisterRequestDTO;
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
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        return authService.login(loginRequest);
    }

    @GetMapping("/me")
    public AuthResponseDTO me(Authentication authentication) {
        AppUser appUser = (AppUser) authentication.getPrincipal();
        return authService.me(appUser);
    }
}
