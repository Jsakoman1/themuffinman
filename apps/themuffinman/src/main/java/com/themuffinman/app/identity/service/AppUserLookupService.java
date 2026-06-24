package com.themuffinman.app.identity.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserLookupService {

    private final AppUserRepository appUserRepository;

    public AppUser requireById(Long userId) {
        return requireById(userId, "AppUser not found with id " + userId);
    }

    public AppUser requireById(Long userId, String notFoundMessage) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> ServiceErrors.notFound(notFoundMessage));
    }
}
