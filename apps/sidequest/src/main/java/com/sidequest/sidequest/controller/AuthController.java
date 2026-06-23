package com.sidequest.sidequest.controller;

import com.sidequest.sidequest.dto.auth.AuthResponse;
import com.sidequest.sidequest.dto.auth.LoginRequest;
import com.sidequest.sidequest.dto.auth.RegisterRequest;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.model.AppUserRole;
import com.sidequest.sidequest.repository.AppUserRepository;
import com.sidequest.sidequest.security.JwtService;
import com.sidequest.sidequest.service.ServiceErrors;
import com.sidequest.sidequest.service.RichTextInputValidator;
import com.sidequest.sidequest.service.UserInputNormalizer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
        String email = UserInputNormalizer.normalizeEmail(registerRequest.email());
        if (appUserRepository.existsByEmail(email)) {
            throw ServiceErrors.conflict("Email already exists");
        }

        AppUser savedAppUser = appUserRepository.save(buildRegisteredUser(registerRequest, email));
        return buildAuthResponse(savedAppUser, jwtService.generateToken(savedAppUser));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        String email = UserInputNormalizer.normalizeEmail(loginRequest.email());
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> ServiceErrors.unauthorized("Invalid email"));

        if (!passwordEncoder.matches(loginRequest.password(), appUser.getPasswordHash())) {
            throw ServiceErrors.unauthorized("Invalid password");
        }

        return buildAuthResponse(appUser, jwtService.generateToken(appUser));
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        AppUser appUser = (AppUser) authentication.getPrincipal();
        return buildAuthResponse(appUser, null);
    }

    private AppUser buildRegisteredUser(RegisterRequest registerRequest, String email) {
        AppUser appUser = new AppUser();
        appUser.setEmail(email);
        appUser.setUsername(registerRequest.username());
        appUser.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
        appUser.setRole(AppUserRole.USER);
        return appUser;
    }

    private AuthResponse buildAuthResponse(AppUser appUser, String token) {
        return new AuthResponse(
                appUser.getId(),
                appUser.getEmail(),
                appUser.getUsername(),
                RichTextInputValidator.sanitize(appUser.getProfileDescription()),
                appUser.getProfileAvatarDataUrl(),
                appUser.getCreatedAt(),
                resolveRoleName(appUser),
                token);
    }

    private String resolveRoleName(AppUser appUser) {
        return appUser.getRole() == null ? AppUserRole.USER.name() : appUser.getRole().name();
    }
}
