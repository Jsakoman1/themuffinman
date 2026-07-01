package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.event.DomainEventPublisher;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.event.QuestApplicationNewsEvent;
import com.themuffinman.app.vision.model.QuestApplication;
import com.themuffinman.app.vision.model.QuestApplicationStatus;
import com.themuffinman.app.vision.repository.QuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawMyApplicationUseCase {

    private final QuestApplicationWorkflowSupport workflowSupport;
    private final QuestApplicationRepository questApplicationRepository;
    private final DomainEventPublisher domainEventPublisher;

    public QuestApplication execute(Long questId, AppUser currentUser) {
        QuestApplication application = workflowSupport.requirePendingMyApplication(questId, currentUser);
        application.setStatus(QuestApplicationStatus.WITHDRAWN);
        QuestApplication savedApplication = questApplicationRepository.save(application);
        domainEventPublisher.publish(new QuestApplicationNewsEvent(QuestApplicationNewsEvent.Type.WITHDRAWN, savedApplication.getQuest(), savedApplication, currentUser));
        return savedApplication;
    }
}
