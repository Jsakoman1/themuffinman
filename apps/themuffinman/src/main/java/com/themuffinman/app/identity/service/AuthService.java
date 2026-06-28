package com.themuffinman.app.identity.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.normalization.UserInputNormalizer;
import com.themuffinman.app.identity.dto.auth.AuthResponse;
import com.themuffinman.app.identity.dto.auth.LoginRequest;
import com.themuffinman.app.identity.dto.auth.RegisterRequest;
import com.themuffinman.app.identity.mapper.AuthMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMgr authMgr;

    public AuthResponse register(RegisterRequest registerRequest) {
        String email = UserInputNormalizer.normalizeEmail(registerRequest.email());
        if (appUserRepository.existsByEmail(email)) {
            throw ServiceErrors.conflict("Email already exists");
        }

        AppUser savedAppUser = appUserRepository.save(buildRegisteredUser(registerRequest, email));
        return authMgr.toResponse(savedAppUser, jwtService.generateToken(savedAppUser));
    }

    public AuthResponse login(LoginRequest loginRequest) {
        String email = UserInputNormalizer.normalizeEmail(loginRequest.email());
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> ServiceErrors.unauthorized("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.password(), appUser.getPasswordHash())) {
            throw ServiceErrors.unauthorized("Invalid email or password");
        }

        return authMgr.toResponse(appUser, jwtService.generateToken(appUser));
    }

    public AuthResponse me(AppUser appUser) {
        return authMgr.toResponse(appUser, null);
    }

    private AppUser buildRegisteredUser(RegisterRequest registerRequest, String email) {
        AppUser appUser = new AppUser();
        appUser.setEmail(email);
        appUser.setUsername(registerRequest.username());
        appUser.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
        appUser.setRole(AppUserRole.USER);
        return appUser;
    }
}
