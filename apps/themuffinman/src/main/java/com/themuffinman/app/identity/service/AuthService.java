package com.themuffinman.app.identity.service;

import com.jsakoman.authfoundation.CredentialInputPolicy;
import com.jsakoman.authfoundation.EmailPolicy;
import com.jsakoman.authfoundation.PasswordPolicyProfile;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.dto.auth.AuthResponseDTO;
import com.themuffinman.app.identity.dto.auth.LoginRequestDTO;
import com.themuffinman.app.identity.dto.auth.RegisterRequestDTO;
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
    private static final CredentialInputPolicy CREDENTIAL_INPUT_POLICY = CredentialInputPolicy.forProfile(
            EmailPolicy.legacyCompatibleBaseline(), PasswordPolicyProfile.SINGLE_FACTOR);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMgr authMgr;

    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {
        String email = normalizeEmail(registerRequest.email());
        if (appUserRepository.existsByEmail(email)) {
            throw ServiceErrors.conflict("Email already exists");
        }

        AppUser savedAppUser = appUserRepository.save(buildRegisteredUser(registerRequest, email, validateNewPassword(registerRequest.password())));
        return authMgr.toResponse(savedAppUser, jwtService.generateToken(savedAppUser));
    }

    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        String email = normalizeEmail(loginRequest.email());
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> ServiceErrors.unauthorized("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.password(), appUser.getPasswordHash())) {
            throw ServiceErrors.unauthorized("Invalid email or password");
        }

        return authMgr.toResponse(appUser, jwtService.generateToken(appUser));
    }

    public AuthResponseDTO me(AppUser appUser) {
        return authMgr.toResponse(appUser, null);
    }

    private AppUser buildRegisteredUser(RegisterRequestDTO registerRequest, String email, String password) {
        AppUser appUser = new AppUser();
        appUser.setEmail(email);
        appUser.setUsername(registerRequest.username());
        appUser.setPasswordHash(passwordEncoder.encode(password));
        appUser.setRole(AppUserRole.USER);
        return appUser;
    }

    private String normalizeEmail(String email) {
        return CREDENTIAL_INPUT_POLICY.normalizeEmail(email).value();
    }

    private String validateNewPassword(String password) {
        try {
            return CREDENTIAL_INPUT_POLICY.validatePassword(password);
        } catch (IllegalArgumentException exception) {
            throw ServiceErrors.badRequest("Password must contain at least 15 Unicode characters");
        }
    }
}
