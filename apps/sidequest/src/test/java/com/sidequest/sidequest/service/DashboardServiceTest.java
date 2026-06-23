package com.sidequest.sidequest.service;

import com.sidequest.sidequest.dto.DashboardSummaryDTO;
import com.sidequest.sidequest.dto.DashboardResponseDTO;
import com.sidequest.sidequest.dto.QuestResponseDTO;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.model.AppUserRole;
import com.sidequest.sidequest.model.Quest;
import com.sidequest.sidequest.model.QuestApplication;
import com.sidequest.sidequest.model.QuestApplicationStatus;
import com.sidequest.sidequest.model.QuestStatus;
import com.sidequest.sidequest.repository.AppUserRepository;
import com.sidequest.sidequest.repository.QuestApplicationRepository;
import com.sidequest.sidequest.mapper.AppUserMgr;
import com.sidequest.sidequest.mapper.QuestApplicationMgr;
import com.sidequest.sidequest.mapper.QuestNewsMgr;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private QuestService questService;

    @Mock
    private QuestApplicationRepository questApplicationRepository;

    @Mock
    private QuestNewsService questNewsService;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private CircleService circleService;

    @Mock
    private AppUserService appUserService;

    @Mock
    private QuestApplicationMgr questApplicationMgr;

    @Mock
    private QuestNewsMgr questNewsMgr;

    @Mock
    private AppUserMgr appUserMgr;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getMySummaryAggregatesDashboardCounts() {
        AppUser currentUser = new AppUser();
        currentUser.setId(5L);

        AppUser otherUser = new AppUser();
        otherUser.setId(6L);

        Quest openQuest = new Quest();
        openQuest.setId(1L);
        openQuest.setCreator(currentUser);
        openQuest.setStatus(QuestStatus.OPEN);

        Quest assignedQuest = new Quest();
        assignedQuest.setId(2L);
        assignedQuest.setCreator(currentUser);
        assignedQuest.setStatus(QuestStatus.ASSIGNED);

        Quest waitingQuest = new Quest();
        waitingQuest.setId(3L);
        waitingQuest.setCreator(currentUser);
        waitingQuest.setStatus(QuestStatus.WAITING_CONFIRMATION);

        Quest inProgressQuest = new Quest();
        inProgressQuest.setId(4L);
        inProgressQuest.setCreator(otherUser);
        inProgressQuest.setStatus(QuestStatus.IN_PROGRESS);

        QuestApplication pendingApplication = new QuestApplication();
        pendingApplication.setId(11L);
        pendingApplication.setStatus(QuestApplicationStatus.PENDING);

        QuestApplication approvedOpenApplication = new QuestApplication();
        approvedOpenApplication.setId(12L);
        approvedOpenApplication.setStatus(QuestApplicationStatus.APPROVED);
        approvedOpenApplication.setQuest(openQuest);

        QuestApplication approvedAssignedApplication = new QuestApplication();
        approvedAssignedApplication.setId(13L);
        approvedAssignedApplication.setStatus(QuestApplicationStatus.APPROVED);
        approvedAssignedApplication.setQuest(assignedQuest);

        QuestApplication approvedWaitingApplication = new QuestApplication();
        approvedWaitingApplication.setId(14L);
        approvedWaitingApplication.setStatus(QuestApplicationStatus.APPROVED);
        approvedWaitingApplication.setQuest(waitingQuest);

        QuestApplication approvedInProgressApplication = new QuestApplication();
        approvedInProgressApplication.setId(16L);
        approvedInProgressApplication.setStatus(QuestApplicationStatus.APPROVED);
        approvedInProgressApplication.setQuest(inProgressQuest);

        QuestApplication declinedApplication = new QuestApplication();
        declinedApplication.setId(15L);
        declinedApplication.setStatus(QuestApplicationStatus.DECLINED);

        when(questService.getAllQuests(currentUser)).thenReturn(List.of(openQuest, assignedQuest, waitingQuest, inProgressQuest));
        when(questApplicationRepository.findByApplicantId(currentUser.getId())).thenReturn(List.of(
                pendingApplication,
                approvedOpenApplication,
                approvedAssignedApplication,
                approvedWaitingApplication,
                approvedInProgressApplication,
                declinedApplication
        ));
        when(questNewsService.getUnreadCount(currentUser)).thenReturn(7L);
        when(appUserRepository.count()).thenReturn(12L);
        when(appUserRepository.countByRole(AppUserRole.ADMIN)).thenReturn(2L);

        DashboardSummaryDTO summary = dashboardService.getMySummary(currentUser);

        assertEquals(4L, summary.getQuestCount());
        assertEquals(2L, summary.getVisibleMyQuestsCount());
        assertEquals(1L, summary.getPendingWorkApplicationsCount());
        assertEquals(3L, summary.getActiveWorkApplicationsCount());
        assertEquals(1L, summary.getActiveMyQuestsCount());
        assertEquals(4L, summary.getActiveWorkCount());
        assertEquals(0L, summary.getCompletedMyQuestsCount());
        assertEquals(1L, summary.getOpenQuestCount());
        assertEquals(1L, summary.getAssignedQuestCount());
        assertEquals(1L, summary.getWaitingConfirmationQuestCount());
        assertEquals(7L, summary.getUnreadNewsCount());
        assertEquals(12L, summary.getTotalUserCount());
        assertEquals(2L, summary.getAdminUserCount());
    }

    @Test
    void getMySummaryReturnsEmptySummaryForAnonymousUser() {
        DashboardSummaryDTO summary = dashboardService.getMySummary(null);

        assertEquals(0L, summary.getQuestCount());
        assertEquals(0L, summary.getUnreadNewsCount());
        assertEquals(0L, summary.getTotalUserCount());
    }

    @Test
    void getMyDashboardAssemblesNormalizedScreenModel() {
        AppUser currentUser = new AppUser();
        currentUser.setId(5L);
        currentUser.setRole(AppUserRole.USER);

        Quest ownedQuest = new Quest();
        ownedQuest.setId(1L);
        ownedQuest.setCreator(currentUser);
        ownedQuest.setStatus(QuestStatus.OPEN);
        ownedQuest.setTitle("Fix fence");

        Quest availableQuest = new Quest();
        availableQuest.setId(2L);
        AppUser otherCreator = new AppUser();
        otherCreator.setId(6L);
        availableQuest.setCreator(otherCreator);
        availableQuest.setStatus(QuestStatus.OPEN);
        availableQuest.setTitle("Paint shed");

        when(questService.getAllQuests(currentUser)).thenReturn(List.of(ownedQuest, availableQuest));
        when(questApplicationRepository.findByApplicantId(currentUser.getId())).thenReturn(List.of());
        when(questNewsService.getMyNews(currentUser)).thenReturn(List.of());
        when(circleService.getIncomingRequests(currentUser)).thenReturn(List.of());
        when(circleService.getCircles(currentUser)).thenReturn(List.of());
        when(questService.toResponses(List.of(availableQuest, ownedQuest), currentUser)).thenReturn(List.of(
                QuestResponseDTO.builder().id(2L).status(QuestStatus.OPEN).viewerRelation(com.sidequest.sidequest.dto.QuestViewerRelation.VIEWER).build(),
                QuestResponseDTO.builder().id(1L).status(QuestStatus.OPEN).viewerRelation(com.sidequest.sidequest.dto.QuestViewerRelation.OWNER).build()
        ));

        DashboardResponseDTO result = dashboardService.getMyDashboard(currentUser);

        assertEquals(2, result.getQuests().size());
        assertEquals(1, result.getMyQuests().size());
        assertEquals(1, result.getAvailableQuests().size());
        assertTrue(result.getMyQuests().stream().allMatch(quest -> quest.getId() == 1L));
    }
}
