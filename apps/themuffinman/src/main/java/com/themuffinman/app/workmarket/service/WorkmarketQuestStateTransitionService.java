package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service("workmarketQuestStateTransitionService")
@RequiredArgsConstructor
public class WorkmarketQuestStateTransitionService {

    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketQuestValidationService questValidationService;
    private final WorkmarketQuestWorkflowNotificationService questWorkflowNotificationService;
    private final WorkmarketQuestAccessPolicyService questAccessPolicyService;

    public void requireQuestStatus(Quest quest, QuestStatus requiredStatus, String message) {
        if (quest.getStatus() != requiredStatus) {
            throw ServiceErrors.badRequest(message);
        }
    }

    public void validateQuestExecutionAuthority(Quest quest, AppUser currentUser) {
        boolean hasApprovedApplication = hasApprovedApplication(quest, currentUser);

        if (questAccessPolicyService.canExecuteQuest(quest, currentUser, hasApprovedApplication)) {
            return;
        }

        throw ServiceErrors.forbidden("You are not allowed to manage this quest");
    }

    public void validateQuestTermDecisionAuthority(Quest quest, AppUser currentUser) {
        boolean hasApprovedApplication = hasApprovedApplication(quest, currentUser);

        if (!questAccessPolicyService.canDecideQuestTermChange(quest, currentUser, hasApprovedApplication)) {
            throw ServiceErrors.forbidden("You are not allowed to confirm this quest term change");
        }
    }

    public void applyAdminQuestStatusChange(Quest quest, QuestStatus newStatus, AppUser actor) {
        QuestStatus previousStatus = quest.getStatus();
        quest.setStatus(newStatus);

        if (newStatus != QuestStatus.WAITING_CONFIRMATION) {
            clearPendingQuestTermChange(quest);
        }

        if (previousStatus != QuestStatus.OPEN && newStatus == QuestStatus.OPEN) {
            quest.setReopenedAt(Instant.now());
            reopenQuestApplications(quest);
            questWorkflowNotificationService.notifyQuestApplicants(
                    quest,
                    actor,
                    QuestNewsType.QUEST_REOPENED,
                    "Quest reopened",
                    "The quest \"" + quest.getTitle() + "\" was reopened."
            );
        }
    }

