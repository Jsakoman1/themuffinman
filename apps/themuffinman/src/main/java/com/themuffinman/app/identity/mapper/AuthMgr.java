package com.themuffinman.app.identity.mapper;

import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.dto.auth.AuthResponse;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import org.springframework.stereotype.Component;

@Component
public class AuthMgr {

    public AuthResponse toResponse(AppUser appUser, String token) {
        if (appUser == null) {
            return null;
        }

        return new AuthResponse(
                appUser.getId(),
                appUser.getEmail(),
                appUser.getUsername(),
                RichTextInputValidator.sanitize(appUser.getProfileDescription()),
                appUser.getProfileAvatarDataUrl(),
                appUser.getCreatedAt(),
                appUser.getRole() == null ? AppUserRole.USER.name() : appUser.getRole().name(),
                token);
    }
}
