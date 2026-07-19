package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.WorkerReassignmentRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("workmarketWorkerManagementService")
@RequiredArgsConstructor
@Transactional
public class WorkmarketWorkerManagementService {

    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketQuestRepository questRepository;
    private final WorkmarketQuestApplicationWorkflowSupport workflowSupport;
    private final WorkmarketQuestWorkflowNotificationService notificationService;
    private final WorkmarketQuestApplicationReadService applicationReadService;

    public QuestApplicationResponseDTO releaseWorker(
            Long questId,
            Long workerApplicationId,
            AppUser currentUser
    ) {
        Quest quest = requireManageableWorkerQuest(questId, currentUser);
        QuestApplication worker = requireApprovedApplication(questId, workerApplicationId);
        worker.setStatus(QuestApplicationStatus.RELEASED);
        restoreOpenStatusWhenSlotIsAvailable(quest);
        questApplicationRepository.save(worker);
        questRepository.save(quest);
        notificationService.notifyWorkerReleased(quest, currentUser, worker);
        return applicationReadService.toApplicantResponse(worker);
    }

    public QuestApplicationResponseDTO replaceWorker(
            Long questId,
            Long workerApplicationId,
            WorkerReassignmentRequestDTO request,
            AppUser currentUser
    ) {
        if (request == null || request.getReplacementApplicationId() == null) {
            throw ServiceErrors.badRequest("Replacement application is required");
        }

        Quest quest = requireManageableWorkerQuest(questId, currentUser);
        if (workerApplicationId.equals(request.getReplacementApplicationId())) {
            throw ServiceErrors.badRequest("Replacement worker must be different from the current worker");
        }

        QuestApplication releasedWorker = requireApprovedApplication(questId, workerApplicationId);
        QuestApplication replacementWorker = questApplicationRepository
                .findForQuestApplicationDetailByStatus(
                        request.getReplacementApplicationId(), questId, QuestApplicationStatus.PENDING)
                .orElseThrow(() -> ServiceErrors.conflict(
                        "REPLACEMENT_APPLICATION_STALE",
                        "Replacement application changed; refresh the applications before trying again"));

        releasedWorker.setStatus(QuestApplicationStatus.RELEASED);
        replacementWorker.setStatus(QuestApplicationStatus.APPROVED);
        quest.setStatus(QuestStatus.ASSIGNED);
        questRepository.save(quest);
        questApplicationRepository.save(releasedWorker);
        QuestApplication savedReplacement = questApplicationRepository.save(replacementWorker);
        notificationService.notifyWorkerReassigned(quest, currentUser, releasedWorker, savedReplacement);
        return applicationReadService.toApplicantResponse(savedReplacement);
    }

    private Quest requireManageableWorkerQuest(Long questId, AppUser currentUser) {
        Quest quest = workflowSupport.requireQuest(questId);
        if (!workflowSupport.canManageWorkers(quest, currentUser)) {
            throw ServiceErrors.forbidden("Only the quest owner or an administrator can manage workers");
        }
        if (quest.getStatus() != QuestStatus.ASSIGNED
                && quest.getStatus() != QuestStatus.IN_PROGRESS
                && quest.getStatus() != QuestStatus.PAUSED) {
            throw ServiceErrors.conflict(
                    "QUEST_WORKER_STATE_STALE",
                    "Quest state changed; refresh the applications before managing workers");
        }
        return quest;
    }

    private QuestApplication requireApprovedApplication(Long questId, Long applicationId) {
        return questApplicationRepository.findForQuestApplicationDetailByStatus(
                        applicationId, questId, QuestApplicationStatus.APPROVED)
                .orElseThrow(() -> ServiceErrors.conflict(
                        "WORKER_ASSIGNMENT_STALE",
                        "Worker assignment changed; refresh the applications before trying again"));
    }

    private void restoreOpenStatusWhenSlotIsAvailable(Quest quest) {
        long approvedCount = questApplicationRepository.countByQuestIdAndStatus(
                quest.getId(), QuestApplicationStatus.APPROVED);
        int target = Math.max(quest.getAssigneeTarget() == null ? 1 : quest.getAssigneeTarget(), 1);
        if (approvedCount < target) {
            quest.setStatus(QuestStatus.OPEN);
        }
    }
}
