package com.themuffinman.app.vision.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardReadQueryServiceTest {

    @Mock
    private QuestService questService;
    @Mock
    private QuestApplicationRepository questApplicationRepository;
    @Mock
    private QuestApplicationService questApplicationService;
    @Mock
    private QuestNewsService questNewsService;
    @Mock
    private CircleReadService circleReadService;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private AppUserReadService appUserReadService;
    @Mock
    private QuestNewsMgr questNewsMgr;
    @Mock
    private AppUserMgr appUserMgr;

    @Test
    void loadBuildsSortedReadModelForAdminUser() {
        DashboardReadQueryService service = new DashboardReadQueryService(
                questService,
                questApplicationRepository,
                questApplicationService,
                questNewsService,
                circleReadService,
                appUserRepository,
                appUserReadService,
                questNewsMgr,
                appUserMgr
        );

        AppUser currentUser = new AppUser();
        currentUser.setId(5L);
        currentUser.setRole(AppUserRole.ADMIN);

        Quest openQuest = new Quest();
        openQuest.setId(2L);
        openQuest.setStatus(QuestStatus.OPEN);
        Quest assignedQuest = new Quest();
        assignedQuest.setId(1L);
        assignedQuest.setStatus(QuestStatus.ASSIGNED);

        QuestApplication pendingApplication = new QuestApplication();
        pendingApplication.setId(11L);
        pendingApplication.setStatus(QuestApplicationStatus.PENDING);
        QuestApplication approvedApplication = new QuestApplication();
        approvedApplication.setId(10L);
        approvedApplication.setStatus(QuestApplicationStatus.APPROVED);

        QuestApplicationResponseDTO pendingDto = QuestApplicationResponseDTO.builder().id(11L).build();
        QuestApplicationResponseDTO approvedDto = QuestApplicationResponseDTO.builder().id(10L).build();

        QuestNewsItemResponseDTO newsDto = QuestNewsItemResponseDTO.builder().id(31L).title("New application").build();
        CircleRequestResponseDTO requestDto = CircleRequestResponseDTO.builder().id(41L).build();
        CircleGroupResponseDTO circleDto = CircleGroupResponseDTO.builder().id(51L).name("Friends").build();
        AppUserResponseDTO adminDto = AppUserResponseDTO.builder().id(1L).username("admin").build();

        when(questService.getAllQuests(currentUser)).thenReturn(List.of(openQuest, assignedQuest));
        when(questApplicationRepository.findForApplicantDashboard(currentUser.getId())).thenReturn(List.of(pendingApplication, approvedApplication));
        when(questApplicationService.toApplicantResponse(approvedApplication)).thenReturn(approvedDto);
        when(questApplicationService.toApplicantResponse(pendingApplication)).thenReturn(pendingDto);
        when(questNewsService.getMyNews(currentUser)).thenReturn(List.of());
        when(questNewsService.getUnreadCount(currentUser)).thenReturn(3L);
        when(circleReadService.getIncomingRequests(currentUser)).thenReturn(List.of(requestDto));
        when(circleReadService.getCircles(currentUser)).thenReturn(List.of(circleDto));
        when(appUserRepository.count()).thenReturn(12L);
        when(appUserRepository.countByRole(AppUserRole.ADMIN)).thenReturn(2L);
        when(appUserReadService.getAllAppUsers(null)).thenReturn(List.of(currentUser));
        when(appUserMgr.toDto(currentUser)).thenReturn(adminDto);

        DashboardReadModel readModel = service.load(currentUser);

        assertEquals(List.of(2L, 1L), readModel.sortedQuests().stream().map(Quest::getId).toList());
        assertEquals(List.of(10L, 11L), readModel.sortedApplications().stream().map(QuestApplicationResponseDTO::getId).toList());
        assertEquals(1, readModel.incomingCircleRequests().size());
        assertEquals(1, readModel.circles().size());
        assertEquals(3L, readModel.unreadNewsCount());
        assertEquals(12L, readModel.totalUserCount());
        assertEquals(2L, readModel.adminUserCount());
        assertEquals(List.of(adminDto), readModel.appUsers());
    }
}
