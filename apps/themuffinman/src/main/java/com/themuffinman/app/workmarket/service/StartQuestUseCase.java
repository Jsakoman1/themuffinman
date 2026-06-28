package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import com.themuffinman.app.workmarket.model.QuestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StartQuestUseCase {

    private final QuestExecutionPrimitiveService questExecutionPrimitiveService;

    public Quest execute(Long questId, AppUser currentUser) {
        Quest quest = questExecutionPrimitiveService.resolveTargetForExecutionMutation(questId, currentUser);
        questExecutionPrimitiveService.validateState(quest, QuestStatus.ASSIGNED, "Quest can only be started after an application is approved");
        quest.setStatus(QuestStatus.IN_PROGRESS);
        Quest savedQuest = questExecutionPrimitiveService.persistMutation(quest);
        questExecutionPrimitiveService.emitApprovedApplicantNotification(
                savedQuest,
                currentUser,
                QuestNewsType.QUEST_STARTED,
                "Quest started",
                "The quest \"" + savedQuest.getTitle() + "\" has started.");
        return savedQuest;
    }
}
