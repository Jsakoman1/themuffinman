package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.event.DomainEventPublisher;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.event.QuestApplicationNewsEvent;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.repository.QuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeclineApplicationUseCase {

    private final QuestApplicationWorkflowSupport workflowSupport;
    private final QuestApplicationRepository questApplicationRepository;
    private final DomainEventPublisher domainEventPublisher;

    public QuestApplication execute(Long questId, Long applicationId, AppUser currentUser) {
        Quest quest = workflowSupport.requireOpenQuest(questId);
        workflowSupport.validateQuestOwnerOrAdmin(quest, currentUser);

        QuestApplication application = workflowSupport.requirePendingApplication(questId, applicationId);
        application.setStatus(QuestApplicationStatus.DECLINED);
        QuestApplication savedApplication = questApplicationRepository.save(application);
        domainEventPublisher.publish(new QuestApplicationNewsEvent(QuestApplicationNewsEvent.Type.DECLINED, quest, savedApplication, currentUser));
        return savedApplication;
    }
}
