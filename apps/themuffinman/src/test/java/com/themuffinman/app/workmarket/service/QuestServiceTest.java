package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestViewerRelationDTO;
import com.themuffinman.app.location.service.LocationSettingsService;
import com.themuffinman.app.location.service.LocationGeoService;
import com.themuffinman.app.location.service.LocationQuestPresentationService;
import com.themuffinman.app.workmarket.mapper.QuestApplicationMgr;
import com.themuffinman.app.workmarket.mapper.QuestMgr;
import com.themuffinman.app.workmarket.mapper.UserReviewMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.dto.CircleSummaryDTO;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.workmarket.repository.QuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.QuestRepository;
import com.themuffinman.app.workmarket.repository.UserReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestServiceTest {

    @Mock
    private QuestRepository questRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AppUserLookupService appUserLookupService;

    @Mock
    private QuestApplicationRepository questApplicationRepository;

    @Mock
    private QuestVisibilityService questVisibilityService;

    @Mock
    private QuestMgr questMgr;

    @Mock
    private QuestApplicationMgr questApplicationMgr;

    @Mock
    private UserReviewRepository userReviewRepository;

    @Mock
    private UserReviewMgr userReviewMgr;

    @Mock
    private QuestApplicationService questApplicationService;

    @Mock
    private QuestWorkflowNotificationService questWorkflowNotificationService;

    @Mock
    private LocationSettingsService locationSettingsService;

    @Mock
    private LocationGeoService locationGeoService;

    @Mock
    private LocationQuestPresentationService locationQuestPresentationService;

    private QuestAccessPolicyService questAccessPolicyService;
    private QuestQueryService questQueryService;
    private QuestUpdateService questUpdateService;
    private QuestValidationService questValidationService;
    private QuestStateTransitionService questStateTransitionService;
    private QuestExecutionPrimitiveService questExecutionPrimitiveService;
    private CreateQuestUseCase createQuestUseCase;
    private UpdateQuestUseCase updateQuestUseCase;
    private DeleteQuestUseCase deleteQuestUseCase;
    private StartQuestUseCase startQuestUseCase;
    private CompleteQuestUseCase completeQuestUseCase;
    private ConfirmQuestTermChangeUseCase confirmQuestTermChangeUseCase;
    private RejectQuestTermChangeUseCase rejectQuestTermChangeUseCase;
    private WorkmarketPresentationHelper workmarketPresentationHelper;
    private QuestViewAssembler questViewAssembler;
    private QuestReadService questReadService;

    private QuestService questService;

    @BeforeEach
    void setUp() {
        questAccessPolicyService = new QuestAccessPolicyService();
        questQueryService = new QuestQueryService(locationGeoService);
        questValidationService = new QuestValidationService(questVisibilityService);
        questStateTransitionService = new QuestStateTransitionService(
                questApplicationRepository,
                questValidationService,
                questWorkflowNotificationService,
                questAccessPolicyService
        );
        questUpdateService = new QuestUpdateService(
                appUserLookupService,
                questValidationService,
                questStateTransitionService,
                questAccessPolicyService,
                locationSettingsService,
                questApplicationRepository
        );
        questExecutionPrimitiveService = new QuestExecutionPrimitiveService(
                questRepository,
                questApplicationRepository,
                appUserLookupService,
                questAccessPolicyService,
                questStateTransitionService,
                questWorkflowNotificationService
        );
        createQuestUseCase = new CreateQuestUseCase(
                questValidationService,
                questStateTransitionService,
                questExecutionPrimitiveService,
                questMgr,
                locationSettingsService
        );
        updateQuestUseCase = new UpdateQuestUseCase(questExecutionPrimitiveService, questUpdateService);
        deleteQuestUseCase = new DeleteQuestUseCase(questExecutionPrimitiveService);
        startQuestUseCase = new StartQuestUseCase(questExecutionPrimitiveService);
        completeQuestUseCase = new CompleteQuestUseCase(questExecutionPrimitiveService);
        confirmQuestTermChangeUseCase = new ConfirmQuestTermChangeUseCase(questExecutionPrimitiveService, questStateTransitionService);
        rejectQuestTermChangeUseCase = new RejectQuestTermChangeUseCase(questExecutionPrimitiveService, questStateTransitionService);
        workmarketPresentationHelper = new WorkmarketPresentationHelper();
        QuestPresentationAssembler questPresentationAssembler = new QuestPresentationAssembler(
                workmarketPresentationHelper,
                locationQuestPresentationService
        );
        questViewAssembler = new QuestViewAssembler(
                questMgr,
                questAccessPolicyService,
                userReviewRepository,
                userReviewMgr,
                workmarketPresentationHelper,
                questApplicationRepository,
                questPresentationAssembler
        );
        questReadService = new QuestReadService(
                questRepository,
                questApplicationRepository,
                questApplicationService,
                questVisibilityService,
                questAccessPolicyService,
                questQueryService,
                questExecutionPrimitiveService,
                questMgr,
                questViewAssembler
        );
        questService = new QuestService(
                questReadService,
                createQuestUseCase,
                updateQuestUseCase,
                deleteQuestUseCase,
                startQuestUseCase,
                completeQuestUseCase,
                confirmQuestTermChangeUseCase,
                rejectQuestTermChangeUseCase
        );
    }

    private void stubQuestViewerContext() {
        when(questMgr.withViewerContext(any(QuestResponseDTO.class), any(QuestViewerRelationDTO.class), anyList(), anyBoolean(), any(), anyBoolean()))
                .thenAnswer(invocation -> {
                    QuestResponseDTO dto = invocation.getArgument(0);
                    dto.setViewerRelation(invocation.getArgument(1));
                    dto.setAllowedActions(invocation.getArgument(2));
                    dto.setHasApplied(invocation.getArgument(3));
                    dto.setMyApplicationId(invocation.getArgument(4));
                    dto.setCanViewApplications(invocation.getArgument(5));
                    return dto;
                });
    }

    @Test
    void createQuestUsesAuthenticatedUserAsCreator() {
        AppUser currentUser = createUser(5L, "creator");
        Instant scheduledAt = Instant.now().plusSeconds(7 * 24 * 3600);
        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Fix garden fence")
                .description("Need help with a small repair")
                .awardAmount(BigDecimal.valueOf(45))
                .scheduledAt(scheduledAt)
                .termFixed(true)
                .build();
        Quest mappedQuest = new Quest();
        Quest savedQuest = new Quest();
        savedQuest.setId(10L);

        when(questMgr.toEntity(requestDTO, currentUser)).thenReturn(mappedQuest);
        when(questRepository.save(mappedQuest)).thenReturn(savedQuest);

        Quest result = questService.createQuest(requestDTO, currentUser);

        assertEquals(10L, result.getId());
        assertEquals(scheduledAt, mappedQuest.getScheduledAt());
        assertEquals(true, mappedQuest.isTermFixed());
        assertEquals(QuestAudience.CIRCLES, mappedQuest.getAudience());
        verify(questMgr).toEntity(requestDTO, currentUser);
        verify(questRepository).save(mappedQuest);
    }

    @Test
    void createQuestRejectsEmptyRichTextDescription() {
        AppUser currentUser = createUser(5L, "creator");
        Instant scheduledAt = Instant.now().plusSeconds(7 * 24 * 3600);
        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Fix garden fence")
                .description("<p><br></p>")
                .awardAmount(BigDecimal.valueOf(45))
                .scheduledAt(scheduledAt)
                .termFixed(true)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> questService.createQuest(requestDTO, currentUser));

        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void createQuestRejectsPastScheduledTime() {
        AppUser currentUser = createUser(5L, "creator");
        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Fix garden fence")
                .description("Need help with a small repair")
                .awardAmount(BigDecimal.valueOf(45))
                .scheduledAt(Instant.now().minusSeconds(3600))
                .termFixed(true)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> questService.createQuest(requestDTO, currentUser));

        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("cannot be in the past"));
    }

    @Test
    void getAllQuestsReturnsOnlyVisibleQuestsForNonAdminUsers() {
        AppUser currentUser = createUser(5L, "viewer");
        AppUser creator = createUser(6L, "creator");

        Quest visibleQuest = new Quest();
        visibleQuest.setId(1L);
        visibleQuest.setCreator(currentUser);
        visibleQuest.setAudience(QuestAudience.CIRCLES);

        Quest hiddenQuest = new Quest();
        hiddenQuest.setId(2L);
        hiddenQuest.setCreator(creator);
        hiddenQuest.setAudience(QuestAudience.CIRCLES);

        when(questRepository.findForQuestList()).thenReturn(List.of(visibleQuest, hiddenQuest));
        when(questVisibilityService.canViewQuest(currentUser, visibleQuest)).thenReturn(true);
        when(questVisibilityService.canViewQuest(currentUser, hiddenQuest)).thenReturn(false);

        List<Quest> result = questService.getAllQuests(currentUser);

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    void getAllQuestsReturnsCircleQuestsForConnectedUsers() {
        AppUser currentUser = createUser(5L, "viewer");
        AppUser creator = createUser(6L, "creator");

        Quest visibleQuest = new Quest();
        visibleQuest.setId(3L);
        visibleQuest.setCreator(creator);
        visibleQuest.setAudience(QuestAudience.CIRCLES);

        when(questRepository.findForQuestList()).thenReturn(List.of(visibleQuest));
        when(questVisibilityService.canViewQuest(currentUser, visibleQuest)).thenReturn(true);

        List<Quest> result = questService.getAllQuests(currentUser);

        assertEquals(1, result.size());
        assertEquals(3L, result.getFirst().getId());
    }

    @Test
    void getAllQuestsReturnsEveryoneAudienceForAnyUser() {
        AppUser currentUser = createUser(5L, "viewer");
        AppUser creator = createUser(6L, "creator");

        Quest visibleQuest = new Quest();
        visibleQuest.setId(4L);
        visibleQuest.setCreator(creator);
        visibleQuest.setAudience(QuestAudience.EVERYONE);

        when(questRepository.findForQuestList()).thenReturn(List.of(visibleQuest));
        when(questVisibilityService.canViewQuest(currentUser, visibleQuest)).thenReturn(true);

        List<Quest> result = questService.getAllQuests(currentUser);

        assertEquals(1, result.size());
        assertEquals(4L, result.getFirst().getId());
    }

    @Test
    void searchQuestsFiltersByQueryAudienceAndPagination() {
        stubQuestViewerContext();
        AppUser currentUser = createUser(5L, "viewer");
        AppUser creator = createUser(6L, "creator");

        Quest firstQuest = new Quest();
        firstQuest.setId(11L);
        firstQuest.setCreator(creator);
        firstQuest.setTitle("Fix the fence");
        firstQuest.setDescription("Need help in the garden");
        firstQuest.setAudience(QuestAudience.EVERYONE);
        firstQuest.setAwardAmount(BigDecimal.valueOf(80));
        firstQuest.setScheduledAt(Instant.parse("2026-01-10T10:00:00Z"));
        firstQuest.setStatus(QuestStatus.OPEN);

        Quest secondQuest = new Quest();
        secondQuest.setId(12L);
        secondQuest.setCreator(creator);
        secondQuest.setTitle("Paint the shed");
        secondQuest.setDescription("A small weekend task");
        secondQuest.setAudience(QuestAudience.CIRCLES);
        secondQuest.setAwardAmount(BigDecimal.valueOf(30));
        secondQuest.setScheduledAt(Instant.parse("2026-01-11T10:00:00Z"));
        secondQuest.setStatus(QuestStatus.OPEN);

        when(questRepository.findForQuestList()).thenReturn(List.of(firstQuest, secondQuest));
        when(questVisibilityService.canViewQuest(currentUser, firstQuest)).thenReturn(true);
        when(questVisibilityService.canViewQuest(currentUser, secondQuest)).thenReturn(true);
        when(questMgr.toDto(org.mockito.ArgumentMatchers.any(Quest.class))).thenAnswer(invocation -> {
            Quest quest = invocation.getArgument(0);
            return QuestResponseDTO.builder().id(quest.getId()).build();
        });

        QuestListResponseDTO result = questService.searchQuests(
                currentUser,
                "fence",
                QuestStatus.OPEN,
                QuestAudience.EVERYONE,
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-31"),
                null,
                null,
                true,
                false,
                false,
                null,
                "highest",
                0,
                10
        );

        assertEquals(1, result.getTotalItems());
        assertEquals(1, result.getTotalPages());
        assertEquals(11L, result.getItems().getFirst().getId());
    }

    @Test
    void searchQuestsSupportsPaginationAcrossMultipleResults() {
        stubQuestViewerContext();
        AppUser currentUser = createUser(5L, "viewer");
        AppUser creator = createUser(6L, "creator");

        Quest firstQuest = new Quest();
        firstQuest.setId(21L);
        firstQuest.setCreator(creator);
        firstQuest.setTitle("Quest A");
        firstQuest.setAudience(QuestAudience.EVERYONE);
        firstQuest.setAwardAmount(BigDecimal.valueOf(10));
        firstQuest.setStatus(QuestStatus.OPEN);

        Quest secondQuest = new Quest();
        secondQuest.setId(22L);
        secondQuest.setCreator(creator);
        secondQuest.setTitle("Quest B");
        secondQuest.setAudience(QuestAudience.EVERYONE);
        secondQuest.setAwardAmount(BigDecimal.valueOf(20));
        secondQuest.setStatus(QuestStatus.OPEN);

        when(questRepository.findForQuestList()).thenReturn(List.of(firstQuest, secondQuest));
        when(questVisibilityService.canViewQuest(currentUser, firstQuest)).thenReturn(true);
        when(questVisibilityService.canViewQuest(currentUser, secondQuest)).thenReturn(true);
        when(questMgr.toDto(org.mockito.ArgumentMatchers.any(Quest.class))).thenAnswer(invocation -> {
            Quest quest = invocation.getArgument(0);
            return QuestResponseDTO.builder().id(quest.getId()).build();
        });

        QuestListResponseDTO result = questService.searchQuests(
                currentUser,
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                false,
                null,
                "recommended",
                1,
                1
        );

        assertEquals(2, result.getTotalItems());
        assertEquals(2, result.getTotalPages());
        assertEquals(21L, result.getItems().getFirst().getId());
    }

    @Test
    void getQuestResponseByIdIncludesViewerRelationAndAllowedActionsForApprovedApplicant() {
        stubQuestViewerContext();
        AppUser currentUser = createUser(5L, "worker");
        AppUser creator = createUser(6L, "creator");

        Quest quest = new Quest();
        quest.setId(31L);
        quest.setCreator(creator);
        quest.setTitle("Move furniture");
        quest.setStatus(QuestStatus.WAITING_CONFIRMATION);

        QuestApplication application = new QuestApplication();
        application.setId(41L);
        application.setQuest(quest);
        application.setApplicant(currentUser);
        application.setStatus(QuestApplicationStatus.APPROVED);

        when(questRepository.findForQuestDetail(31L)).thenReturn(Optional.of(quest));
        when(questVisibilityService.canViewQuest(currentUser, quest)).thenReturn(true);
        when(questApplicationRepository.findForApplicantDashboard(currentUser.getId())).thenReturn(List.of(application));
        when(questMgr.toDto(quest)).thenReturn(QuestResponseDTO.builder()
                .id(quest.getId())
                .status(quest.getStatus())
                .audience(QuestAudience.CIRCLES)
                .visibleToCircles(List.of(CircleSummaryDTO.builder().id(9L).name("Trusted neighbours").build()))
                .build());

        QuestResponseDTO result = questService.getQuestResponseById(31L, currentUser);

        assertEquals(QuestViewerRelationDTO.APPROVED_APPLICANT, result.getViewerRelation());
        assertEquals(true, result.isHasApplied());
        assertEquals(41L, result.getMyApplicationId());
        assertEquals(false, result.isCanViewApplications());
        assertEquals(List.of(QuestAllowedActionDTO.CONFIRM_TERM_CHANGE, QuestAllowedActionDTO.REJECT_TERM_CHANGE), result.getAllowedActions());
        assertEquals("Term change waiting", result.getPresentation().getTermChangeSummaryLabel());
        assertEquals("Confirm term change", result.getPresentation().getTermChangeConfirmLabel());
        assertEquals("Reject term change", result.getPresentation().getTermChangeRejectLabel());
    }

    @Test
    void getQuestResponseByIdIncludesApplyActionForFreshViewer() {
        stubQuestViewerContext();
        AppUser currentUser = createUser(5L, "viewer");
        AppUser creator = createUser(6L, "creator");

        Quest quest = new Quest();
        quest.setId(32L);
        quest.setCreator(creator);
        quest.setTitle("Clean garage");
        quest.setStatus(QuestStatus.OPEN);

        when(questRepository.findForQuestDetail(32L)).thenReturn(Optional.of(quest));
        when(questVisibilityService.canViewQuest(currentUser, quest)).thenReturn(true);
        when(questApplicationRepository.findForApplicantDashboard(currentUser.getId())).thenReturn(List.of());
        when(questMgr.toDto(quest)).thenReturn(QuestResponseDTO.builder()
                .id(quest.getId())
                .status(quest.getStatus())
                .audience(QuestAudience.CIRCLES)
                .visibleToCircles(List.of(CircleSummaryDTO.builder().id(9L).name("Trusted neighbours").build()))
                .build());

        QuestResponseDTO result = questService.getQuestResponseById(32L, currentUser);

        assertEquals(QuestViewerRelationDTO.VIEWER, result.getViewerRelation());
        assertEquals(false, result.isHasApplied());
        assertEquals(List.of(QuestAllowedActionDTO.APPLY), result.getAllowedActions());
    }

    @Test
    void getQuestResponseByIdIncludesOwnerActionsForQuestCreator() {
        stubQuestViewerContext();
        AppUser currentUser = createUser(5L, "creator");

        Quest quest = new Quest();
        quest.setId(34L);
        quest.setCreator(currentUser);
        quest.setTitle("Organize garage");
        quest.setStatus(QuestStatus.OPEN);

        when(questRepository.findForQuestDetail(34L)).thenReturn(Optional.of(quest));
        when(questVisibilityService.canViewQuest(currentUser, quest)).thenReturn(true);
        when(questApplicationRepository.findForApplicantDashboard(currentUser.getId())).thenReturn(List.of());
        when(questMgr.toDto(quest)).thenReturn(QuestResponseDTO.builder()
                .id(quest.getId())
                .status(quest.getStatus())
                .audience(QuestAudience.CIRCLES)
                .visibleToCircles(List.of(CircleSummaryDTO.builder().id(9L).name("Trusted neighbours").build()))
                .build());

        QuestResponseDTO result = questService.getQuestResponseById(34L, currentUser);

        assertEquals(QuestViewerRelationDTO.OWNER, result.getViewerRelation());
        assertEquals(List.of(QuestAllowedActionDTO.EDIT, QuestAllowedActionDTO.VIEW_APPLICATIONS, QuestAllowedActionDTO.DELETE), result.getAllowedActions());
        assertEquals(false, result.isHasApplied());
        assertEquals(null, result.getMyApplicationId());
        assertEquals(true, result.getPresentation().isPostingSettingsVisible());
        assertEquals("Trusted neighbours", result.getPresentation().getVisibleToCirclesLabel());
    }

    @Test
    void getQuestDetailResponseByIdReturnsQuestMyApplicationAndVisibleApplications() {
        stubQuestViewerContext();
        AppUser currentUser = createUser(5L, "owner");

        Quest quest = new Quest();
        quest.setId(33L);
        quest.setCreator(currentUser);
        quest.setTitle("Assemble shelf");
        quest.setStatus(QuestStatus.OPEN);
        quest.setAudience(QuestAudience.CIRCLES);
        CircleGroup trustedNeighbours = new CircleGroup();
        trustedNeighbours.setId(9L);
        trustedNeighbours.setName("Trusted neighbours");
        quest.getVisibleToCircles().add(trustedNeighbours);

        AppUser applicant = createUser(6L, "worker");
        QuestApplication visibleApplication = new QuestApplication();
        visibleApplication.setId(51L);
        visibleApplication.setQuest(quest);
        visibleApplication.setApplicant(applicant);
        visibleApplication.setStatus(QuestApplicationStatus.PENDING);

        QuestApplication myApplication = new QuestApplication();
        myApplication.setId(52L);
        myApplication.setQuest(quest);
        myApplication.setApplicant(currentUser);
        myApplication.setStatus(QuestApplicationStatus.DECLINED);

        when(questRepository.findForQuestDetail(33L)).thenReturn(Optional.of(quest));
        when(questVisibilityService.canViewQuest(currentUser, quest)).thenReturn(true);
        when(questApplicationRepository.findForApplicantDashboard(currentUser.getId())).thenReturn(List.of(myApplication));
        when(questMgr.toDto(quest)).thenReturn(QuestResponseDTO.builder()
                .id(quest.getId())
                .status(quest.getStatus())
                .audience(QuestAudience.CIRCLES)
                .visibleToCircles(List.of(CircleSummaryDTO.builder().id(9L).name("Trusted neighbours").build()))
                .build());
        when(questApplicationService.toViewerResponse(myApplication, currentUser)).thenReturn(QuestApplicationResponseDTO.builder().id(52L).build());
        when(questApplicationService.getApplicationsViewForQuest(33L, currentUser, false)).thenReturn(
                com.themuffinman.app.workmarket.dto.QuestApplicationsViewDTO.builder()
                        .visibleApplications(List.of(
                                QuestApplicationResponseDTO.builder().id(51L).build(),
                                QuestApplicationResponseDTO.builder().id(52L).build()
                        ))
                        .build()
        );

        QuestDetailResponseDTO result = questService.getQuestDetailResponseById(33L, currentUser);

        assertEquals(QuestViewerRelationDTO.OWNER, result.getSummary().getViewerRelation());
        assertEquals(true, result.getSummary().isCanViewApplications());
        assertEquals(52L, result.getMyApplication().getId());
        assertEquals(2, result.getSections().getApplicationsView().getVisibleApplications().size());
        assertEquals(false, result.getSections().getReview().isVisible());
        assertEquals(false, result.getSections().getExecution().isVisible());
        assertEquals(true, result.getSections().getManagement().isDeleteVisible());
        assertEquals(true, result.getSections().getManagement().isPostingSettingsVisible());
        assertEquals("Circles", result.getSections().getManagement().getAudienceLabel());
        assertEquals("Trusted neighbours", result.getSections().getManagement().getVisibleToCirclesLabel());
    }

    @Test
    void getQuestDetailResponseByIdBuildsReviewExecutionAndTermChangeSections() {
        stubQuestViewerContext();
        AppUser applicant = createUser(5L, "worker");
        AppUser creator = createUser(7L, "creator");

        Quest quest = new Quest();
        quest.setId(33L);
        quest.setCreator(creator);
        quest.setTitle("Assemble shelf");
        quest.setStatus(QuestStatus.COMPLETED);

        QuestApplication myApplication = new QuestApplication();
        myApplication.setId(52L);
        myApplication.setQuest(quest);
        myApplication.setApplicant(applicant);
        myApplication.setStatus(QuestApplicationStatus.APPROVED);

        when(questRepository.findForQuestDetail(33L)).thenReturn(Optional.of(quest));
        when(questVisibilityService.canViewQuest(applicant, quest)).thenReturn(true);
        when(questApplicationRepository.findForApplicantDashboard(applicant.getId())).thenReturn(List.of(myApplication));
        when(questMgr.toDto(quest)).thenReturn(QuestResponseDTO.builder().id(quest.getId()).status(quest.getStatus()).creatorId(creator.getId()).build());
        when(questApplicationService.toViewerResponse(myApplication, applicant)).thenReturn(QuestApplicationResponseDTO.builder()
                .id(52L)
                .questId(33L)
                .status(QuestApplicationStatus.APPROVED)
                .build());
        when(userReviewRepository.findByQuestIdAndReviewerIdAndReviewedUserId(33L, applicant.getId(), creator.getId()))
                .thenReturn(Optional.empty());

        QuestDetailResponseDTO result = questService.getQuestDetailResponseById(33L, applicant);

        assertEquals(true, result.getSections().getReview().isVisible());
        assertEquals(true, result.getSections().getReview().isCanSubmit());
        assertEquals("employer", result.getSections().getReview().getTarget().getRoleLabel());
        assertEquals(false, result.getSections().getTermChange().isVisible());
        assertEquals(true, result.getSections().getExecution().isVisible());
        assertEquals("You are the approved applicant for this quest.", result.getSections().getExecution().getHelperText());
        assertEquals(false, result.getSections().getManagement().isPostingSettingsVisible());
        assertEquals(null, result.getSections().getManagement().getVisibleToCirclesLabel());
    }

    @Test
    void getQuestDetailResponseByIdUsesApplicantActionsForMyPendingApplication() {
        stubQuestViewerContext();
        AppUser applicant = createUser(5L, "worker");
        AppUser creator = createUser(7L, "creator");

        Quest quest = new Quest();
        quest.setId(33L);
        quest.setCreator(creator);
        quest.setTitle("Assemble shelf");
        quest.setStatus(QuestStatus.OPEN);

        QuestApplication myApplication = new QuestApplication();
        myApplication.setId(52L);
        myApplication.setQuest(quest);
        myApplication.setApplicant(applicant);
        myApplication.setStatus(QuestApplicationStatus.PENDING);

        QuestApplicationResponseDTO myApplicationDto = QuestApplicationResponseDTO.builder()
                .id(52L)
                .questId(33L)
                .status(QuestApplicationStatus.PENDING)
                .allowedActions(List.of(ApplicationAllowedActionDTO.EDIT, ApplicationAllowedActionDTO.WITHDRAW))
                .build();

        when(questRepository.findForQuestDetail(33L)).thenReturn(Optional.of(quest));
        when(questVisibilityService.canViewQuest(applicant, quest)).thenReturn(true);
        when(questApplicationRepository.findForApplicantDashboard(applicant.getId())).thenReturn(List.of(myApplication));
        when(questMgr.toDto(quest)).thenReturn(QuestResponseDTO.builder().id(quest.getId()).status(quest.getStatus()).creatorId(creator.getId()).build());
        when(questApplicationService.toViewerResponse(myApplication, applicant)).thenReturn(myApplicationDto);

        QuestDetailResponseDTO result = questService.getQuestDetailResponseById(33L, applicant);

        assertNotNull(result.getMyApplication());
        assertEquals(List.of(ApplicationAllowedActionDTO.EDIT, ApplicationAllowedActionDTO.WITHDRAW), result.getMyApplication().getAllowedActions());
        verify(questApplicationService).toViewerResponse(myApplication, applicant);
    }

    @Test
    void getApplicationDetailResponseByIdReturnsApplicationAndQuestForApplicant() {
        stubQuestViewerContext();
        AppUser creator = createUser(7L, "creator");
        AppUser applicant = createUser(5L, "worker");

        Quest quest = new Quest();
        quest.setId(33L);
        quest.setCreator(creator);
        quest.setTitle("Assemble shelf");
        quest.setStatus(QuestStatus.OPEN);

        QuestApplication application = new QuestApplication();
        application.setId(52L);
        application.setQuest(quest);
        application.setApplicant(applicant);
        application.setStatus(QuestApplicationStatus.PENDING);

        when(questApplicationRepository.findForApplicationDetail(52L)).thenReturn(Optional.of(application));
        when(questRepository.findForQuestDetail(33L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findForApplicantDashboard(applicant.getId())).thenReturn(List.of(application));
        when(questMgr.toDto(quest)).thenReturn(QuestResponseDTO.builder()
                .id(33L)
                .status(QuestStatus.OPEN)
                .creatorUsername("creator")
                .questNavigation(com.themuffinman.app.common.dto.NavigationTargetDTO.builder()
                        .type(com.themuffinman.app.common.dto.NavigationTargetType.QUEST_DETAIL)
                        .entityId(33L)
                        .build())
                .creatorNavigation(com.themuffinman.app.common.dto.NavigationTargetDTO.builder()
                        .type(com.themuffinman.app.common.dto.NavigationTargetType.USER_PROFILE)
                        .entityId(7L)
                        .build())
                .build());
        when(questApplicationService.toViewerResponse(application, applicant)).thenReturn(QuestApplicationResponseDTO.builder()
                .id(52L)
                .questId(33L)
                .questTitle("Assemble shelf")
                .allowedActions(List.of(ApplicationAllowedActionDTO.EDIT, ApplicationAllowedActionDTO.WITHDRAW))
                .build());

        QuestApplicationDetailResponseDTO result = questService.getApplicationDetailResponseById(52L, applicant);

        assertEquals(52L, result.getSummary().getId());
        assertEquals(List.of(ApplicationAllowedActionDTO.EDIT, ApplicationAllowedActionDTO.WITHDRAW), result.getApplication().getAllowedActions());
        assertEquals(33L, result.getSections().getQuest().getId());
        assertEquals(QuestViewerRelationDTO.APPLICANT, result.getSections().getQuest().getViewerRelation());
        assertEquals(true, result.getSections().getNavigation().isCanOpenQuest());
        assertEquals(true, result.getSections().getNavigation().isCanOpenPostedBy());
        assertEquals(33L, result.getSections().getNavigation().getQuestId());
        assertEquals("Assemble shelf", result.getSections().getContext().getQuestLabel());
        assertEquals("creator", result.getSections().getContext().getPostedByLabel());
        assertEquals(true, result.getSections().getContext().isShowStatus());
        assertEquals(true, result.getSections().getContext().isShowTerm());
        assertEquals(true, result.getSections().getContext().isShowWorkers());
        verify(questApplicationService).toViewerResponse(application, applicant);
    }

    @Test
    void getApplicationDetailResponseByIdRejectsUnrelatedViewer() {
        AppUser creator = createUser(7L, "creator");
        AppUser applicant = createUser(8L, "worker");
        AppUser viewer = createUser(5L, "viewer");

        Quest quest = new Quest();
        quest.setId(33L);
        quest.setCreator(creator);
        quest.setTitle("Assemble shelf");
        quest.setStatus(QuestStatus.OPEN);

        QuestApplication application = new QuestApplication();
        application.setId(52L);
        application.setQuest(quest);
        application.setApplicant(applicant);
        application.setStatus(QuestApplicationStatus.PENDING);

        when(questApplicationRepository.findForApplicationDetail(52L)).thenReturn(Optional.of(application));
        when(questRepository.findForQuestDetail(33L)).thenReturn(Optional.of(quest));
        assertThrows(ResponseStatusException.class, () -> questService.getApplicationDetailResponseById(52L, viewer));
    }

    @Test
    void adminCanSeeCircleQuestsRegardlessOfRelationship() {
        AppUser admin = createUser(5L, "admin");
        admin.setRole(AppUserRole.ADMIN);
        AppUser creator = createUser(6L, "creator");

        Quest hiddenQuest = new Quest();
        hiddenQuest.setId(5L);
        hiddenQuest.setCreator(creator);
        hiddenQuest.setAudience(QuestAudience.CIRCLES);

        when(questRepository.findForQuestList()).thenReturn(List.of(hiddenQuest));
        when(questVisibilityService.canViewQuest(admin, hiddenQuest)).thenReturn(true);

        List<Quest> result = questService.getAllQuests(admin);

        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst().getId());
    }

    @Test
    void updateQuestThrowsWhenAuthenticatedUserIsNotOwner() {
        AppUser creator = createUser(1L, "creator");
        AppUser otherUser = createUser(2L, "other");
        Quest quest = new Quest();
        quest.setId(9L);
        quest.setCreator(creator);

        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Updated title")
                .description("Updated description")
                .awardAmount(BigDecimal.TEN)
                .build();

        when(questRepository.findForQuestDetail(9L)).thenReturn(Optional.of(quest));

        assertThrows(ResponseStatusException.class, () -> questService.updateQuest(9L, requestDTO, otherUser));
    }

    @Test
    void updateQuestChangesQuestWhenAuthenticatedUserIsOwner() {
        AppUser creator = createUser(1L, "creator");
        Quest quest = new Quest();
        quest.setId(9L);
        quest.setCreator(creator);
        quest.setAssigneeTarget(3);

        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Updated title")
                .description("Updated description")
                .awardAmount(BigDecimal.valueOf(80))
                .build();

        when(questRepository.findForQuestDetail(9L)).thenReturn(Optional.of(quest));
        when(questRepository.save(quest)).thenReturn(quest);

        questService.updateQuest(9L, requestDTO, creator);

        ArgumentCaptor<Quest> questCaptor = ArgumentCaptor.forClass(Quest.class);
        verify(questRepository).save(questCaptor.capture());
        Quest savedQuest = questCaptor.getValue();

        assertEquals("Updated title", savedQuest.getTitle());
        assertEquals("Updated description", savedQuest.getDescription());
        assertEquals(BigDecimal.valueOf(80), savedQuest.getAwardAmount());
        assertEquals(3, savedQuest.getAssigneeTarget());
    }

    @Test
    void updateQuestAllowsOwnerToAssignQuestBeforeAllSlotsAreFilled() {
        AppUser creator = createUser(1L, "creator");
        Quest quest = new Quest();
        quest.setId(24L);
        quest.setCreator(creator);
        quest.setTitle("Move furniture");
        quest.setDescription("Need help");
        quest.setAwardAmount(BigDecimal.valueOf(50));
        quest.setAssigneeTarget(3);
        quest.setStatus(QuestStatus.OPEN);

        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Move furniture")
                .description("Need help")
                .awardAmount(BigDecimal.valueOf(50))
                .status(QuestStatus.ASSIGNED)
                .build();

        when(questRepository.findForQuestDetail(24L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.countByQuestIdAndStatus(24L, QuestApplicationStatus.APPROVED)).thenReturn(1L);
        when(questRepository.save(quest)).thenReturn(quest);

        questService.updateQuest(24L, requestDTO, creator);

        assertEquals(QuestStatus.ASSIGNED, quest.getStatus());
    }

    @Test
    void updateQuestAllowsOwnerToEditPastQuestWithoutChangingTerm() {
        AppUser creator = createUser(1L, "creator");
        Instant pastScheduledAt = Instant.now().minusSeconds(2 * 24 * 3600);

        Quest quest = new Quest();
        quest.setId(21L);
        quest.setCreator(creator);
        quest.setTitle("Original title");
        quest.setDescription("Original description");
        quest.setAwardAmount(BigDecimal.valueOf(40));
        quest.setScheduledAt(pastScheduledAt);
        quest.setTermFixed(true);

        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Updated title")
                .description("Updated description")
                .awardAmount(BigDecimal.valueOf(55))
                .scheduledAt(pastScheduledAt)
                .termFixed(true)
                .build();

        when(questRepository.findForQuestDetail(21L)).thenReturn(Optional.of(quest));
        when(questRepository.save(quest)).thenReturn(quest);

        questService.updateQuest(21L, requestDTO, creator);

        assertEquals("Updated title", quest.getTitle());
        assertEquals("Updated description", quest.getDescription());
        assertEquals(BigDecimal.valueOf(55), quest.getAwardAmount());
        assertEquals(pastScheduledAt, quest.getScheduledAt());
        assertTrue(quest.isTermFixed());
    }

    @Test
    void updateQuestPreservesRichTextDescriptionWhitespace() {
        AppUser creator = createUser(1L, "creator");
        Quest quest = new Quest();
        quest.setId(22L);
        quest.setCreator(creator);
        quest.setDescription("<p>Original</p>");
        quest.setAwardAmount(BigDecimal.valueOf(40));

        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Updated title")
                .description("<p>  Indented</p>")
                .awardAmount(BigDecimal.valueOf(40))
                .build();

        when(questRepository.findForQuestDetail(22L)).thenReturn(Optional.of(quest));
        when(questRepository.save(quest)).thenReturn(quest);

        questService.updateQuest(22L, requestDTO, creator);

        assertEquals("<p>&nbsp;&nbsp;Indented</p>", quest.getDescription());
    }

    @Test
    void updateQuestToFreeClearsExistingApplicationPrices() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = new Quest();
        quest.setId(23L);
        quest.setCreator(creator);
        quest.setTitle("Original title");
        quest.setDescription("Original description");
        quest.setAwardAmount(BigDecimal.valueOf(40));

        QuestApplication application = new QuestApplication();
        application.setId(14L);
        application.setQuest(quest);
        application.setApplicant(applicant);
        application.setProposedPrice(BigDecimal.valueOf(25));

        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Updated title")
                .description("Updated description")
                .awardAmount(BigDecimal.ZERO)
                .build();

        when(questRepository.findForQuestDetail(23L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findForQuestApplicationManagement(23L)).thenReturn(List.of(application));
        when(questRepository.save(quest)).thenReturn(quest);

        questService.updateQuest(23L, requestDTO, creator);

        assertEquals(BigDecimal.ZERO, quest.getAwardAmount());
        assertEquals(null, application.getProposedPrice());
    }

    @Test
    void adminUpdateQuestCanChangeCreatorAndStatus() {
        AppUser admin = createUser(1L, "admin");
        admin.setRole(AppUserRole.ADMIN);
        AppUser originalCreator = createUser(3L, "original-creator");
        AppUser newCreator = createUser(2L, "new-creator");

        Quest quest = new Quest();
        quest.setId(31L);
        quest.setCreator(originalCreator);
        quest.setStatus(QuestStatus.OPEN);

        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Updated title")
                .description("Updated description")
                .awardAmount(BigDecimal.valueOf(80))
                .creatorId(newCreator.getId())
                .status(QuestStatus.CANCELLED)
                .build();

        when(questRepository.findForQuestDetail(31L)).thenReturn(Optional.of(quest));
        when(appUserLookupService.requireById(2L, "Creator not found with id 2")).thenReturn(newCreator);
        when(questRepository.save(quest)).thenReturn(quest);

        questService.updateQuest(31L, requestDTO, admin);

        assertEquals(newCreator, quest.getCreator());
        assertEquals(QuestStatus.CANCELLED, quest.getStatus());
        assertEquals("Updated title", quest.getTitle());
        assertEquals("Updated description", quest.getDescription());
    }

    @Test
    void startQuestMovesQuestToInProgressWhenAssigned() {
        AppUser creator = createUser(1L, "creator");
        Quest quest = new Quest();
        quest.setId(10L);
        quest.setCreator(creator);
        quest.setStatus(QuestStatus.ASSIGNED);

        when(questRepository.findForQuestDetail(10L)).thenReturn(Optional.of(quest));
        when(questRepository.save(quest)).thenReturn(quest);

        questService.startQuest(10L, creator);

        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
    }

    @Test
    void completeQuestMovesQuestToCompletedWhenInProgress() {
        AppUser creator = createUser(1L, "creator");
        Quest quest = new Quest();
        quest.setId(11L);
        quest.setCreator(creator);
        quest.setStatus(QuestStatus.IN_PROGRESS);

        when(questRepository.findForQuestDetail(11L)).thenReturn(Optional.of(quest));
        when(questRepository.save(quest)).thenReturn(quest);

        questService.completeQuest(11L, creator);

        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
    }

    @Test
    void startQuestAllowsApprovedApplicantToBeginWork() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = new Quest();
        quest.setId(21L);
        quest.setCreator(creator);
        quest.setStatus(QuestStatus.ASSIGNED);

        QuestApplication approvedApplication = new QuestApplication();
        approvedApplication.setId(41L);

        when(questRepository.findForQuestDetail(21L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findForViewerApplicationWithStatus(21L, 2L, QuestApplicationStatus.APPROVED))
                .thenReturn(Optional.of(approvedApplication));
        when(questRepository.save(quest)).thenReturn(quest);

        questService.startQuest(21L, applicant);

        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
    }

    @Test
    void completeQuestAllowsApprovedApplicantToFinishWork() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = new Quest();
        quest.setId(22L);
        quest.setCreator(creator);
        quest.setStatus(QuestStatus.IN_PROGRESS);

        QuestApplication approvedApplication = new QuestApplication();
        approvedApplication.setId(42L);

        when(questRepository.findForQuestDetail(22L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findForViewerApplicationWithStatus(22L, 2L, QuestApplicationStatus.APPROVED))
                .thenReturn(Optional.of(approvedApplication));
        when(questRepository.save(quest)).thenReturn(quest);

        questService.completeQuest(22L, applicant);

        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
    }

    @Test
    void startQuestThrowsWhenQuestIsNotAssigned() {
        AppUser creator = createUser(1L, "creator");
        Quest quest = new Quest();
        quest.setId(12L);
        quest.setCreator(creator);
        quest.setStatus(QuestStatus.OPEN);

        when(questRepository.findForQuestDetail(12L)).thenReturn(Optional.of(quest));

        assertThrows(ResponseStatusException.class, () -> questService.startQuest(12L, creator));
    }

    @Test
    void updateQuestQueuesTermChangeWhenQuestIsAssigned() {
        AppUser creator = createUser(1L, "creator");
        Instant originalScheduledAt = Instant.now().plusSeconds(7 * 24 * 3600);
        Instant updatedScheduledAt = Instant.now().plusSeconds(9 * 24 * 3600);
        Quest quest = new Quest();
        quest.setId(15L);
        quest.setCreator(creator);
        quest.setStatus(QuestStatus.ASSIGNED);
        quest.setScheduledAt(originalScheduledAt);
        quest.setTermFixed(true);

        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Updated title")
                .description("Updated description")
                .awardAmount(BigDecimal.valueOf(80))
                .scheduledAt(updatedScheduledAt)
                .termFixed(false)
                .build();

        when(questRepository.findForQuestDetail(15L)).thenReturn(Optional.of(quest));
        when(questRepository.save(quest)).thenReturn(quest);

        questService.updateQuest(15L, requestDTO, creator);

        assertEquals(QuestStatus.WAITING_CONFIRMATION, quest.getStatus());
        assertEquals(updatedScheduledAt, quest.getPendingScheduledAt());
        assertEquals(Boolean.FALSE, quest.getPendingTermFixed());
        assertEquals(QuestStatus.ASSIGNED, quest.getTermChangePreviousStatus());
    }

    @Test
    void updateQuestRestoresPreviousStatusWhenAdminEditsWaitingConfirmationQuest() {
        AppUser admin = createUser(1L, "admin");
        admin.setRole(AppUserRole.ADMIN);
        Instant originalScheduledAt = Instant.now().plusSeconds(7 * 24 * 3600);
        Instant pendingScheduledAt = Instant.now().plusSeconds(9 * 24 * 3600);
        Instant updatedScheduledAt = Instant.now().plusSeconds(12 * 24 * 3600);

        Quest quest = new Quest();
        quest.setId(18L);
        quest.setCreator(admin);
        quest.setStatus(QuestStatus.WAITING_CONFIRMATION);
        quest.setScheduledAt(originalScheduledAt);
        quest.setTermFixed(true);
        quest.setPendingScheduledAt(pendingScheduledAt);
        quest.setPendingTermFixed(false);
        quest.setTermChangePreviousStatus(QuestStatus.ASSIGNED);

        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Updated title")
                .description("Updated description")
                .awardAmount(BigDecimal.valueOf(80))
                .scheduledAt(updatedScheduledAt)
                .termFixed(true)
                .status(QuestStatus.WAITING_CONFIRMATION)
                .build();

        when(questRepository.findForQuestDetail(18L)).thenReturn(Optional.of(quest));
        when(questRepository.save(quest)).thenReturn(quest);

        questService.updateQuest(18L, requestDTO, admin);

        assertEquals(QuestStatus.ASSIGNED, quest.getStatus());
        assertEquals(updatedScheduledAt, quest.getScheduledAt());
        assertEquals(true, quest.isTermFixed());
        assertEquals(null, quest.getPendingScheduledAt());
        assertEquals(null, quest.getPendingTermFixed());
        assertEquals(null, quest.getTermChangePreviousStatus());
    }

    @Test
    void updateQuestClearsPendingTermStateWhenAdminChangesStatusAwayFromWaitingConfirmation() {
        AppUser admin = createUser(1L, "admin");
        admin.setRole(AppUserRole.ADMIN);

        Quest quest = new Quest();
        quest.setId(19L);
        quest.setCreator(admin);
        quest.setStatus(QuestStatus.WAITING_CONFIRMATION);
        quest.setScheduledAt(Instant.parse("2026-01-10T10:00:00Z"));
        quest.setTermFixed(true);
        quest.setPendingScheduledAt(Instant.parse("2026-01-12T11:00:00Z"));
        quest.setPendingTermFixed(false);
        quest.setTermChangePreviousStatus(QuestStatus.ASSIGNED);

        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Updated title")
                .description("Updated description")
                .awardAmount(BigDecimal.valueOf(80))
                .status(QuestStatus.OPEN)
                .build();

        when(questRepository.findForQuestDetail(19L)).thenReturn(Optional.of(quest));
        when(questRepository.save(quest)).thenReturn(quest);

        questService.updateQuest(19L, requestDTO, admin);

        assertEquals(QuestStatus.OPEN, quest.getStatus());
        assertEquals(null, quest.getPendingScheduledAt());
        assertEquals(null, quest.getPendingTermFixed());
        assertEquals(null, quest.getTermChangePreviousStatus());
    }

    @Test
    void updateQuestResetsQuestApplicationsWhenAdminReopensQuest() {
        AppUser admin = createUser(1L, "admin");
        admin.setRole(AppUserRole.ADMIN);

        Quest quest = new Quest();
        quest.setId(20L);
        quest.setCreator(admin);
        quest.setStatus(QuestStatus.ASSIGNED);

        QuestApplication approvedApplication = new QuestApplication();
        approvedApplication.setId(301L);
        approvedApplication.setStatus(QuestApplicationStatus.APPROVED);

        QuestApplication declinedApplication = new QuestApplication();
        declinedApplication.setId(302L);
        declinedApplication.setStatus(QuestApplicationStatus.DECLINED);

        QuestApplication pendingApplication = new QuestApplication();
        pendingApplication.setId(303L);
        pendingApplication.setStatus(QuestApplicationStatus.PENDING);

        QuestApplication withdrawnApplication = new QuestApplication();
        withdrawnApplication.setId(304L);
        withdrawnApplication.setStatus(QuestApplicationStatus.WITHDRAWN);

        QuestRequestDTO requestDTO = QuestRequestDTO.builder()
                .title("Updated title")
                .description("Updated description")
                .awardAmount(BigDecimal.valueOf(80))
                .status(QuestStatus.OPEN)
                .build();

        when(questRepository.findForQuestDetail(20L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findForQuestApplicationManagement(20L)).thenReturn(List.of(
                approvedApplication,
                declinedApplication,
                pendingApplication,
                withdrawnApplication
        ));
        when(questRepository.save(quest)).thenReturn(quest);
        when(questApplicationRepository.saveAll(List.of(approvedApplication, declinedApplication, pendingApplication)))
                .thenReturn(List.of(approvedApplication, declinedApplication, pendingApplication));

        questService.updateQuest(20L, requestDTO, admin);

        assertEquals(QuestStatus.OPEN, quest.getStatus());
        assertNotNull(quest.getReopenedAt());
        assertEquals(QuestApplicationStatus.PENDING, approvedApplication.getStatus());
        assertEquals(QuestApplicationStatus.PENDING, declinedApplication.getStatus());
        assertEquals(QuestApplicationStatus.PENDING, pendingApplication.getStatus());
        assertEquals(QuestApplicationStatus.WITHDRAWN, withdrawnApplication.getStatus());
    }

    @Test
    void deleteQuestDeletesQuestAndApplicationsWhenAuthenticatedUserIsOwner() {
        AppUser creator = createUser(1L, "creator");
        Quest quest = new Quest();
        quest.setId(14L);
        quest.setCreator(creator);

        when(questRepository.findForQuestDetail(14L)).thenReturn(Optional.of(quest));

        questService.deleteQuest(14L, creator);

        verify(questApplicationRepository).deleteByQuestId(14L);
        verify(questRepository).deleteById(14L);
    }

    @Test
    void confirmQuestTermChangeAppliesPendingTermForApprovedApplicant() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = new Quest();
        quest.setId(16L);
        quest.setCreator(creator);
        quest.setStatus(QuestStatus.WAITING_CONFIRMATION);
        quest.setScheduledAt(Instant.parse("2026-01-10T10:00:00Z"));
        quest.setTermFixed(true);
        quest.setPendingScheduledAt(Instant.parse("2026-01-12T11:00:00Z"));
        quest.setPendingTermFixed(false);
        quest.setTermChangePreviousStatus(QuestStatus.ASSIGNED);

        com.themuffinman.app.workmarket.model.QuestApplication approvedApplication = new com.themuffinman.app.workmarket.model.QuestApplication();
        approvedApplication.setId(22L);

        when(questRepository.findForQuestDetail(16L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findForViewerApplicationWithStatus(16L, 2L, com.themuffinman.app.workmarket.model.QuestApplicationStatus.APPROVED))
                .thenReturn(Optional.of(approvedApplication));
        when(questRepository.save(quest)).thenReturn(quest);

        questService.confirmQuestTermChange(16L, applicant);

        assertEquals(Instant.parse("2026-01-12T11:00:00Z"), quest.getScheduledAt());
        assertEquals(false, quest.isTermFixed());
        assertEquals(QuestStatus.ASSIGNED, quest.getStatus());
        assertEquals(null, quest.getPendingScheduledAt());
        assertEquals(null, quest.getPendingTermFixed());
    }

    @Test
    void rejectQuestTermChangeRestoresPreviousStatusForApprovedApplicant() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = new Quest();
        quest.setId(17L);
        quest.setCreator(creator);
        quest.setStatus(QuestStatus.WAITING_CONFIRMATION);
        quest.setScheduledAt(Instant.parse("2026-01-10T10:00:00Z"));
        quest.setTermFixed(true);
        quest.setPendingScheduledAt(Instant.parse("2026-01-12T11:00:00Z"));
        quest.setPendingTermFixed(false);
        quest.setTermChangePreviousStatus(QuestStatus.IN_PROGRESS);

        com.themuffinman.app.workmarket.model.QuestApplication approvedApplication = new com.themuffinman.app.workmarket.model.QuestApplication();
        approvedApplication.setId(23L);

        when(questRepository.findForQuestDetail(17L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findForViewerApplicationWithStatus(17L, 2L, com.themuffinman.app.workmarket.model.QuestApplicationStatus.APPROVED))
                .thenReturn(Optional.of(approvedApplication));
        when(questRepository.save(quest)).thenReturn(quest);

        questService.rejectQuestTermChange(17L, applicant);

        assertEquals(Instant.parse("2026-01-10T10:00:00Z"), quest.getScheduledAt());
        assertEquals(true, quest.isTermFixed());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
        assertEquals(null, quest.getPendingScheduledAt());
        assertEquals(null, quest.getPendingTermFixed());
    }

    private AppUser createUser(Long id, String username) {
        AppUser appUser = new AppUser();
        appUser.setId(id);
        appUser.setUsername(username);
        appUser.setEmail(username + "@example.com");
        return appUser;
    }
}
