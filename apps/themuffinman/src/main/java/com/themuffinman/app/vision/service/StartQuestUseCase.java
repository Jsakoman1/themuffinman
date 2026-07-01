package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestNewsType;
import com.themuffinman.app.vision.model.QuestStatus;
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
