package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
class WorkmarketQuestSearchScopeService {

    private final WorkmarketQuestRepository questRepository;

    List<Quest> loadQuestSearchScope(AppUser currentUser, Integer radiusKm) {
        if (radiusKm == null) {
            return questRepository.findForQuestList();
        }

        if (currentUser == null || currentUser.getLocationLatitude() == null || currentUser.getLocationLongitude() == null) {
            return List.of();
        }

        List<Long> nearbyQuestIds = questRepository.findIdsWithinRadius(
                currentUser.getLocationLatitude(),
                currentUser.getLocationLongitude(),
                radiusKm * 1000
        );
        if (nearbyQuestIds.isEmpty()) {
            return List.of();
        }

        return questRepository.findForQuestListByIds(nearbyQuestIds);
    }
}
