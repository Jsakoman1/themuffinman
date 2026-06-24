package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import com.themuffinman.app.workmarket.repository.QuestApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestWorkflowNotificationService {

    private final QuestApplicationRepository questApplicationRepository;
    private final QuestNewsService questNewsService;

    public QuestWorkflowNotificationService(
            QuestApplicationRepository questApplicationRepository,
            QuestNewsService questNewsService
    ) {
        this.questApplicationRepository = questApplicationRepository;
        this.questNewsService = questNewsService;
    }

    public void notifyQuestDeleted(Quest quest, AppUser actor) {
        List<QuestApplication> applications = questApplicationRepository.findByQuestId(quest.getId());
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

    public void notifyApprovedApplicant(Quest quest, AppUser actor, QuestNewsType type, String title, String message) {
        List<QuestApplication> approvedApplications = questApplicationRepository.findByQuestIdAndStatus(
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
        List<QuestApplication> applications = questApplicationRepository.findByQuestId(quest.getId());
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
}
