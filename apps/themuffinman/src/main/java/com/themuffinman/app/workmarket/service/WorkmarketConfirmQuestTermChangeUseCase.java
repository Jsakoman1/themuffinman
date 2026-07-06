package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("workmarketConfirmQuestTermChangeUseCase")
@RequiredArgsConstructor
public class WorkmarketConfirmQuestTermChangeUseCase {

    private final WorkmarketQuestExecutionPrimitiveService questExecutionPrimitiveService;
    private final WorkmarketQuestStateTransitionService questStateTransitionService;

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
