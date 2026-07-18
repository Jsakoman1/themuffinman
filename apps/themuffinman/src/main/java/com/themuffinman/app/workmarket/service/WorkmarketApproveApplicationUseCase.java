package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.event.DomainEventPublisher;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.event.WorkmarketQuestApplicationNewsEvent;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service("workmarketApproveApplicationUseCase")
@RequiredArgsConstructor
public class WorkmarketApproveApplicationUseCase {

    private final WorkmarketQuestApplicationWorkflowSupport workflowSupport;
    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketQuestRepository questRepository;
    private final DomainEventPublisher domainEventPublisher;

    public QuestApplication execute(Long questId, Long applicationId, AppUser currentUser) {
        Quest quest = workflowSupport.requireOpenQuest(questId);
        workflowSupport.validateQuestOwnerOrAdmin(quest, currentUser);

        QuestApplication application = workflowSupport.requirePendingApplication(questId, applicationId);
        int assigneeTarget = Math.max(quest.getAssigneeTarget() == null ? 1 : quest.getAssigneeTarget(), 1);
        long approvedCount = questApplicationRepository.countByQuestIdAndStatus(questId, QuestApplicationStatus.APPROVED);
        if (approvedCount >= assigneeTarget) {
            throw com.themuffinman.app.common.errors.ServiceErrors.badRequest("This quest already has all worker spots filled");
        }

        application.setStatus(QuestApplicationStatus.APPROVED);
        boolean filledAllSpots = approvedCount + 1 >= assigneeTarget;
        quest.setStatus(filledAllSpots ? QuestStatus.ASSIGNED : QuestStatus.OPEN);

        questRepository.save(quest);
        QuestApplication savedApplication = questApplicationRepository.save(application);
        domainEventPublisher.publish(new WorkmarketQuestApplicationNewsEvent(
                WorkmarketQuestApplicationNewsEvent.Type.APPROVED,
                quest,
                savedApplication,
                currentUser
        ));
        return savedApplication;
    }

}
