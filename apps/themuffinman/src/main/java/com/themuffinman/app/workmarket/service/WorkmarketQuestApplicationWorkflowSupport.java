package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("workmarketQuestApplicationWorkflowSupport")
@RequiredArgsConstructor
public class WorkmarketQuestApplicationWorkflowSupport {

    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketQuestRepository questRepository;
    private final WorkmarketQuestVisibilityService questVisibilityService;
    private final WorkmarketQuestAccessPolicyService questAccessPolicyService;

    public Quest requireQuest(Long questId) {
        return questRepository.findForQuestDetail(questId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest not found with id " + questId));
    }

    public Quest requireOpenQuest(Long questId) {
        Quest quest = requireQuest(questId);
        validateQuestIsOpen(quest);
        return quest;
    }

    public Quest requireVisibleOpenQuest(Long questId, AppUser currentUser) {
        Quest quest = requireQuest(questId);
        if (!questVisibilityService.canViewQuest(currentUser, quest)) {
            throw ServiceErrors.notFound("Quest not found with id " + questId);
        }

        validateQuestIsOpen(quest);
        return quest;
    }

    public QuestApplication requirePendingApplication(Long questId, Long applicationId) {
        QuestApplication application = questApplicationRepository.findForQuestApplicationDetail(applicationId, questId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest application not found with id " + applicationId));

        if (application.getStatus() != QuestApplicationStatus.PENDING) {
            throw ServiceErrors.badRequest("Only pending applications can be modified");
        }

        return application;
    }

    public QuestApplication requirePendingMyApplication(Long questId, AppUser currentUser) {
        QuestApplication application = questApplicationRepository.findForViewerApplication(questId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Quest application not found for current user"));

        if (application.getStatus() != QuestApplicationStatus.PENDING) {
            throw ServiceErrors.badRequest("Only pending applications can be modified");
        }

        return application;
    }

    public void validateNotQuestCreator(Quest quest, AppUser currentUser) {
        if (quest.getCreator().getId().equals(currentUser.getId())) {
            throw ServiceErrors.badRequest("Quest creator cannot apply to their own quest");
        }
    }

    public void validateNoDuplicateApplication(Long questId, Long applicantId) {
        if (questApplicationRepository.existsByQuestIdAndApplicantId(questId, applicantId)) {
            throw ServiceErrors.conflict("You have already applied for this quest");
        }
    }

    public void validateApplicationInput(QuestApplicationRequestDTO dto, Quest quest) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Quest application request is required");
        }

        if (!RichTextInputValidator.hasContent(dto.getMessage())) {
            throw ServiceErrors.badRequest("Application message is required");
        }

        if (isFreeQuest(quest)) {
            if (dto.getProposedPrice() != null) {
                throw ServiceErrors.badRequest("Free quest applications cannot have a proposed price");
            }
            return;
        }

        if (dto.getProposedPrice() == null) {
            throw ServiceErrors.badRequest("Proposed price is required for paid quests");
        }

        if (dto.getProposedPrice().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            throw ServiceErrors.badRequest("Proposed price must be at least 0.01");
        }
    }

    public boolean isFreeQuest(Quest quest) {
        return quest.getAwardAmount() != null && quest.getAwardAmount().compareTo(BigDecimal.ZERO) == 0;
    }

    public void validateQuestOwnerOrAdmin(Quest quest, AppUser currentUser) {
        if (!questAccessPolicyService.canManageQuestApplications(quest, currentUser)) {
            throw ServiceErrors.forbidden("You are not allowed to view these applications");
        }
    }

    public void validateAdmin(AppUser currentUser) {
        if (!questAccessPolicyService.isAdmin(currentUser)) {
            throw ServiceErrors.forbidden("Admin access is required");
        }
    }

    private void validateQuestIsOpen(Quest quest) {
        if (quest.getStatus() != QuestStatus.OPEN) {
            throw ServiceErrors.badRequest("Applications are only allowed for open quests");
        }
    }
}
