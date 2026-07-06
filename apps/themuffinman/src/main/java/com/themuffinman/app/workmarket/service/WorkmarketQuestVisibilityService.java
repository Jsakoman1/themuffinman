package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.service.CircleRelationshipReadService;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("workmarketQuestVisibilityService")
@RequiredArgsConstructor
public class WorkmarketQuestVisibilityService {

    private final CircleRelationshipReadService circleRelationshipReadService;
    private final WorkmarketQuestAccessPolicyService questAccessPolicyService;

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
                    .anyMatch(circleId -> circleRelationshipReadService.isCircleMember(circleId, currentUser.getId()));
        }

        return circleRelationshipReadService.isCircleBetween(currentUser, quest.getCreator());
    }

    public List<CircleGroup> getVisibleCircles(AppUser owner, List<Long> circleIds) {
        return circleRelationshipReadService.getOwnedCirclesByIds(owner, circleIds);
    }
}
