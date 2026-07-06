package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("workmarketDeleteQuestUseCase")
@RequiredArgsConstructor
public class WorkmarketDeleteQuestUseCase {

    private final WorkmarketQuestExecutionPrimitiveService questExecutionPrimitiveService;

    public void execute(Long questId, AppUser currentUser) {
        Quest quest = questExecutionPrimitiveService.resolveTargetForOwnerMutation(questId, currentUser);
        questExecutionPrimitiveService.deleteMutationData(quest, currentUser);
    }
}
