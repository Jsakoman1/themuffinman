package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestListPresetDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkmarketQuestListPresetResolver {

    private final WorkmarketQuestVisibilityService questVisibilityService;
    private final WorkmarketQuestAccessPolicyService questAccessPolicyService;

    public List<Quest> resolve(QuestListPresetDTO preset, List<Quest> scopedQuests, AppUser currentUser) {
        return switch (preset) {
            case AVAILABLE -> scopedQuests.stream()
                    .filter(quest -> questVisibilityService.canViewQuest(currentUser, quest))
                    .filter(quest -> quest.getStatus() == QuestStatus.OPEN)
                    .filter(quest -> currentUser == null || !questAccessPolicyService.isQuestOwner(quest, currentUser))
                    .toList();
            case MY_VISIBLE -> scopedQuests.stream()
                    .filter(quest -> questVisibilityService.canViewQuest(currentUser, quest))
                    .filter(quest -> questAccessPolicyService.isQuestOwner(quest, currentUser))
                    .filter(quest -> quest.getStatus() == QuestStatus.OPEN
                            || quest.getStatus() == QuestStatus.ASSIGNED
                            || quest.getStatus() == QuestStatus.IN_PROGRESS
                            || quest.getStatus() == QuestStatus.WAITING_CONFIRMATION)
                    .toList();
            case MY_ACTIVE -> scopedQuests.stream()
                    .filter(quest -> questVisibilityService.canViewQuest(currentUser, quest))
                    .filter(quest -> questAccessPolicyService.isQuestOwner(quest, currentUser))
                    .filter(quest -> quest.getStatus() == QuestStatus.ASSIGNED
                            || quest.getStatus() == QuestStatus.IN_PROGRESS
                            || quest.getStatus() == QuestStatus.WAITING_CONFIRMATION)
                    .toList();
        };
    }
}
