package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RejectQuestTermChangeUseCase {

    private final QuestExecutionPrimitiveService questExecutionPrimitiveService;
    private final QuestStateTransitionService questStateTransitionService;

    public Quest execute(Long questId, AppUser currentUser) {
        Quest quest = questExecutionPrimitiveService.resolveTargetForTermDecision(questId, currentUser);
        questStateTransitionService.rejectQuestTermChange(quest);
        Quest savedQuest = questExecutionPrimitiveService.persistMutation(quest);
        questExecutionPrimitiveService.emitApprovedApplicantNotification(
                savedQuest,
                currentUser,
                QuestNewsType.QUEST_TERM_REJECTED,
                "Quest time rejected",
                "The proposed time change for \"" + savedQuest.getTitle() + "\" was rejected.");
        questExecutionPrimitiveService.emitCreatorNotification(
                savedQuest,
                currentUser,
                QuestNewsType.QUEST_TERM_REJECTED,
                "Quest time rejected",
                "The proposed time change for \"" + savedQuest.getTitle() + "\" was rejected.");
        return savedQuest;
    }
}
