package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.location.model.QuestLocationVisibility;
import com.themuffinman.app.location.service.LocationSettingsService;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service("workmarketQuestUpdateService")
@RequiredArgsConstructor
public class WorkmarketQuestUpdateService {

    private final AppUserLookupService appUserLookupService;
    private final WorkmarketQuestValidationService questValidationService;
    private final WorkmarketQuestStateTransitionService questStateTransitionService;
    private final WorkmarketQuestAccessPolicyService questAccessPolicyService;
    private final LocationSettingsService locationSettingsService;
    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketQuestRepository questRepository;

    @Transactional
    public Quest reopenQuestForVision(Long questId, AppUser currentUser) {
        Quest quest = questRepository.findByIdWithCreator(questId)
                .orElseThrow(() -> com.themuffinman.app.common.errors.ServiceErrors.notFound("Quest not found with id " + questId));
        questStateTransitionService.applyOwnerQuestStatusChange(quest, QuestStatus.OPEN, currentUser);
        return quest;
    }

    @Transactional
    public Quest cancelQuestForVision(Long questId, AppUser currentUser) {
        Quest quest = questRepository.findByIdWithCreator(questId)
                .orElseThrow(() -> com.themuffinman.app.common.errors.ServiceErrors.notFound("Quest not found with id " + questId));
        questStateTransitionService.applyOwnerQuestStatusChange(quest, QuestStatus.CANCELLED, currentUser);
        return quest;
    }

    @Transactional
    public Quest pauseQuest(Long questId, AppUser currentUser) {
        Quest quest = questRepository.findByIdWithCreator(questId)
                .orElseThrow(() -> com.themuffinman.app.common.errors.ServiceErrors.notFound("Quest not found with id " + questId));
        questStateTransitionService.applyOwnerQuestStatusChange(quest, QuestStatus.PAUSED, currentUser);
        return quest;
    }

    @Transactional
    public Quest resumeQuest(Long questId, AppUser currentUser) {
        Quest quest = questRepository.findByIdWithCreator(questId)
                .orElseThrow(() -> com.themuffinman.app.common.errors.ServiceErrors.notFound("Quest not found with id " + questId));
        questStateTransitionService.applyOwnerQuestStatusChange(quest, QuestStatus.OPEN, currentUser);
        return quest;
    }

    @Transactional
    public Quest updateQuestTitleForVision(Long questId, String title, AppUser currentUser) {
        Quest quest = questRepository.findByIdWithCreator(questId)
                .orElseThrow(() -> com.themuffinman.app.common.errors.ServiceErrors.notFound("Quest not found with id " + questId));
        if (!questAccessPolicyService.canManageQuest(quest, currentUser)) {
            throw com.themuffinman.app.common.errors.ServiceErrors.forbidden("You can only edit your own quest");
        }
        String normalizedTitle = questValidationService.normalizeQuestText(title);
        if (normalizedTitle == null || normalizedTitle.isBlank()) {
            throw com.themuffinman.app.common.errors.ServiceErrors.badRequest("Quest title is required");
        }
        quest.setTitle(normalizedTitle);
        return quest;
    }

    public void applyQuestUpdates(Quest quest, QuestRequestDTO dto, AppUser currentUser) {
        questValidationService.validateUpdateRequest(dto);
        boolean becomingFree = dto.getAwardAmount() != null
                && dto.getAwardAmount().compareTo(BigDecimal.ZERO) == 0
                && (quest.getAwardAmount() == null || quest.getAwardAmount().compareTo(BigDecimal.ZERO) != 0);
        quest.setTitle(questValidationService.normalizeQuestText(dto.getTitle()));
        quest.setDescription(questValidationService.normalizeQuestRichText(dto.getDescription()));
        quest.setAwardAmount(dto.getAwardAmount());
        if (becomingFree) {
            clearApplicationPrices(quest.getId());
        }
        if (dto.getAssigneeTarget() != null) {
            quest.setAssigneeTarget(questValidationService.normalizeAssigneeTarget(dto.getAssigneeTarget()));
        }
        if (dto.getShowApprovedApplicants() != null) {
            quest.setShowApprovedApplicants(dto.getShowApprovedApplicants());
        }
        if (dto.getImages() != null) {
            quest.setImages(questValidationService.copyImages(dto.getImages()));
        }
        if (dto.getAudience() != null) {
            quest.setAudience(dto.getAudience());
        }
        questValidationService.applyQuestVisibilityCircles(quest, quest.getAudience(), dto.getSelectedCircleIds(), quest.getCreator());
        boolean termChanged = hasTermChanged(quest, dto);

        if (!questAccessPolicyService.isAdmin(currentUser)) {
            if (dto.getStatus() != null) {
                questStateTransitionService.applyOwnerQuestStatusChange(quest, dto.getStatus(), currentUser);
            }
            if (termChanged) {
                questStateTransitionService.applyOwnerTermUpdate(quest, dto, currentUser);
            }
            locationSettingsService.applyQuestLocation(quest, dto, quest.getCreator());
            return;
        }

        if (dto.getCreatorId() != null) {
            quest.setCreator(appUserLookupService.requireById(dto.getCreatorId(), "Creator not found with id " + dto.getCreatorId()));
            questValidationService.applyQuestVisibilityCircles(quest, quest.getAudience(), dto.getSelectedCircleIds(), quest.getCreator());
        }

        if (dto.getStatus() != null) {
            questStateTransitionService.applyAdminQuestStatusChange(quest, dto.getStatus(), currentUser);
        }

        if (termChanged) {
            questStateTransitionService.applyAdminTermUpdate(quest, dto);
        }

        locationSettingsService.applyQuestLocation(quest, dto, quest.getCreator());
    }

    private void clearApplicationPrices(Long questId) {
        questApplicationRepository.findForQuestApplicationManagement(questId).forEach(application -> application.setProposedPrice(null));
    }

    private boolean hasTermChanged(Quest quest, QuestRequestDTO dto) {
        if (dto.getScheduledAt() == null && dto.getEndsAt() == null && dto.getTermFixed() == null) {
            return false;
        }

        return !Objects.equals(dto.getScheduledAt(), quest.getScheduledAt())
                || !Objects.equals(dto.getEndsAt(), quest.getEndsAt())
                || !Objects.equals(dto.getTermFixed(), quest.isTermFixed());
    }
}
