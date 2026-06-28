package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.QuestApplicationListResponseDTO;
import com.themuffinman.app.workmarket.dto.AdminQuestApplicationUpdateRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.workmarket.mapper.QuestApplicationMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.QuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.QuestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestApplicationServiceTest {

    @Mock
    private QuestApplicationRepository questApplicationRepository;

    @Mock
    private QuestRepository questRepository;

    @Mock
    private QuestApplicationMgr questApplicationMgr;

    @Mock
    private QuestNewsService questNewsService;

    @Mock
    private QuestVisibilityService questVisibilityService;

    @Spy
    private WorkmarketPresentationHelper presentationHelper = new WorkmarketPresentationHelper();

    @InjectMocks
    private QuestApplicationService questApplicationService;

    @Test
    void applyForQuestUsesAuthenticatedUserAsApplicant() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        QuestApplicationRequestDTO requestDTO = QuestApplicationRequestDTO.builder()
                .message("I can help")
                .proposedPrice(BigDecimal.valueOf(25))
                .build();
        QuestApplication application = new QuestApplication();
        QuestApplication savedApplication = new QuestApplication();
        QuestApplicationResponseDTO responseDTO = QuestApplicationResponseDTO.builder()
                .id(100L)
                .applicantId(applicant.getId())
                .build();

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questVisibilityService.canViewQuest(applicant, quest)).thenReturn(true);
        when(questApplicationRepository.existsByQuestIdAndApplicantId(7L, 2L)).thenReturn(false);
        when(questApplicationMgr.toEntity(requestDTO, quest, applicant)).thenReturn(application);
        when(questApplicationRepository.save(application)).thenReturn(savedApplication);
        when(questApplicationMgr.toDto(savedApplication)).thenReturn(responseDTO);

        QuestApplicationResponseDTO result = questApplicationService.applyForQuest(7L, requestDTO, applicant);

        assertEquals(100L, result.getId());
        verify(questApplicationMgr).toEntity(requestDTO, quest, applicant);
        verify(questNewsService).notifyApplicationCreated(quest, application, applicant);
    }

    @Test
    void applyForQuestThrowsWhenApplicantIsQuestCreator() {
        AppUser creator = createUser(1L, "creator");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        QuestApplicationRequestDTO requestDTO = QuestApplicationRequestDTO.builder()
                .message("I can help")
                .build();

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questVisibilityService.canViewQuest(creator, quest)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> questApplicationService.applyForQuest(7L, requestDTO, creator));
    }

    @Test
    void applyForQuestThrowsWhenQuestIsNotOpen() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = createQuest(7L, creator, QuestStatus.COMPLETED);
        QuestApplicationRequestDTO requestDTO = QuestApplicationRequestDTO.builder()
                .message("I can help")
                .build();

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questVisibilityService.canViewQuest(applicant, quest)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> questApplicationService.applyForQuest(7L, requestDTO, applicant));
    }

    @Test
    void applyForQuestRejectsEmptyRichTextMessage() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        QuestApplicationRequestDTO requestDTO = QuestApplicationRequestDTO.builder()
                .message("<p><br></p>")
                .proposedPrice(BigDecimal.valueOf(25))
                .build();

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questVisibilityService.canViewQuest(applicant, quest)).thenReturn(true);
        when(questApplicationRepository.existsByQuestIdAndApplicantId(7L, 2L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> questApplicationService.applyForQuest(7L, requestDTO, applicant));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void applyForQuestAllowsMissingPriceForFreeQuest() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        quest.setAwardAmount(BigDecimal.ZERO);
        QuestApplicationRequestDTO requestDTO = QuestApplicationRequestDTO.builder()
                .message("I can help")
                .build();
        QuestApplication application = new QuestApplication();
        QuestApplication savedApplication = new QuestApplication();
        QuestApplicationResponseDTO responseDTO = QuestApplicationResponseDTO.builder()
                .id(100L)
                .applicantId(applicant.getId())
                .build();

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questVisibilityService.canViewQuest(applicant, quest)).thenReturn(true);
        when(questApplicationRepository.existsByQuestIdAndApplicantId(7L, 2L)).thenReturn(false);
        when(questApplicationMgr.toEntity(requestDTO, quest, applicant)).thenReturn(application);
        when(questApplicationRepository.save(application)).thenReturn(savedApplication);
        when(questApplicationMgr.toDto(savedApplication)).thenReturn(responseDTO);

        QuestApplicationResponseDTO result = questApplicationService.applyForQuest(7L, requestDTO, applicant);

        assertEquals(100L, result.getId());
        verify(questApplicationMgr).toEntity(requestDTO, quest, applicant);
    }

    @Test
    void applyForQuestRejectsPriceForFreeQuest() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        quest.setAwardAmount(BigDecimal.ZERO);
        QuestApplicationRequestDTO requestDTO = QuestApplicationRequestDTO.builder()
                .message("I can help")
                .proposedPrice(BigDecimal.valueOf(25))
                .build();

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questVisibilityService.canViewQuest(applicant, quest)).thenReturn(true);
        when(questApplicationRepository.existsByQuestIdAndApplicantId(7L, 2L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> questApplicationService.applyForQuest(7L, requestDTO, applicant));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"Free quest applications cannot have a proposed price\"", exception.getMessage());
    }

    @Test
    void getApplicationsForQuestThrowsWhenAuthenticatedUserIsNotOwner() {
        AppUser creator = createUser(1L, "creator");
        AppUser otherUser = createUser(3L, "other");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));

        assertThrows(ResponseStatusException.class, () -> questApplicationService.getApplicationsForQuest(7L, otherUser));
    }

    @Test
    void getApplicationsForQuestReturnsApplicationsForOwner() {
        AppUser creator = createUser(1L, "creator");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        QuestApplication application = new QuestApplication();
        QuestApplicationResponseDTO responseDTO = QuestApplicationResponseDTO.builder()
                .id(22L)
                .build();

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findByQuestId(7L)).thenReturn(List.of(application));
        when(questApplicationMgr.toDto(application)).thenReturn(responseDTO);

        List<QuestApplicationResponseDTO> result = questApplicationService.getApplicationsForQuest(7L, creator);

        assertEquals(1, result.size());
        assertEquals(22L, result.getFirst().getId());
    }

    @Test
    void getApplicationsViewForQuestShowsApprovedAsFeaturedByDefault() {
        AppUser creator = createUser(1L, "creator");
        AppUser approvedApplicant = createUser(2L, "approved");
        AppUser declinedApplicant = createUser(3L, "declined");
        Quest quest = createQuest(7L, creator, QuestStatus.ASSIGNED);
        QuestApplication approvedApplication = createApplication(11L, quest, approvedApplicant, QuestApplicationStatus.APPROVED);
        QuestApplication declinedApplication = createApplication(12L, quest, declinedApplicant, QuestApplicationStatus.DECLINED);

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findByQuestId(7L)).thenReturn(List.of(approvedApplication, declinedApplication));
        when(questApplicationMgr.toDto(approvedApplication)).thenReturn(QuestApplicationResponseDTO.builder().id(11L).status(QuestApplicationStatus.APPROVED).build());
        when(questApplicationMgr.toDto(declinedApplication)).thenReturn(QuestApplicationResponseDTO.builder().id(12L).status(QuestApplicationStatus.DECLINED).build());

        QuestApplicationsViewDTO result = questApplicationService.getApplicationsViewForQuest(7L, creator, false);

        assertEquals(11L, result.getFeaturedApplication().getId());
        assertEquals(1, result.getApprovedApplications().size());
        assertEquals(0, result.getVisibleApplications().size());
        assertEquals(0, result.getPendingApplicationCount());
        assertEquals(null, result.getOldestPendingApplicationId());
        assertEquals(1, result.getHiddenApplicationsCount());
        assertEquals(11L, result.getSelectedApplicationId());
        assertEquals(true, result.isCanRevealHiddenApplications());
        assertEquals("Show other applications", result.getRevealLabel());
    }

    @Test
    void getApplicationsViewForQuestHidesCancelledApplicationsUntilExpanded() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "worker");
        Quest quest = createQuest(7L, creator, QuestStatus.CANCELLED);
        QuestApplication application = createApplication(11L, quest, applicant, QuestApplicationStatus.DECLINED);

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findByQuestId(7L)).thenReturn(List.of(application));
        when(questApplicationMgr.toDto(application)).thenReturn(QuestApplicationResponseDTO.builder().id(11L).status(QuestApplicationStatus.DECLINED).build());

        QuestApplicationsViewDTO result = questApplicationService.getApplicationsViewForQuest(7L, creator, false);

        assertEquals(0, result.getVisibleApplications().size());
        assertEquals(1, result.getHiddenApplicationsCount());
        assertEquals(null, result.getSelectedApplicationId());
        assertEquals(true, result.isCanRevealHiddenApplications());
        assertEquals("Show all applications", result.getRevealLabel());
    }

    @Test
    void getApplicationsViewForQuestSelectsFirstVisibleApplicationWhenNoFeaturedExists() {
        AppUser creator = createUser(1L, "creator");
        AppUser firstApplicant = createUser(2L, "first");
        AppUser secondApplicant = createUser(3L, "second");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        QuestApplication newerApplication = createApplication(12L, quest, secondApplicant, QuestApplicationStatus.PENDING);
        QuestApplication olderApplication = createApplication(11L, quest, firstApplicant, QuestApplicationStatus.PENDING);

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findByQuestId(7L)).thenReturn(List.of(olderApplication, newerApplication));
        when(questApplicationMgr.toDto(newerApplication)).thenReturn(QuestApplicationResponseDTO.builder().id(12L).status(QuestApplicationStatus.PENDING).build());
        when(questApplicationMgr.toDto(olderApplication)).thenReturn(QuestApplicationResponseDTO.builder().id(11L).status(QuestApplicationStatus.PENDING).build());

        QuestApplicationsViewDTO result = questApplicationService.getApplicationsViewForQuest(7L, creator, false);

        assertEquals(2, result.getVisibleApplications().size());
        assertEquals(2, result.getPendingApplicationCount());
        assertEquals(11L, result.getOldestPendingApplicationId());
        assertEquals(12L, result.getSelectedApplicationId());
    }

    @Test
    void getApplicationsViewForQuestExposesOldestPendingSelectionByCreatedAtThenId() {
        AppUser creator = createUser(1L, "creator");
        AppUser firstApplicant = createUser(2L, "first");
        AppUser secondApplicant = createUser(3L, "second");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        QuestApplication sameTimeHigherId = createApplication(12L, quest, secondApplicant, QuestApplicationStatus.PENDING);
        QuestApplication olderApplication = createApplication(11L, quest, firstApplicant, QuestApplicationStatus.PENDING);
        olderApplication.setCreatedAt(Instant.parse("2026-06-01T09:00:00Z"));
        sameTimeHigherId.setCreatedAt(Instant.parse("2026-06-01T10:00:00Z"));

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findByQuestId(7L)).thenReturn(List.of(sameTimeHigherId, olderApplication));
        when(questApplicationMgr.toDto(sameTimeHigherId)).thenReturn(QuestApplicationResponseDTO.builder().id(12L).status(QuestApplicationStatus.PENDING).build());
        when(questApplicationMgr.toDto(olderApplication)).thenReturn(QuestApplicationResponseDTO.builder().id(11L).status(QuestApplicationStatus.PENDING).build());

        QuestApplicationsViewDTO result = questApplicationService.getApplicationsViewForQuest(7L, creator, false);

        assertEquals(11L, result.getOldestPendingApplicationId());
    }

    @Test
    void getApplicationsViewForQuestShowsAllVisibleApplicationsWhenExpanded() {
        AppUser creator = createUser(1L, "creator");
        AppUser approvedApplicant = createUser(2L, "approved");
        AppUser declinedApplicant = createUser(3L, "declined");
        Quest quest = createQuest(7L, creator, QuestStatus.ASSIGNED);
        QuestApplication approvedApplication = createApplication(11L, quest, approvedApplicant, QuestApplicationStatus.APPROVED);
        QuestApplication declinedApplication = createApplication(12L, quest, declinedApplicant, QuestApplicationStatus.DECLINED);

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findByQuestId(7L)).thenReturn(List.of(approvedApplication, declinedApplication));
        when(questApplicationMgr.toDto(approvedApplication)).thenReturn(QuestApplicationResponseDTO.builder().id(11L).status(QuestApplicationStatus.APPROVED).build());
        when(questApplicationMgr.toDto(declinedApplication)).thenReturn(QuestApplicationResponseDTO.builder().id(12L).status(QuestApplicationStatus.DECLINED).build());

        QuestApplicationsViewDTO result = questApplicationService.getApplicationsViewForQuest(7L, creator, true);

        assertEquals(1, result.getVisibleApplications().size());
        assertEquals(12L, result.getVisibleApplications().getFirst().getId());
        assertEquals(1, result.getApprovedApplications().size());
        assertEquals(0, result.getHiddenApplicationsCount());
        assertEquals(true, result.isShowingAllApplications());
    }

    @Test
    void getPublicApprovedApplicationsViewReturnsReadonlyApprovedApplicantsOnly() {
        AppUser creator = createUser(1L, "creator");
        AppUser approvedApplicant = createUser(2L, "approved");
        Quest quest = createQuest(7L, creator, QuestStatus.ASSIGNED);
        QuestApplication approvedApplication = createApplication(11L, quest, approvedApplicant, QuestApplicationStatus.APPROVED);

        when(questApplicationRepository.findByQuestIdAndStatus(7L, QuestApplicationStatus.APPROVED)).thenReturn(List.of(approvedApplication));
        when(questApplicationMgr.toDto(approvedApplication)).thenReturn(QuestApplicationResponseDTO.builder()
                .id(11L)
                .status(QuestApplicationStatus.APPROVED)
                .applicantUsername("approved")
                .build());

        QuestApplicationsViewDTO result = questApplicationService.getPublicApprovedApplicationsViewForQuest(7L);

        assertEquals(1, result.getApprovedApplications().size());
        assertEquals(0, result.getVisibleApplications().size());
        assertEquals(0, result.getPendingApplicationCount());
        assertEquals(null, result.getOldestPendingApplicationId());
        assertEquals(false, result.isCanRevealHiddenApplications());
        assertEquals("approved", result.getApprovedApplications().getFirst().getApplicantUsername());
    }

    @Test
    void approveApplicationKeepsQuestOpenUntilAllWorkerSpotsAreFilled() {
        AppUser creator = createUser(1L, "creator");
        AppUser firstApplicant = createUser(2L, "first");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        quest.setAssigneeTarget(2);
        QuestApplication firstApplication = createApplication(11L, quest, firstApplicant, QuestApplicationStatus.PENDING);
        QuestApplicationResponseDTO responseDTO = QuestApplicationResponseDTO.builder()
                .id(11L)
                .status(QuestApplicationStatus.APPROVED)
                .build();

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findByIdAndQuestId(11L, 7L)).thenReturn(Optional.of(firstApplication));
        when(questApplicationRepository.countByQuestIdAndStatus(7L, QuestApplicationStatus.APPROVED)).thenReturn(0L);
        when(questApplicationRepository.save(firstApplication)).thenReturn(firstApplication);
        when(questApplicationMgr.toDto(firstApplication)).thenReturn(responseDTO);

        QuestApplicationResponseDTO result = questApplicationService.approveApplication(7L, 11L, creator);

        assertEquals(QuestApplicationStatus.APPROVED, result.getStatus());
        assertEquals(QuestStatus.OPEN, quest.getStatus());
    }

    @Test
    void searchApplicationsForAdminFiltersAndPaginates() {
        AppUser admin = createUser(1L, "admin");
        admin.setRole(AppUserRole.ADMIN);

        QuestApplication first = new QuestApplication();
        first.setCreatedAt(java.time.Instant.parse("2026-01-02T10:00:00Z"));
        QuestApplication second = new QuestApplication();
        second.setCreatedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));

        QuestApplicationResponseDTO firstDto = QuestApplicationResponseDTO.builder()
                .id(11L)
                .questTitle("Paint fence")
                .applicantUsername("alex")
                .questStatus(QuestStatus.OPEN)
                .status(QuestApplicationStatus.PENDING)
                .message("Can do it")
                .createdAt(java.time.Instant.parse("2026-01-02T10:00:00Z"))
                .build();
        QuestApplicationResponseDTO secondDto = QuestApplicationResponseDTO.builder()
                .id(12L)
                .questTitle("Move boxes")
                .applicantUsername("jamie")
                .questStatus(QuestStatus.ASSIGNED)
                .status(QuestApplicationStatus.APPROVED)
                .message("Already approved")
                .createdAt(java.time.Instant.parse("2026-01-01T10:00:00Z"))
                .build();

        when(questApplicationRepository.findAllDetailed()).thenReturn(List.of(first, second));
        when(questApplicationMgr.toDto(first)).thenReturn(firstDto);
        when(questApplicationMgr.toDto(second)).thenReturn(secondDto);

        QuestApplicationListResponseDTO result = questApplicationService.searchApplicationsForAdmin(
                admin,
                "paint",
                QuestApplicationStatus.PENDING,
                0,
                10
        );

        assertEquals(1, result.getItems().size());
        assertEquals(11L, result.getItems().getFirst().getId());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalItems());
    }

    @Test
    void approveApplicationSetsApplicationAndQuestStatus() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        AppUser otherApplicant = createUser(3L, "other");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        QuestApplication approvedApplication = createApplication(11L, quest, applicant, QuestApplicationStatus.PENDING);
        QuestApplication otherApplication = createApplication(12L, quest, otherApplicant, QuestApplicationStatus.PENDING);
        QuestApplicationResponseDTO responseDTO = QuestApplicationResponseDTO.builder()
                .id(11L)
                .status(QuestApplicationStatus.APPROVED)
                .build();

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findByIdAndQuestId(11L, 7L)).thenReturn(Optional.of(approvedApplication));
        when(questApplicationRepository.findByQuestIdAndStatus(7L, QuestApplicationStatus.PENDING)).thenReturn(List.of(approvedApplication, otherApplication));
        when(questApplicationRepository.save(approvedApplication)).thenReturn(approvedApplication);
        when(questApplicationMgr.toDto(approvedApplication)).thenReturn(responseDTO);

        QuestApplicationResponseDTO result = questApplicationService.approveApplication(7L, 11L, creator);

        assertEquals(QuestApplicationStatus.APPROVED, result.getStatus());
        assertEquals(QuestStatus.ASSIGNED, quest.getStatus());
        assertEquals(QuestApplicationStatus.APPROVED, approvedApplication.getStatus());
        assertEquals(QuestApplicationStatus.DECLINED, otherApplication.getStatus());
        verify(questNewsService).notifyApplicationApproved(quest, approvedApplication, creator);
        verify(questNewsService).notifyApplicationDeclined(quest, otherApplication, creator);
    }

    @Test
    void declineApplicationSetsStatusToDeclined() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        QuestApplication application = createApplication(11L, quest, applicant, QuestApplicationStatus.PENDING);
        QuestApplicationResponseDTO responseDTO = QuestApplicationResponseDTO.builder()
                .id(11L)
                .status(QuestApplicationStatus.DECLINED)
                .build();

        when(questRepository.findByIdWithCreator(7L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findByIdAndQuestId(11L, 7L)).thenReturn(Optional.of(application));
        when(questApplicationRepository.save(application)).thenReturn(application);
        when(questApplicationMgr.toDto(application)).thenReturn(responseDTO);

        QuestApplicationResponseDTO result = questApplicationService.declineApplication(7L, 11L, creator);

        assertEquals(QuestApplicationStatus.DECLINED, result.getStatus());
        assertEquals(QuestApplicationStatus.DECLINED, application.getStatus());
    }

    @Test
    void updateApplicationForAdminCanEditMessageAndPrice() {
        AppUser admin = createUser(1L, "admin");
        admin.setRole(AppUserRole.ADMIN);
        AppUser creator = createUser(2L, "creator");
        AppUser applicant = createUser(3L, "applicant");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        QuestApplication application = createApplication(11L, quest, applicant, QuestApplicationStatus.PENDING);
        application.setMessage("Old");
        application.setProposedPrice(BigDecimal.TEN);

        AdminQuestApplicationUpdateRequestDTO dto = AdminQuestApplicationUpdateRequestDTO.builder()
                .message("<p>New</p>")
                .proposedPrice(BigDecimal.valueOf(55))
                .build();
        QuestApplicationResponseDTO responseDTO = QuestApplicationResponseDTO.builder()
                .id(11L)
                .status(QuestApplicationStatus.PENDING)
                .build();

        when(questApplicationRepository.findByIdDetailed(11L)).thenReturn(Optional.of(application));
        when(questApplicationRepository.save(application)).thenReturn(application);
        when(questApplicationMgr.toDto(application)).thenReturn(responseDTO);

        QuestApplicationResponseDTO result = questApplicationService.updateApplicationForAdmin(11L, dto, admin);

        assertEquals("<p>New</p>", application.getMessage());
        assertEquals(BigDecimal.valueOf(55), application.getProposedPrice());
        assertEquals(11L, result.getId());
    }

    @Test
    void updateApplicationForAdminRejectsPriceForFreeQuest() {
        AppUser admin = createUser(1L, "admin");
        admin.setRole(AppUserRole.ADMIN);
        AppUser creator = createUser(2L, "creator");
        AppUser applicant = createUser(3L, "applicant");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        quest.setAwardAmount(BigDecimal.ZERO);
        QuestApplication application = createApplication(11L, quest, applicant, QuestApplicationStatus.PENDING);

        AdminQuestApplicationUpdateRequestDTO dto = AdminQuestApplicationUpdateRequestDTO.builder()
                .proposedPrice(BigDecimal.valueOf(55))
                .build();

        when(questApplicationRepository.findByIdDetailed(11L)).thenReturn(Optional.of(application));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> questApplicationService.updateApplicationForAdmin(11L, dto, admin));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void deleteApplicationForAdminReopensAssignedQuestWhenApprovedApplicationIsRemoved() {
        AppUser admin = createUser(1L, "admin");
        admin.setRole(AppUserRole.ADMIN);
        Quest quest = createQuest(7L, createUser(2L, "creator"), QuestStatus.ASSIGNED);
        QuestApplication application = createApplication(11L, quest, createUser(3L, "worker"), QuestApplicationStatus.APPROVED);

        when(questApplicationRepository.findByIdDetailed(11L)).thenReturn(Optional.of(application));

        questApplicationService.deleteApplicationForAdmin(11L, admin);

        assertEquals(QuestStatus.OPEN, quest.getStatus());
        verify(questRepository).save(quest);
        verify(questApplicationRepository).delete(application);
    }

    @Test
    void withdrawMyApplicationSetsStatusToWithdrawn() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        QuestApplication application = createApplication(11L, quest, applicant, QuestApplicationStatus.PENDING);
        QuestApplicationResponseDTO responseDTO = QuestApplicationResponseDTO.builder()
                .id(11L)
                .status(QuestApplicationStatus.WITHDRAWN)
                .build();

        when(questApplicationRepository.findByQuestIdAndApplicantId(7L, 2L)).thenReturn(Optional.of(application));
        when(questApplicationRepository.save(application)).thenReturn(application);
        when(questApplicationMgr.toDto(application)).thenReturn(responseDTO);

        QuestApplicationResponseDTO result = questApplicationService.withdrawMyApplication(7L, applicant);

        assertEquals(QuestApplicationStatus.WITHDRAWN, result.getStatus());
        assertEquals(QuestApplicationStatus.WITHDRAWN, application.getStatus());
        verify(questNewsService).notifyApplicationWithdrawn(quest, application, applicant);
    }

    @Test
    void withdrawMyApplicationThrowsWhenApplicationIsNotPending() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = createQuest(7L, creator, QuestStatus.OPEN);
        QuestApplication application = createApplication(11L, quest, applicant, QuestApplicationStatus.APPROVED);

        when(questApplicationRepository.findByQuestIdAndApplicantId(7L, 2L)).thenReturn(Optional.of(application));

        assertThrows(ResponseStatusException.class, () -> questApplicationService.withdrawMyApplication(7L, applicant));
    }

    @Test
    void updateMyApplicationPreservesRichTextMessageWhitespace() {
        AppUser creator = createUser(1L, "creator");
        AppUser applicant = createUser(2L, "applicant");
        Quest quest = createQuest(8L, creator, QuestStatus.OPEN);
        QuestApplication application = createApplication(13L, quest, applicant, QuestApplicationStatus.PENDING);
        QuestApplicationRequestDTO requestDTO = QuestApplicationRequestDTO.builder()
                .message("<p>  I can help</p>")
                .proposedPrice(BigDecimal.valueOf(25))
                .build();
        QuestApplicationResponseDTO responseDTO = QuestApplicationResponseDTO.builder()
                .id(13L)
                .build();

        when(questApplicationRepository.findByQuestIdAndApplicantId(8L, applicant.getId())).thenReturn(Optional.of(application));
        when(questApplicationRepository.save(application)).thenReturn(application);
        when(questApplicationMgr.toDto(application)).thenReturn(responseDTO);

        questApplicationService.updateMyApplication(8L, requestDTO, applicant);

        assertEquals("<p>&nbsp;&nbsp;I can help</p>", application.getMessage());
    }

    private AppUser createUser(Long id, String username) {
        AppUser appUser = new AppUser();
        appUser.setId(id);
        appUser.setUsername(username);
        appUser.setEmail(username + "@example.com");
        return appUser;
    }

    private Quest createQuest(Long id, AppUser creator, QuestStatus status) {
        Quest quest = new Quest();
        quest.setId(id);
        quest.setCreator(creator);
        quest.setStatus(status);
        quest.setTitle("Quest title");
        return quest;
    }

    private QuestApplication createApplication(Long id, Quest quest, AppUser applicant, QuestApplicationStatus status) {
        QuestApplication application = new QuestApplication();
        application.setId(id);
        application.setQuest(quest);
        application.setApplicant(applicant);
        application.setStatus(status);
        application.setMessage("Message");
        application.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z").plusSeconds(id));
        return application;
    }
}
