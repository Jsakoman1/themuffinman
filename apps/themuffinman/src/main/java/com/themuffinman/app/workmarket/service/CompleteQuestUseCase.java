package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import com.themuffinman.app.workmarket.model.QuestStatus;
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
