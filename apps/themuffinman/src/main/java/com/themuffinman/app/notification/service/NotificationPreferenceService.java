package com.themuffinman.app.notification.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.notification.dto.NotificationPreferenceItemDTO;
import com.themuffinman.app.notification.dto.NotificationPreferenceResponseDTO;
import com.themuffinman.app.notification.dto.NotificationPreferenceUpdateDTO;
import com.themuffinman.app.notification.model.NotificationPreference;
import com.themuffinman.app.notification.model.NotificationPreferenceCategory;
import com.themuffinman.app.notification.model.NotificationPreferenceLevel;
import com.themuffinman.app.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {
    private final NotificationPreferenceRepository repository;

    @Transactional(readOnly = true)
    public NotificationPreferenceResponseDTO getForUser(AppUser user) {
        Map<String, Boolean> saved = new java.util.HashMap<>();
        repository.findByUserId(user.getId()).forEach(item -> saved.put(key(item.getCategory(), item.getLevel()), item.isEnabled()));
        List<NotificationPreferenceItemDTO> items = java.util.Arrays.stream(NotificationPreferenceCategory.values())
                .flatMap(category -> java.util.Arrays.stream(NotificationPreferenceLevel.values())
                        .map(level -> NotificationPreferenceItemDTO.builder()
                                .category(category).level(level)
                                .enabled(saved.getOrDefault(key(category, level), true))
                                .required(category == NotificationPreferenceCategory.SYSTEM)
                                .build()))
                .toList();
        return NotificationPreferenceResponseDTO.builder().items(items).build();
    }

    @Transactional
    public NotificationPreferenceResponseDTO update(AppUser user, List<NotificationPreferenceUpdateDTO> updates) {
        if (updates == null || updates.isEmpty()) {
            throw ServiceErrors.badRequest("At least one notification preference is required");
        }
        Map<String, NotificationPreference> existing = new java.util.HashMap<>();
        repository.findByUserId(user.getId()).forEach(item -> existing.put(key(item.getCategory(), item.getLevel()), item));
        for (NotificationPreferenceUpdateDTO update : updates) {
            if (update.getCategory() == NotificationPreferenceCategory.SYSTEM && Boolean.FALSE.equals(update.getEnabled())) {
                throw ServiceErrors.badRequest("System notifications cannot be disabled");
            }
            String key = key(update.getCategory(), update.getLevel());
            NotificationPreference preference = existing.computeIfAbsent(key, ignored -> {
                NotificationPreference created = new NotificationPreference();
                created.setUser(user);
                created.setCategory(update.getCategory());
                created.setLevel(update.getLevel());
                return created;
            });
            preference.setEnabled(Boolean.TRUE.equals(update.getEnabled()));
        }
        repository.saveAll(existing.values());
        return getForUser(user);
    }

    private String key(NotificationPreferenceCategory category, NotificationPreferenceLevel level) {
        return category.name() + ":" + level.name();
    }
}
