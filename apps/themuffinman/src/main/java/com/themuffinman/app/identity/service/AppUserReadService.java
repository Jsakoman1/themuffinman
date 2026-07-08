package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.common.search.SearchTextSupport;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestMgr;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
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
    private final WorkmarketQuestRepository questRepository;
    private final WorkmarketQuestMgr questMgr;

    @Transactional(readOnly = true)
    public List<AppUser> getAllAppUsers(String query) {
        String normalizedQuery = SearchTextSupport.normalizeQuery(query);

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

        return SearchTextSupport.containsAnyNormalized(normalizedQuery,
                appUser.getUsername(),
                appUser.getEmail(),
                appUser.getRole() == null ? AppUserRole.USER.name() : appUser.getRole().name(),
                appUser.getProfileDescription()
        );
    }
}
