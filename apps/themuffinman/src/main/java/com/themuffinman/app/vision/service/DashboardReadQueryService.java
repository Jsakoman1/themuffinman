package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.concepts.ActorIdentity;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserReadService;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.vision.mapper.QuestNewsMgr;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestApplication;
import com.themuffinman.app.vision.model.QuestApplicationStatus;
import com.themuffinman.app.vision.model.QuestStatus;
import com.themuffinman.app.vision.repository.QuestApplicationRepository;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
class DashboardReadQueryService {

    private final QuestService questService;
    private final QuestApplicationRepository questApplicationRepository;
    private final QuestApplicationService questApplicationService;
    private final QuestNewsService questNewsService;
    private final CircleReadService circleReadService;
    private final AppUserRepository appUserRepository;
    private final AppUserReadService appUserReadService;
    private final QuestNewsMgr questNewsMgr;
    private final AppUserMgr appUserMgr;

    DashboardReadModel load(AppUser currentUser) {
        if (currentUser == null) {
            return DashboardReadModel.empty();
        }

        List<Quest> sortedQuests = sortQuests(questService.getAllQuests(currentUser));
        List<QuestApplication> applications = questApplicationRepository.findForApplicantDashboard(currentUser.getId());
        List<QuestApplicationResponseDTO> sortedApplications = sortApplications(applications);
        List<QuestNewsItemResponseDTO> recentNews = questNewsService.getMyNews(currentUser).stream()
                .limit(6)
                .map(questNewsMgr::toDto)
                .toList();
        List<CircleRequestResponseDTO> incomingCircleRequests = circleReadService.getIncomingRequests(currentUser);
        List<CircleGroupResponseDTO> circles = circleReadService.getCircles(currentUser);
        long unreadNewsCount = questNewsService.getUnreadCount(currentUser);
        long totalUserCount = appUserRepository.count();
        long adminUserCount = appUserRepository.countByRole(AppUserRole.ADMIN);
        List<AppUserResponseDTO> appUsers = dashboardAppUsers(currentUser);

        return new DashboardReadModel(
                sortedQuests,
                applications,
                sortedApplications,
                recentNews,
                incomingCircleRequests,
                circles,
                unreadNewsCount,
                totalUserCount,
                adminUserCount,
                appUsers
        );
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

    private List<AppUserResponseDTO> dashboardAppUsers(AppUser currentUser) {
        if (!ActorIdentity.from(currentUser).admin()) {
            return List.of();
        }

        return appUserReadService.getAllAppUsers(null).stream()
                .map(appUserMgr::toDto)
                .toList();
    }

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
}
