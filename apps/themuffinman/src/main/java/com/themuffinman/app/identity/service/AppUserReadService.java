package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.common.normalization.SearchQueryNormalizer;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.vision.mapper.QuestMgr;
import com.themuffinman.app.vision.model.QuestStatus;
import com.themuffinman.app.vision.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppUserReadService {

    private final AppUserRepository appUserRepository;
    private final AppUserLookupService appUserLookupService;
    private final QuestRepository questRepository;
    private final QuestMgr questMgr;

    @Transactional(readOnly = true)
    public List<AppUser> getAllAppUsers(String query) {
        String normalizedQuery = SearchQueryNormalizer.normalize(query).toLowerCase();

        return appUserRepository.findAll().stream()
                .filter(appUser -> matchesUserQuery(appUser, normalizedQuery))
                .sorted(Comparator.comparing(AppUser::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public AppUser getAppUser(Long id) {
        return appUserLookupService.requireById(id);
    }

    @Transactional(readOnly = true)
    public long countQuestsByCreatorId(Long creatorId) {
        return questRepository.countByCreatorIdAndStatus(creatorId, QuestStatus.OPEN);
    }

    @Transactional(readOnly = true)
    public List<QuestResponseDTO> getOpenQuestsByCreatorId(Long creatorId) {
        return questRepository.findByCreatorIdAndStatusOrderByIdDesc(creatorId, QuestStatus.OPEN)
                .stream()
                .limit(6)
                .map(questMgr::toDto)
                .toList();
    }

    private boolean matchesUserQuery(AppUser appUser, String normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            return true;
        }

        return normalizedHaystack(
                appUser.getUsername(),
                appUser.getEmail(),
                appUser.getRole() == null ? AppUserRole.USER.name() : appUser.getRole().name(),
                appUser.getProfileDescription()
        ).contains(normalizedQuery);
    }

    private String normalizedHaystack(String... values) {
        return java.util.Arrays.stream(values)
                .map(value -> value == null ? "" : value)
                .reduce((left, right) -> left + " " + right)
                .orElse("")
                .toLowerCase();
    }
}
