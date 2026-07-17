package com.themuffinman.app.search.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.search.dto.SavedSearchIntentRequestDTO;
import com.themuffinman.app.search.dto.SavedSearchIntentResponseDTO;
import com.themuffinman.app.search.model.SavedSearchIntent;
import com.themuffinman.app.search.repository.SavedSearchIntentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class SavedSearchIntentService {
    private static final List<String> FAMILIES = List.of("quest", "user", "business", "ride", "thing", "circle", "conversation");
    private final SavedSearchIntentRepository repository;

    public List<SavedSearchIntentResponseDTO> getMine(AppUser user) { return repository.findByOwnerId(user.getId()).stream().map(this::toDto).toList(); }

    @Transactional
    public SavedSearchIntentResponseDTO create(SavedSearchIntentRequestDTO request, AppUser user) {
        SavedSearchIntent intent = new SavedSearchIntent(); intent.setOwner(user); return save(intent, request);
    }

    @Transactional
    public SavedSearchIntentResponseDTO update(Long id, SavedSearchIntentRequestDTO request, AppUser user) {
        SavedSearchIntent intent = repository.findOwnedById(id, user.getId()).orElseThrow(() -> ServiceErrors.notFound("Saved search not found"));
        return save(intent, request);
    }

    @Transactional
    public void delete(Long id, AppUser user) { repository.delete(repository.findOwnedById(id, user.getId()).orElseThrow(() -> ServiceErrors.notFound("Saved search not found"))); }

    private SavedSearchIntentResponseDTO save(SavedSearchIntent intent, SavedSearchIntentRequestDTO request) {
        if (request == null || request.getQuery() == null || request.getQuery().trim().isBlank()) throw ServiceErrors.badRequest("Saved search query is required");
        String query = request.getQuery().trim(); if (query.length() > 240) throw ServiceErrors.badRequest("Saved search query is too long");
        String family = request.getEntityFamily() == null || request.getEntityFamily().isBlank() ? null : request.getEntityFamily().trim().toLowerCase(Locale.ROOT);
        if (family != null && !FAMILIES.contains(family)) throw ServiceErrors.badRequest("Unsupported saved search family");
        if (request.getExpiresAt() != null && !request.getExpiresAt().isAfter(Instant.now())) throw ServiceErrors.badRequest("Saved search expiry must be in the future");
        intent.setQueryText(query); intent.setEntityFamily(family); intent.setPaused(request.isPaused()); intent.setNotifyEnabled(request.getNotifyEnabled() == null || request.getNotifyEnabled()); intent.setExpiresAt(request.getExpiresAt());
        return toDto(repository.save(intent));
    }

    private SavedSearchIntentResponseDTO toDto(SavedSearchIntent intent) { return SavedSearchIntentResponseDTO.builder().id(intent.getId()).query(intent.getQueryText()).entityFamily(intent.getEntityFamily()).paused(intent.isPaused()).notifyEnabled(intent.isNotifyEnabled()).expiresAt(intent.getExpiresAt()).createdAt(intent.getCreatedAt()).updatedAt(intent.getUpdatedAt()).build(); }
}
