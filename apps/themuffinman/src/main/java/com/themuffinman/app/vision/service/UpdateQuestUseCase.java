package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestRequestDTO;
import com.themuffinman.app.vision.model.Quest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateQuestUseCase {

    private final QuestExecutionPrimitiveService questExecutionPrimitiveService;
    private final QuestUpdateService questUpdateService;

    public Quest execute(Long questId, QuestRequestDTO dto, AppUser currentUser) {
        Quest quest = questExecutionPrimitiveService.resolveTargetForOwnerMutation(questId, currentUser);
        questUpdateService.applyQuestUpdates(quest, dto, currentUser);
        return questExecutionPrimitiveService.persistMutation(quest);
    }
}