    public void applyAdminTermUpdate(Quest quest, QuestRequestDTO dto) {
        questValidationService.validateTermInput(dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
        applyConfirmedQuestTermFields(quest, dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());

        if (quest.getStatus() == QuestStatus.WAITING_CONFIRMATION
                && (dto.getStatus() == null || dto.getStatus() == QuestStatus.WAITING_CONFIRMATION)) {
            restoreQuestStatusAfterTermDecision(quest);
        }

        clearPendingQuestTermChange(quest);
    }

    public void applyOwnerTermUpdate(Quest quest, QuestRequestDTO dto, AppUser currentUser) {
        if (dto.getScheduledAt() == null && dto.getEndsAt() == null && dto.getTermFixed() == null) {
            return;
        }

        questValidationService.validateTermInput(dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());

        if (quest.getStatus() == QuestStatus.OPEN || quest.getStatus() == QuestStatus.CANCELLED) {
            applyConfirmedQuestTermFields(quest, dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
            clearPendingQuestTermChange(quest);
            return;
        }

        if (quest.getStatus() == QuestStatus.WAITING_CONFIRMATION) {
            applyPendingQuestTermChange(quest, dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
            questWorkflowNotificationService.notifyApprovedApplicant(
                    quest,
                    currentUser,
                    QuestNewsType.QUEST_TERM_CONFIRMATION_REQUESTED,
                    "Term change updated",
                    "The pending time for \"" + quest.getTitle() + "\" was updated."
            );
            return;
        }

        if (quest.getStatus() == QuestStatus.ASSIGNED || quest.getStatus() == QuestStatus.IN_PROGRESS) {
            queueQuestTermChange(quest, dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
            questWorkflowNotificationService.notifyApprovedApplicant(
                    quest,
                    currentUser,
                    QuestNewsType.QUEST_TERM_CONFIRMATION_REQUESTED,
                    "Term confirmation needed",
                    "The owner requested a new time for \"" + quest.getTitle() + "\"."
            );
            return;
        }

        throw ServiceErrors.badRequest("Term can only be changed on an active quest");
    }

    public void applyOwnerQuestStatusChange(Quest quest, QuestStatus targetStatus, AppUser currentUser) {
        if (targetStatus == null || targetStatus == quest.getStatus()) {
            return;
        }

        if (!questAccessPolicyService.isQuestOwner(quest, currentUser)) {
            throw ServiceErrors.forbidden("Only the quest owner can change this quest status");
        }

        if (targetStatus == QuestStatus.ASSIGNED) {
            long approvedCount = questApplicationRepository.countByQuestIdAndStatus(quest.getId(), QuestApplicationStatus.APPROVED);
            if (approvedCount < 1) {
                throw ServiceErrors.badRequest("Approve at least one applicant before assigning this quest");
            }

            if (quest.getStatus() != QuestStatus.OPEN) {
                throw ServiceErrors.badRequest("Only open quests can be marked as assigned");
            }

            quest.setStatus(QuestStatus.ASSIGNED);
            return;
        }

        if (targetStatus == QuestStatus.OPEN) {
            if (quest.getStatus() != QuestStatus.ASSIGNED) {
                throw ServiceErrors.badRequest("Only assigned quests can be reopened here");
            }

            quest.setStatus(QuestStatus.OPEN);
            return;
        }

        throw ServiceErrors.badRequest("Owners can only switch between open and assigned here");
    }

    public void confirmQuestTermChange(Quest quest) {
        if (quest.getStatus() != QuestStatus.WAITING_CONFIRMATION) {
            throw ServiceErrors.badRequest("Quest term change is not waiting for confirmation");
        }

        if (quest.getPendingTermFixed() == null && quest.getPendingScheduledAt() == null && quest.getPendingEndsAt() == null) {
            throw ServiceErrors.badRequest("No pending term change to confirm");
        }

        quest.setScheduledAt(quest.getPendingScheduledAt());
        quest.setEndsAt(quest.getPendingEndsAt());
        quest.setTermFixed(Boolean.TRUE.equals(quest.getPendingTermFixed()));
        restoreQuestStatusAfterTermDecision(quest);
        clearPendingQuestTermChange(quest);
    }

    public void rejectQuestTermChange(Quest quest) {
        if (quest.getStatus() != QuestStatus.WAITING_CONFIRMATION) {
            throw ServiceErrors.badRequest("Quest term change is not waiting for confirmation");
        }

        restoreQuestStatusAfterTermDecision(quest);
        clearPendingQuestTermChange(quest);
    }

    public void applyConfirmedQuestTermFields(Quest quest, java.time.Instant scheduledAt, java.time.Instant endsAt, Boolean termFixed) {
        quest.setScheduledAt(scheduledAt);
        quest.setEndsAt(endsAt);
        quest.setTermFixed(Boolean.TRUE.equals(termFixed));
    }

    private void applyPendingQuestTermChange(Quest quest, Instant scheduledAt, Instant endsAt, Boolean termFixed) {
        quest.setPendingScheduledAt(scheduledAt);
        quest.setPendingEndsAt(endsAt);
        quest.setPendingTermFixed(termFixed);
    }

    private void queueQuestTermChange(Quest quest, Instant scheduledAt, Instant endsAt, Boolean termFixed) {
        applyPendingQuestTermChange(quest, scheduledAt, endsAt, termFixed);
        quest.setTermChangePreviousStatus(quest.getStatus());
        quest.setStatus(QuestStatus.WAITING_CONFIRMATION);
    }

    private void clearPendingQuestTermChange(Quest quest) {
        quest.setPendingScheduledAt(null);
        quest.setPendingEndsAt(null);
        quest.setPendingTermFixed(null);
        quest.setTermChangePreviousStatus(null);
    }

    private void restoreQuestStatusAfterTermDecision(Quest quest) {
        if (quest.getTermChangePreviousStatus() == null) {
            throw ServiceErrors.badRequest("Missing previous quest status for term change");
        }

        quest.setStatus(quest.getTermChangePreviousStatus());
    }

    private void reopenQuestApplications(Quest quest) {
        List<QuestApplication> reopenedApplications = new ArrayList<>();
        List<QuestApplication> applications = questApplicationRepository.findForQuestApplicationManagement(quest.getId());
        if (applications.isEmpty()) {
            return;
        }

        for (QuestApplication application : applications) {
            if (application.getStatus() == QuestApplicationStatus.WITHDRAWN) {
                continue;
            }

            application.setStatus(QuestApplicationStatus.PENDING);
            reopenedApplications.add(application);
        }

        if (!reopenedApplications.isEmpty()) {
            questApplicationRepository.saveAll(reopenedApplications);
        }
    }

    private boolean hasApprovedApplication(Quest quest, AppUser currentUser) {
        return currentUser != null && questApplicationRepository.findForViewerApplicationWithStatus(
                quest.getId(),
                currentUser.getId(),
                QuestApplicationStatus.APPROVED
        ).isPresent();
    }
}
