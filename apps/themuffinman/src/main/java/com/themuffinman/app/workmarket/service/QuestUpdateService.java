package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import org.springframework.stereotype.Service;

@Service
public class QuestUpdateService {

    private final AppUserLookupService appUserLookupService;
    private final QuestValidationService questValidationService;
    private final QuestStateTransitionService questStateTransitionService;
    private final QuestAccessPolicyService questAccessPolicyService;

    public QuestUpdateService(
            AppUserLookupService appUserLookupService,
            QuestValidationService questValidationService,
            QuestStateTransitionService questStateTransitionService,
            QuestAccessPolicyService questAccessPolicyService
    ) {
        this.appUserLookupService = appUserLookupService;
        this.questValidationService = questValidationService;
        this.questStateTransitionService = questStateTransitionService;
        this.questAccessPolicyService = questAccessPolicyService;
    }

    public void applyQuestUpdates(Quest quest, QuestRequestDTO dto, AppUser currentUser) {
        questValidationService.validateUpdateRequest(dto);
        quest.setTitle(questValidationService.normalizeQuestText(dto.getTitle()));
        quest.setDescription(questValidationService.normalizeQuestText(dto.getDescription()));
        quest.setAwardAmount(dto.getAwardAmount());
        if (dto.getAssigneeTarget() != null) {
            quest.setAssigneeTarget(questValidationService.normalizeAssigneeTarget(dto.getAssigneeTarget()));
        }
        if (dto.getImages() != null) {
            quest.setImages(questValidationService.copyImages(dto.getImages()));
        }
        if (dto.getAudience() != null) {
            quest.setAudience(dto.getAudience());
        }
        questValidationService.applyQuestVisibilityCircles(quest, quest.getAudience(), dto.getSelectedCircleIds(), quest.getCreator());

        if (!questAccessPolicyService.isAdmin(currentUser)) {
            questStateTransitionService.applyOwnerTermUpdate(quest, dto, currentUser);
            return;
        }

        if (dto.getCreatorId() != null) {
            quest.setCreator(appUserLookupService.requireById(dto.getCreatorId(), "Creator not found with id " + dto.getCreatorId()));
            questValidationService.applyQuestVisibilityCircles(quest, quest.getAudience(), dto.getSelectedCircleIds(), quest.getCreator());
        }

        if (dto.getStatus() != null) {
            questStateTransitionService.applyAdminQuestStatusChange(quest, dto.getStatus(), currentUser);
        }

        if (dto.getScheduledAt() != null || dto.getEndsAt() != null || dto.getTermFixed() != null) {
            questStateTransitionService.applyAdminTermUpdate(quest, dto);
        }
    }
}
