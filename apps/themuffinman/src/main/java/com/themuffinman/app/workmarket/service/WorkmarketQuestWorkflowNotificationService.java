package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("workmarketQuestWorkflowNotificationService")
@RequiredArgsConstructor
public class WorkmarketQuestWorkflowNotificationService {

    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketQuestNewsService questNewsService;

    public void notifyQuestDeleted(Quest quest, AppUser actor) {
        List<QuestApplication> applications = questApplicationRepository.findForQuestApplicationManagement(quest.getId());
        if (applications.isEmpty()) {
            return;
        }

        for (QuestApplication application : applications) {
            if (application.getStatus() == QuestApplicationStatus.WITHDRAWN) {
                continue;
            }

            questNewsService.notifyQuestDeleted(quest, actor, application.getApplicant());
        }
    }

    public void notifyQuestCancelled(Quest quest, AppUser actor) {
        notifyQuestApplicants(
                quest,
                actor,
                QuestNewsType.QUEST_CANCELLED,
                "Quest cancelled",
                "The quest \"" + quest.getTitle() + "\" was cancelled by its owner."
        );
    }

    public void notifyApprovedApplicant(Quest quest, AppUser actor, QuestNewsType type, String title, String message) {
        List<QuestApplication> approvedApplications = questApplicationRepository.findForQuestApplicationsByStatus(
                quest.getId(),
                QuestApplicationStatus.APPROVED
        );
        if (approvedApplications.isEmpty()) {
            return;
        }

        for (QuestApplication application : approvedApplications) {
            questNewsService.notifyQuestEvent(application.getApplicant(), actor, quest, null, type, title, message);
        }
    }

    public void notifyQuestCreator(Quest quest, AppUser actor, QuestNewsType type, String title, String message) {
        questNewsService.notifyQuestEvent(quest.getCreator(), actor, quest, null, type, title, message);
    }

    public void notifyQuestApplicants(Quest quest, AppUser actor, QuestNewsType type, String title, String message) {
        List<QuestApplication> applications = questApplicationRepository.findForQuestApplicationManagement(quest.getId());
        if (applications.isEmpty()) {
            return;
        }

        for (QuestApplication application : applications) {
            if (application.getStatus() == QuestApplicationStatus.WITHDRAWN) {
                continue;
            }

            questNewsService.notifyQuestEvent(application.getApplicant(), actor, quest, null, type, title, message);
        }
    }

    public void notifyWorkerReleased(Quest quest, AppUser actor, QuestApplication releasedApplication) {
        questNewsService.notifyQuestEvent(
                releasedApplication.getApplicant(),
                actor,
                quest,
                releasedApplication.getId(),
                QuestNewsType.QUEST_WORKER_RELEASED,
                "Worker assignment released",
                "Your worker assignment for \"" + quest.getTitle() + "\" was released by the quest owner."
        );
    }

    public void notifyWorkerReassigned(
            Quest quest,
            AppUser actor,
            QuestApplication releasedApplication,
            QuestApplication replacementApplication
    ) {
        notifyWorkerReleased(quest, actor, releasedApplication);
        questNewsService.notifyQuestEvent(
                replacementApplication.getApplicant(),
                actor,
                quest,
                replacementApplication.getId(),
                QuestNewsType.QUEST_WORKER_REASSIGNED,
                "Worker assignment approved",
                "You were assigned to the quest \"" + quest.getTitle() + "\" as a replacement worker."
        );
    }
}
