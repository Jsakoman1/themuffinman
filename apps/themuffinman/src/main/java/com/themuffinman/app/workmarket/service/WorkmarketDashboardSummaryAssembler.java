package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.concepts.ActorIdentity;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.DashboardSummaryDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

@Service("workmarketDashboardSummaryAssembler")
public class WorkmarketDashboardSummaryAssembler {

    public DashboardSummaryDTO buildSummary(
            AppUser currentUser,
            List<Quest> quests,
            List<QuestApplication> applications,
            long unreadNewsCount,
            long totalUserCount,
            long adminUserCount
    ) {
        if (currentUser == null) {
            return DashboardSummaryDTO.builder().build();
        }

        long questCount = quests.size();
        long visibleMyQuestsCount = countMyQuestsByStatus(quests, currentUser.getId(),
                status -> status == QuestStatus.OPEN || status == QuestStatus.WAITING_CONFIRMATION);
        long activeMyQuestsCount = countActiveMyQuests(quests, currentUser.getId());
        long completedMyQuestsCount = countMyQuestsByStatus(quests, currentUser.getId(), status -> status == QuestStatus.COMPLETED);
        long openQuestCount = countQuestsByStatus(quests, QuestStatus.OPEN);
        long assignedQuestCount = countQuestsByStatus(quests, QuestStatus.ASSIGNED);
        long waitingConfirmationQuestCount = countQuestsByStatus(quests, QuestStatus.WAITING_CONFIRMATION);
        long pendingWorkApplicationsCount = countApplicationsByStatus(applications, QuestApplicationStatus.PENDING);
        long activeWorkApplicationsCount = countActiveWorkApplications(applications);
        long activeWorkCount = activeMyQuestsCount + activeWorkApplicationsCount;

        return DashboardSummaryDTO.builder()
                .adminModeEnabled(ActorIdentity.from(currentUser).admin())
                .questCount(questCount)
                .visibleMyQuestsCount(visibleMyQuestsCount)
                .pendingWorkApplicationsCount(pendingWorkApplicationsCount)
                .activeWorkApplicationsCount(activeWorkApplicationsCount)
                .activeMyQuestsCount(activeMyQuestsCount)
                .activeWorkCount(activeWorkCount)
                .completedMyQuestsCount(completedMyQuestsCount)
                .openQuestCount(openQuestCount)
                .assignedQuestCount(assignedQuestCount)
                .waitingConfirmationQuestCount(waitingConfirmationQuestCount)
                .unreadNewsCount(unreadNewsCount)
                .totalUserCount(totalUserCount)
                .adminUserCount(adminUserCount)
                .build();
    }

    private long countMyQuestsByStatus(
            List<Quest> quests,
            Long currentUserId,
            Predicate<QuestStatus> statusPredicate
    ) {
        return quests.stream()
                .filter(quest -> quest.getCreator() != null && quest.getCreator().getId().equals(currentUserId))
                .filter(quest -> statusPredicate.test(quest.getStatus()))
                .count();
    }

    private long countActiveMyQuests(List<Quest> quests, Long currentUserId) {
        return quests.stream()
                .filter(quest -> quest.getCreator() != null && quest.getCreator().getId().equals(currentUserId))
                .filter(quest -> quest.getStatus() == QuestStatus.ASSIGNED || quest.getStatus() == QuestStatus.IN_PROGRESS)
                .count();
    }

    private long countQuestsByStatus(List<Quest> quests, QuestStatus status) {
        return quests.stream()
                .filter(quest -> quest.getStatus() == status)
                .count();
    }

    private long countApplicationsByStatus(List<QuestApplication> applications, QuestApplicationStatus status) {
        return applications.stream()
                .filter(application -> application.getStatus() == status)
                .count();
    }

    private long countActiveWorkApplications(List<QuestApplication> applications) {
        return applications.stream()
                .filter(application -> application.getStatus() == QuestApplicationStatus.APPROVED)
                .filter(application -> {
                    QuestStatus questStatus = application.getQuest() == null ? null : application.getQuest().getStatus();
                    return questStatus == QuestStatus.ASSIGNED
                            || questStatus == QuestStatus.IN_PROGRESS
                            || questStatus == QuestStatus.WAITING_CONFIRMATION;
                })
                .count();
    }
}
