package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.Quest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteQuestUseCase {

    private final QuestExecutionPrimitiveService questExecutionPrimitiveService;

    public void execute(Long questId, AppUser currentUser) {
        Quest quest = questExecutionPrimitiveService.resolveTargetForOwnerMutation(questId, currentUser);
        questExecutionPrimitiveService.deleteMutationData(quest, currentUser);
    }
}
