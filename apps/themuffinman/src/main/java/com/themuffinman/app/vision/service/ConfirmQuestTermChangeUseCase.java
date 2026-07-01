package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestNewsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfirmQuestTermChangeUseCase {

    private final QuestExecutionPrimitiveService questExecutionPrimitiveService;
    private final QuestStateTransitionService questStateTransitionService;

    public Quest execute(Long questId, AppUser currentUser) {
        Quest quest = questExecutionPrimitiveService.resolveTargetForTermDecision(questId, currentUser);
        questStateTransitionService.confirmQuestTermChange(quest);
        Quest savedQuest = questExecutionPrimitiveService.persistMutation(quest);
        questExecutionPrimitiveService.emitApprovedApplicantNotification(
                savedQuest,
                currentUser,
                QuestNewsType.QUEST_TERM_CONFIRMED,
                "Quest time confirmed",
                "The new time for \"" + savedQuest.getTitle() + "\" was confirmed.");
        questExecutionPrimitiveService.emitCreatorNotification(
                savedQuest,
                currentUser,
                QuestNewsType.QUEST_TERM_CONFIRMED,
                "Quest time confirmed",
                "The new time for \"" + savedQuest.getTitle() + "\" was confirmed.");
        return savedQuest;
    }
}
