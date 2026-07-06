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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        List<QuestApplication> declinedApplications = filledAllSpots
                ? declineOtherPendingApplications(questId, applicationId)
                : List.of();
        questRepository.save(quest);
        QuestApplication savedApplication = questApplicationRepository.save(application);
        for (QuestApplication declinedApplication : declinedApplications) {
            domainEventPublisher.publish(new WorkmarketQuestApplicationNewsEvent(
                    WorkmarketQuestApplicationNewsEvent.Type.DECLINED,
                    quest,
                    declinedApplication,
                    currentUser
            ));
        }
        domainEventPublisher.publish(new WorkmarketQuestApplicationNewsEvent(
                WorkmarketQuestApplicationNewsEvent.Type.APPROVED,
                quest,
                savedApplication,
                currentUser
        ));
        return savedApplication;
    }

    private List<QuestApplication> declineOtherPendingApplications(Long questId, Long approvedApplicationId) {
        List<QuestApplication> pendingApplications = questApplicationRepository.findForQuestApplicationsByStatus(questId, QuestApplicationStatus.PENDING);
        if (pendingApplications.isEmpty()) {
            return List.of();
        }

        List<QuestApplication> declinedApplications = new ArrayList<>();
        for (QuestApplication application : pendingApplications) {
            if (!Objects.equals(application.getId(), approvedApplicationId)) {
                application.setStatus(QuestApplicationStatus.DECLINED);
                declinedApplications.add(application);
            }
        }

        if (!declinedApplications.isEmpty()) {
            questApplicationRepository.saveAll(declinedApplications);
        }

        return declinedApplications;
    }
}
