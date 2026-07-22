package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.event.WorkmarketQuestApplicationNewsEvent;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("workmarketDeclineApplicationUseCase")
@RequiredArgsConstructor
public class WorkmarketDeclineApplicationUseCase {

    private final WorkmarketQuestApplicationWorkflowSupport workflowSupport;
    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketApplicationNewsPublisher applicationNewsPublisher;

    public QuestApplication execute(Long questId, Long applicationId, AppUser currentUser) {
        Quest quest = workflowSupport.requireOpenQuest(questId);
        workflowSupport.validateQuestOwnerOrAdmin(quest, currentUser);

        QuestApplication application = workflowSupport.requirePendingApplication(questId, applicationId);
        application.setStatus(QuestApplicationStatus.DECLINED);
        QuestApplication savedApplication = questApplicationRepository.save(application);
        applicationNewsPublisher.publish(WorkmarketQuestApplicationNewsEvent.Type.DECLINED, savedApplication, currentUser);
        return savedApplication;
    }
}
