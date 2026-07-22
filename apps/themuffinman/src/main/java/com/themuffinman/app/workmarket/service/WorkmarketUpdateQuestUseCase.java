package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("workmarketUpdateQuestUseCase")
@RequiredArgsConstructor
public class WorkmarketUpdateQuestUseCase {

    private final WorkmarketQuestExecutionPrimitiveService questExecutionPrimitiveService;
    private final WorkmarketQuestUpdateService questUpdateService;

    public Quest execute(Long questId, QuestRequestDTO dto, AppUser currentUser) {
        Quest quest = questExecutionPrimitiveService.resolveTargetForOwnerMutation(questId, currentUser);
        if (dto.getResourceVersion() == null) {
            throw com.themuffinman.app.common.errors.ServiceErrors.badRequest("RESOURCE_VERSION_REQUIRED", "Quest resource version is required for updates");
        }
        if (!dto.getResourceVersion().equals(quest.getVersion())) {
            throw com.themuffinman.app.common.errors.ServiceErrors.conflict("STALE_RESOURCE", "Quest has changed; reload before updating");
        }
        questUpdateService.applyQuestUpdates(quest, dto, currentUser);
        return questExecutionPrimitiveService.persistMutation(quest);
    }
}
