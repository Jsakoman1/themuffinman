package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.event.WorkmarketQuestApplicationNewsEvent;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("workmarketWithdrawMyApplicationUseCase")
@RequiredArgsConstructor
public class WorkmarketWithdrawMyApplicationUseCase {

    private final WorkmarketQuestApplicationWorkflowSupport workflowSupport;
    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketApplicationNewsPublisher applicationNewsPublisher;

    public QuestApplication execute(Long questId, AppUser currentUser) {
        QuestApplication application = workflowSupport.requirePendingMyApplication(questId, currentUser);
        application.setStatus(QuestApplicationStatus.WITHDRAWN);
        QuestApplication savedApplication = questApplicationRepository.save(application);
        applicationNewsPublisher.publish(WorkmarketQuestApplicationNewsEvent.Type.WITHDRAWN, savedApplication, currentUser);
        return savedApplication;
    }
}
