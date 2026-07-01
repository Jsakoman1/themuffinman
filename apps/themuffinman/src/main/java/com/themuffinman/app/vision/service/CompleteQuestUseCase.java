package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestNewsType;
import com.themuffinman.app.vision.model.QuestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompleteQuestUseCase {

    private final QuestExecutionPrimitiveService questExecutionPrimitiveService;

    public Quest execute(Long questId, AppUser currentUser) {
        Quest quest = questExecutionPrimitiveService.resolveTargetForExecutionMutation(questId, currentUser);
        questExecutionPrimitiveService.validateState(quest, QuestStatus.IN_PROGRESS, "Quest can only be completed while it is in progress");
        quest.setStatus(QuestStatus.COMPLETED);
        Quest savedQuest = questExecutionPrimitiveService.persistMutation(quest);
        questExecutionPrimitiveService.emitApprovedApplicantNotification(
                savedQuest,
                currentUser,
                QuestNewsType.QUEST_COMPLETED,
                "Quest completed",
                "The quest \"" + savedQuest.getTitle() + "\" has been completed.");
        return savedQuest;
    }
}
