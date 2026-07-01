package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.dto.DashboardSummaryDTO;
import com.themuffinman.app.vision.dto.DashboardResponseDTO;
import com.themuffinman.app.vision.dto.DashboardVoiceConfigDTO;
import com.themuffinman.app.vision.dto.DashboardNotificationDestinationTypeDTO;
import com.themuffinman.app.vision.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestApplication;
import com.themuffinman.app.vision.model.QuestApplicationStatus;
import com.themuffinman.app.vision.model.QuestNewsItem;
import com.themuffinman.app.vision.model.QuestNewsType;
import com.themuffinman.app.vision.model.QuestStatus;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserService;
import com.themuffinman.app.vision.repository.QuestApplicationRepository;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.social.service.CircleService;
import com.themuffinman.app.vision.mapper.QuestNewsMgr;
import com.themuffinman.app.vision.service.VisionOptionsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.time.Instant;

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
    private QuestApplicationService questApplicationService;

    @Mock
    private QuestNewsMgr questNewsMgr;

    @Mock
    private AppUserMgr appUserMgr;

    @Mock
    private VisionOptionsService visionOptionsService;

    @Spy
    private VoiceProperties voiceProperties = new VoiceProperties();

    @Spy
    private DashboardSummaryAssembler dashboardSummaryAssembler = new DashboardSummaryAssembler();

    @Spy
    private DashboardSectionsFactory dashboardSectionsFactory = new DashboardSectionsFactory(
            new DashboardSectionGrouper(),
            new DashboardPlannerAssembler(),
            new DashboardNotificationAssembler()
    );

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
        when(questApplicationRepository.findForApplicantDashboard(currentUser.getId())).thenReturn(List.of(
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
        when(questApplicationRepository.findForApplicantDashboard(currentUser.getId())).thenReturn(List.of());
        when(questNewsService.getMyNews(currentUser)).thenReturn(List.of());
        when(circleService.getIncomingRequests(currentUser)).thenReturn(List.of());
        when(circleService.getCircles(currentUser)).thenReturn(List.of());
        when(questService.toResponses(List.of(availableQuest, ownedQuest), currentUser)).thenReturn(List.of(
                QuestResponseDTO.builder().id(2L).status(QuestStatus.OPEN).viewerRelation(com.themuffinman.app.vision.dto.QuestViewerRelationDTO.VIEWER).build(),
                QuestResponseDTO.builder().id(1L).status(QuestStatus.OPEN).viewerRelation(com.themuffinman.app.vision.dto.QuestViewerRelationDTO.OWNER).build()
        ));
        when(visionOptionsService.getOptions(currentUser)).thenReturn(com.themuffinman.app.vision.dto.VisionOptionsDTO.builder().build());

        DashboardResponseDTO result = dashboardService.getMyDashboard(currentUser);

        assertTrue(result.getOptions() != null);
        assertEquals(2, result.getQuests().size());
        assertEquals(1, result.getMyQuests().size());
        assertEquals(1, result.getAvailableQuests().size());
        assertEquals(1, result.getSections().getRecentMyQuests().size());
        assertEquals("Calendar", result.getSections().getNavigation().getTabs().getFirst().getTitle());
        assertEquals("SideJob", result.getSections().getNavigation().getTabs().get(1).getTitle());
        assertEquals(1, result.getSections().getVisibleMyQuests().size());
        assertEquals(1, result.getSections().getMyQuestGroups().size());
        assertEquals("OPEN", result.getSections().getMyQuestGroups().getFirst().getKey());
        assertEquals(0, result.getSections().getMyApplicationGroups().size());
        assertEquals(1, result.getSections().getOpenWork().getOpenQuests().size());
        assertEquals(0, result.getSections().getOpenWork().getWaitingQuests().size());
        assertEquals(1, result.getSections().getPlanner().getFlexibleItems().size());
        assertEquals(0, result.getSections().getPlanner().getScheduledItems().size());
        assertEquals(0, result.getSections().getNotifications().getRecentItems().size());
        assertTrue(result.getMyQuests().stream().allMatch(quest -> quest.getId() == 1L));
    }

    @Test
    void getMyDashboardBuildsApplicantActionsForPendingApplications() {
        AppUser currentUser = new AppUser();
        currentUser.setId(5L);
        currentUser.setRole(AppUserRole.USER);

        Quest quest = new Quest();
        quest.setId(11L);
        AppUser owner = new AppUser();
        owner.setId(7L);
        owner.setUsername("owner");
        quest.setCreator(owner);
        quest.setTitle("Paint shed");
        quest.setDescription("desc");
        quest.setStatus(QuestStatus.OPEN);
        quest.setTermFixed(false);

        QuestApplication application = new QuestApplication();
        application.setId(21L);
        application.setQuest(quest);
        application.setApplicant(currentUser);
        application.setMessage("hello");
        application.setStatus(QuestApplicationStatus.PENDING);
        QuestApplicationResponseDTO applicationDto = QuestApplicationResponseDTO.builder()
                .id(21L)
                .questId(11L)
                .questTitle("Paint shed")
                .questDescription("desc")
                .questStatus(QuestStatus.OPEN)
                .applicantId(5L)
                .applicantUsername("user")
                .message("hello")
                .status(QuestApplicationStatus.PENDING)
                .proposedPrice(java.math.BigDecimal.TEN)
                .createdAt(Instant.parse("2026-06-24T10:00:00Z"))
                .allowedActions(List.of(ApplicationAllowedActionDTO.EDIT, ApplicationAllowedActionDTO.WITHDRAW))
                .build();

        when(questService.getAllQuests(currentUser)).thenReturn(List.of(quest));
        when(questApplicationRepository.findForApplicantDashboard(currentUser.getId())).thenReturn(List.of(application));
        when(questNewsService.getMyNews(currentUser)).thenReturn(List.of());
        when(circleService.getIncomingRequests(currentUser)).thenReturn(List.of());
        when(circleService.getCircles(currentUser)).thenReturn(List.of());
        when(questApplicationService.toApplicantResponse(application)).thenReturn(applicationDto);
        when(questService.toResponses(List.of(quest), currentUser)).thenReturn(List.of(
                QuestResponseDTO.builder().id(11L).status(QuestStatus.OPEN).viewerRelation(com.themuffinman.app.vision.dto.QuestViewerRelationDTO.VIEWER).build()
        ));

        DashboardResponseDTO result = dashboardService.getMyDashboard(currentUser);

        assertEquals(List.of(ApplicationAllowedActionDTO.EDIT, ApplicationAllowedActionDTO.WITHDRAW), result.getMyApplications().getFirst().getAllowedActions());
    }

    @Test
    void getMyDashboardBuildsNotificationDestinationsFromBackendSection() {
        AppUser currentUser = new AppUser();
        currentUser.setId(5L);
        currentUser.setRole(AppUserRole.USER);

        QuestNewsItem newsItem = new QuestNewsItem();
        newsItem.setId(41L);

        QuestNewsItemResponseDTO newsDto = QuestNewsItemResponseDTO.builder()
                .id(41L)
                .type(QuestNewsType.APPLICATION_APPROVED)
                .title("Application approved")
                .message("Your application was approved.")
                .questId(11L)
                .questTitle("Paint shed")
                .applicationId(21L)
                .actorUserId(7L)
                .actorUsername("owner")
                .createdAt(Instant.parse("2026-06-24T10:00:00Z"))
                .readAt(null)
                .build();

        when(questService.getAllQuests(currentUser)).thenReturn(List.of());
        when(questApplicationRepository.findForApplicantDashboard(currentUser.getId())).thenReturn(List.of());
        when(questNewsService.getMyNews(currentUser)).thenReturn(List.of(newsItem));
        when(circleService.getIncomingRequests(currentUser)).thenReturn(List.of());
        when(circleService.getCircles(currentUser)).thenReturn(List.of());
        when(questService.toResponses(List.of(), currentUser)).thenReturn(List.of());
        when(questNewsMgr.toDto(newsItem)).thenReturn(newsDto);

        DashboardResponseDTO result = dashboardService.getMyDashboard(currentUser);

        assertEquals(1, result.getSections().getNotifications().getRecentItems().size());
        assertEquals(DashboardNotificationDestinationTypeDTO.APPLICATION, result.getSections().getNotifications().getRecentItems().getFirst().getDestinationType());
        assertEquals(21L, result.getSections().getNotifications().getRecentItems().getFirst().getDestinationId());
    }

    @Test
    void getMyVoiceConfigReturnsConfiguredBackendDefaultsForAuthenticatedUser() {
        AppUser currentUser = new AppUser();
        currentUser.setId(5L);

        voiceProperties.setEnabled(true);
        voiceProperties.setSpeechToTextEnabled(true);
        voiceProperties.setTextToSpeechEnabled(true);
        voiceProperties.setProvider("openai");
        voiceProperties.setRecognitionProvider("openai");
        voiceProperties.setSynthesisProvider("openai");
        voiceProperties.setApiKey("test-openai-key");
        voiceProperties.setPreferredLocale("en-US");
        voiceProperties.setInterimResults(true);
        voiceProperties.setContinuousRecognition(false);
        voiceProperties.setMaxAlternatives(2);
        voiceProperties.setAutoSpeakResponses(true);
        voiceProperties.setMaxRecordingMillis(15_000);
        voiceProperties.setMaxAudioBytes(1_500_000);
        voiceProperties.setMaxSpeechTextLength(800);

        DashboardVoiceConfigDTO config = dashboardService.getMyVoiceConfig(currentUser);

        assertTrue(config.isEnabled());
        assertTrue(config.isSpeechToTextEnabled());
        assertTrue(config.isTextToSpeechEnabled());
        assertEquals("openai", config.getRecognitionProvider());
        assertEquals("openai", config.getSynthesisProvider());
        assertEquals("en-US", config.getPreferredLocale());
        assertTrue(config.isInterimResults());
        assertEquals(2, config.getMaxAlternatives());
        assertTrue(config.isAutoSpeakResponses());
        assertEquals(15_000, config.getMaxRecordingMillis());
        assertEquals(1_500_000, config.getMaxAudioBytes());
        assertEquals(800, config.getMaxSpeechTextLength());
    }

    @Test
    void getMyVoiceConfigFailsClosedForAnonymousUser() {
        voiceProperties.setEnabled(true);
        voiceProperties.setSpeechToTextEnabled(true);
        voiceProperties.setTextToSpeechEnabled(true);

        DashboardVoiceConfigDTO config = dashboardService.getMyVoiceConfig(null);

        assertEquals(false, config.isEnabled());
        assertEquals(false, config.isSpeechToTextEnabled());
        assertEquals(false, config.isTextToSpeechEnabled());
    }
}
