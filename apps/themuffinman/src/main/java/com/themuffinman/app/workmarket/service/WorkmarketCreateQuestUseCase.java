package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestMgr;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("workmarketCreateQuestUseCase")
@RequiredArgsConstructor
public class WorkmarketCreateQuestUseCase {

    private final WorkmarketQuestValidationService questValidationService;
    private final WorkmarketQuestStateTransitionService questStateTransitionService;
    private final WorkmarketQuestExecutionPrimitiveService questExecutionPrimitiveService;
    private final WorkmarketQuestMgr questMgr;
    private final com.themuffinman.app.location.service.LocationSettingsService locationSettingsService;

    public Quest execute(QuestRequestDTO dto, AppUser currentUser) {
        // Guided Web intake ends at this canonical use case; the client may present
        // one field at a time, but quest rules remain centralized here.
        questValidationService.validateCreateRequest(dto);
        Quest quest = questMgr.toEntity(dto, questExecutionPrimitiveService.resolveCreator(dto, currentUser));
        if (quest.getAudience() == null) {
            quest.setAudience(QuestAudience.CIRCLES);
        }
        questValidationService.applyQuestVisibilityCircles(
                quest,
                dto.getAudience() == null ? QuestAudience.CIRCLES : dto.getAudience(),
                dto.getSelectedCircleIds(),
                quest.getCreator()
        );
        quest.setAssigneeTarget(questValidationService.normalizeAssigneeTarget(dto.getAssigneeTarget()));
        questStateTransitionService.applyConfirmedQuestTermFields(quest, dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
        locationSettingsService.applyQuestLocation(quest, dto, quest.getCreator());
        return questExecutionPrimitiveService.persistMutation(quest);
    }
}
