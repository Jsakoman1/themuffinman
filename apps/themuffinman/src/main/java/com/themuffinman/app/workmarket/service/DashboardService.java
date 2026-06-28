package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.workmarket.mapper.QuestNewsMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.workmarket.dto.DashboardResponseDTO;
import com.themuffinman.app.workmarket.dto.DashboardSectionsDTO;
import com.themuffinman.app.workmarket.dto.DashboardSummaryDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserService;
import com.themuffinman.app.social.service.CircleService;
import com.themuffinman.app.workmarket.repository.QuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final QuestService questService;
    private final QuestApplicationRepository questApplicationRepository;
    private final QuestNewsService questNewsService;
    private final AppUserRepository appUserRepository;
    private final CircleService circleService;
    private final AppUserService appUserService;
    private final QuestApplicationService questApplicationService;
    private final QuestNewsMgr questNewsMgr;
    private final AppUserMgr appUserMgr;
    private final WorkmarketOptionsService workmarketOptionsService;
    private final DashboardSectionsFactory dashboardSectionsFactory;

    private static final Map<QuestStatus, Integer> QUEST_STATUS_SORT_ORDER = Map.of(
            QuestStatus.OPEN, 0,
            QuestStatus.ASSIGNED, 1,
            QuestStatus.WAITING_CONFIRMATION, 2,
            QuestStatus.IN_PROGRESS, 3,
            QuestStatus.COMPLETED, 4,
            QuestStatus.CANCELLED, 5
    );

    private static final Map<QuestApplicationStatus, Integer> APPLICATION_STATUS_SORT_ORDER = Map.of(
            QuestApplicationStatus.APPROVED, 0,
            QuestApplicationStatus.PENDING, 1,
            QuestApplicationStatus.DECLINED, 2,
            QuestApplicationStatus.WITHDRAWN, 3
    );
    public DashboardResponseDTO getMyDashboard(AppUser currentUser) {
        if (currentUser == null) {
            return DashboardResponseDTO.builder()
                    .options(workmarketOptionsService.getOptions(null))
                    .summary(DashboardSummaryDTO.builder().build())
                    .sections(dashboardSectionsFactory.emptySections())
                    .quests(List.of())
                    .myQuests(List.of())
                    .availableQuests(List.of())
                    .myApplications(List.of())
                    .recentNews(List.of())
                    .incomingCircleRequests(List.of())
                    .circles(List.of())
                    .appUsers(List.of())
                    .build();
        }

        List<Quest> visibleQuests = questService.getAllQuests(currentUser);
        List<Quest> sortedQuests = sortQuests(visibleQuests);
        List<QuestApplication> applications = questApplicationRepository.findByApplicantId(currentUser.getId());
        List<QuestApplicationResponseDTO> sortedApplications = sortApplications(applications);
        List<QuestNewsItemResponseDTO> recentNews = questNewsService.getMyNews(currentUser).stream()
                .limit(6)
                .map(questNewsMgr::toDto)
                .toList();
        List<CircleRequestResponseDTO> incomingCircleRequests = circleService.getIncomingRequests(currentUser);
        List<CircleGroupResponseDTO> circles = circleService.getCircles(currentUser);

        List<QuestResponseDTO> questDtos = questService.toResponses(sortedQuests, currentUser);
        List<QuestResponseDTO> myQuestDtos = questDtos.stream()
                .filter(quest -> quest.getViewerRelation() == com.themuffinman.app.workmarket.dto.QuestViewerRelation.OWNER)
                .toList();
        List<QuestResponseDTO> availableQuestDtos = questDtos.stream()
                .filter(quest -> quest.getStatus() == QuestStatus.OPEN)
                .filter(quest -> quest.getViewerRelation() != com.themuffinman.app.workmarket.dto.QuestViewerRelation.OWNER)
                .toList();
        DashboardSectionsDTO sections = dashboardSectionsFactory.buildSections(myQuestDtos, sortedApplications, recentNews, incomingCircleRequests);

        return DashboardResponseDTO.builder()
                .options(workmarketOptionsService.getOptions(currentUser))
                .summary(buildSummary(currentUser, visibleQuests, applications))
                .sections(sections)
                .quests(questDtos)
                .myQuests(myQuestDtos)
                .availableQuests(availableQuestDtos)
                .myApplications(sortedApplications)
                .recentNews(recentNews)
                .incomingCircleRequests(incomingCircleRequests)
                .circles(circles)
                .appUsers(getDashboardAppUsers(currentUser))
                .build();
    }

    public DashboardSummaryDTO getMySummary(AppUser currentUser) {
        if (currentUser == null) {
            return DashboardSummaryDTO.builder().build();
        }

        List<Quest> quests = questService.getAllQuests(currentUser);
        List<QuestApplication> applications = questApplicationRepository.findByApplicantId(currentUser.getId());
        return buildSummary(currentUser, quests, applications);
    }

    private DashboardSummaryDTO buildSummary(AppUser currentUser, List<Quest> quests, List<QuestApplication> applications) {
        if (currentUser == null) {
            return DashboardSummaryDTO.builder().build();
        }

        long questCount = quests.size();
        long visibleMyQuestsCount = countMyQuestsByStatus(quests, currentUser.getId(), QuestStatus::isVisibleOwnerWork);
        long activeMyQuestsCount = countActiveMyQuests(quests, currentUser.getId());
        long completedMyQuestsCount = countMyQuestsByStatus(quests, currentUser.getId(), status -> status == QuestStatus.COMPLETED);
        long openQuestCount = countQuestsByStatus(quests, QuestStatus.OPEN);
        long assignedQuestCount = countQuestsByStatus(quests, QuestStatus.ASSIGNED);
        long waitingConfirmationQuestCount = countQuestsByStatus(quests, QuestStatus.WAITING_CONFIRMATION);
        long pendingWorkApplicationsCount = countApplicationsByStatus(applications, QuestApplicationStatus.PENDING);
        long activeWorkApplicationsCount = countActiveWorkApplications(applications);
        long activeWorkCount = activeMyQuestsCount + activeWorkApplicationsCount;

        return DashboardSummaryDTO.builder()
                .adminModeEnabled(currentUser.getRole() == AppUserRole.ADMIN)
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
                .unreadNewsCount(questNewsService.getUnreadCount(currentUser))
                .totalUserCount(appUserRepository.count())
                .adminUserCount(appUserRepository.countByRole(AppUserRole.ADMIN))
                .build();
    }

    private List<Quest> sortQuests(List<Quest> quests) {
        return quests.stream()
                .sorted(Comparator
                        .comparing((Quest quest) -> QUEST_STATUS_SORT_ORDER.getOrDefault(quest.getStatus(), Integer.MAX_VALUE))
                        .thenComparing(Quest::getId, Comparator.reverseOrder()))
                .toList();
    }

    private List<QuestApplicationResponseDTO> sortApplications(List<QuestApplication> applications) {
        return applications.stream()
                .sorted(Comparator
                        .comparing((QuestApplication application) -> APPLICATION_STATUS_SORT_ORDER.getOrDefault(application.getStatus(), Integer.MAX_VALUE))
                        .thenComparing(QuestApplication::getId, Comparator.reverseOrder()))
                .map(questApplicationService::toApplicantResponse)
                .toList();
    }

    private List<AppUserResponseDTO> getDashboardAppUsers(AppUser currentUser) {
        if (currentUser.getRole() != AppUserRole.ADMIN) {
            return List.of();
        }

        return appUserService.getAllAppUsers(null).stream()
                .map(appUserMgr::toDto)
                .toList();
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
                .filter(quest -> quest.getStatus().isActiveForOwner())
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
                    return questStatus != null && questStatus.isActiveForWorker();
                })
                .count();
    }
}
