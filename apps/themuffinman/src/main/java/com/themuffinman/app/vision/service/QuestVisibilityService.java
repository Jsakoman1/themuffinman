package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.service.CircleService;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestAudience;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestVisibilityService {

    private final CircleService circleService;
    private final QuestAccessPolicyService questAccessPolicyService;

    public boolean canViewQuest(AppUser currentUser, Quest quest) {
        if (currentUser == null || quest == null) {
            return false;
        }

        if (questAccessPolicyService.canManageQuest(quest, currentUser)) {
            return true;
        }

        if (quest.getAudience() == QuestAudience.EVERYONE) {
            return true;
        }

        if (quest.getVisibleToCircles() != null && !quest.getVisibleToCircles().isEmpty()) {
            return quest.getVisibleToCircles().stream()
                    .map(CircleGroup::getId)
                    .anyMatch(circleId -> circleService.isCircleMember(circleId, currentUser.getId()));
        }

        return circleService.isCircleBetween(currentUser, quest.getCreator());
    }

    public List<CircleGroup> getVisibleCircles(AppUser owner, List<Long> circleIds) {
        return circleService.getOwnedCirclesByIds(owner, circleIds);
    }

}
