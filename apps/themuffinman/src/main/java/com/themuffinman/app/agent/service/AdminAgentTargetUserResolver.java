package com.themuffinman.app.agent.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AdminAgentTargetUserResolver {

    private final AppUserRepository appUserRepository;

    public AdminAgentTargetUserResolver(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser resolveExactUser(String rawQuery) {
        String query = normalize(rawQuery);
        if (query == null) {
            throw ServiceErrors.badRequest("Target user query is required");
        }

        return appUserRepository.findByEmail(query)
                .orElseGet(() -> resolveExactUsername(query, rawQuery));
    }

    private AppUser resolveExactUsername(String query, String rawQuery) {
        List<AppUser> matches = appUserRepository.searchByUsernameOrEmail(query).stream()
                .filter(user -> normalize(user.getUsername()).equals(query) || normalize(user.getEmail()).equals(query))
                .toList();
        if (matches.isEmpty()) {
            throw ServiceErrors.badRequest("No exact user match was found for \"" + rawLabel(rawQuery) + "\"");
        }
        if (matches.size() > 1) {
            throw ServiceErrors.badRequest("More than one exact user match was found for \"" + rawLabel(rawQuery) + "\"");
        }
        return matches.get(0);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.startsWith("@")) {
            normalized = normalized.substring(1);
        }
        normalized = normalized.replace("\"", "").replace("'", "");
        return normalized.isBlank() ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String rawLabel(String value) {
        return value == null ? "" : value.trim();
    }
}
