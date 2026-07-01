package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.service.LocationSettingsService;
import com.themuffinman.app.vision.dto.QuestRequestDTO;
import com.themuffinman.app.vision.mapper.QuestMgr;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestAudience;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateQuestUseCase {

    private final QuestValidationService questValidationService;
    private final QuestStateTransitionService questStateTransitionService;
    private final QuestExecutionPrimitiveService questExecutionPrimitiveService;
    private final QuestMgr questMgr;
    private final LocationSettingsService locationSettingsService;

    public Quest execute(QuestRequestDTO dto, AppUser currentUser) {
        questValidationService.validateCreateRequest(dto);
        Quest quest = questMgr.toEntity(dto, questExecutionPrimitiveService.resolveCreator(dto, currentUser));
        if (quest.getAudience() == null) {
            quest.setAudience(QuestAudience.CIRCLES);
        }
        questValidationService.applyQuestVisibilityCircles(quest, dto.getAudience(), dto.getSelectedCircleIds(), quest.getCreator());
        quest.setAssigneeTarget(questValidationService.normalizeAssigneeTarget(dto.getAssigneeTarget()));
        questStateTransitionService.applyConfirmedQuestTermFields(quest, dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
        locationSettingsService.applyQuestLocation(quest, dto, quest.getCreator());
        return questExecutionPrimitiveService.persistMutation(quest);
    }
}
