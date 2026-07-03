package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.vision.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.vision.dto.QuestRequestDTO;
import com.themuffinman.app.vision.dto.UserReviewRequestDTO;
import com.themuffinman.app.vision.mapper.QuestApplicationMgr;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestApplication;
import com.themuffinman.app.vision.model.QuestApplicationStatus;
import com.themuffinman.app.vision.model.QuestStatus;
import com.themuffinman.app.vision.repository.QuestApplicationRepository;
import com.themuffinman.app.vision.repository.QuestRepository;
import com.themuffinman.app.vision.repository.UserReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class QuestWorkflowScenarioTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private QuestRepository questRepository;

    @Autowired
    private QuestApplicationRepository questApplicationRepository;

    @Autowired
    private QuestApplicationMgr questApplicationMgr;

    @Autowired
    private UserReviewRepository userReviewRepository;

    @Autowired
    private QuestService questService;

    @Autowired
    private QuestApplicationService questApplicationService;

    @Autowired
    private UserReviewService userReviewService;

    @Test
    void createApplyApproveStartCompleteAndReviewScenarioStaysConsistent() {
        AppUser owner = saveUser("owner-scenario");
        AppUser worker = saveUser("worker-scenario");

        Quest quest = questService.createQuest(questRequest("Fence repair", Instant.now().plus(Duration.ofDays(2))), owner);
        questApplicationService.applyForQuest(
                quest.getId(),
                QuestApplicationRequestDTO.builder().message("I can do this tomorrow").proposedPrice(BigDecimal.valueOf(50)).build(),
                worker
        );

        QuestApplication application = questApplicationRepository.findForViewerApplication(quest.getId(), worker.getId()).orElseThrow();
        questApplicationService.approveApplication(quest.getId(), application.getId(), owner);
        assertEquals(QuestStatus.ASSIGNED, questRepository.findForQuestDetail(quest.getId()).orElseThrow().getStatus());

        questService.startQuest(quest.getId(), owner);
        assertEquals(QuestStatus.IN_PROGRESS, questRepository.findForQuestDetail(quest.getId()).orElseThrow().getStatus());

        questService.completeQuest(quest.getId(), worker);
        Quest completedQuest = questRepository.findForQuestDetail(quest.getId()).orElseThrow();
        assertEquals(QuestStatus.COMPLETED, completedQuest.getStatus());

        UserReviewRequestDTO reviewRequest = new UserReviewRequestDTO();
        reviewRequest.setReviewedUserId(worker.getId());
        reviewRequest.setStars(5);
        reviewRequest.setComment("Great work");
        userReviewService.createOrUpdateReview(quest.getId(), reviewRequest, owner);

        assertTrue(userReviewRepository.findByQuestIdAndReviewerIdAndReviewedUserId(quest.getId(), owner.getId(), worker.getId()).isPresent());
    }

    @Test
    void ownerTermChangeThenWorkerConfirmScenarioRestoresAssignedState() {
        AppUser owner = saveUser("owner-confirm");
        AppUser worker = saveUser("worker-confirm");

        Quest quest = createAssignedQuest(owner, worker, "Move sofa");
        Instant updatedStart = Instant.now().plus(Duration.ofDays(3));
        Instant updatedEnd = updatedStart.plus(Duration.ofHours(2));

        questService.updateQuest(
                quest.getId(),
                QuestRequestDTO.builder()
                        .title(quest.getTitle())
                        .description(quest.getDescription())
                        .awardAmount(quest.getAwardAmount())
                        .scheduledAt(updatedStart)
                        .endsAt(updatedEnd)
                        .termFixed(true)
                        .build(),
                owner
        );

        Quest waitingQuest = questRepository.findForQuestDetail(quest.getId()).orElseThrow();
        assertEquals(QuestStatus.WAITING_CONFIRMATION, waitingQuest.getStatus());
        assertNotNull(waitingQuest.getPendingScheduledAt());

        questService.confirmQuestTermChange(quest.getId(), worker);

        Quest confirmedQuest = questRepository.findForQuestDetail(quest.getId()).orElseThrow();
        assertEquals(QuestStatus.ASSIGNED, confirmedQuest.getStatus());
        assertEquals(updatedStart, confirmedQuest.getScheduledAt());
        assertEquals(updatedEnd, confirmedQuest.getEndsAt());
    }

    @Test
    void ownerTermChangeThenWorkerRejectScenarioRestoresPreviousSchedule() {
        AppUser owner = saveUser("owner-reject");
        AppUser worker = saveUser("worker-reject");

        Quest quest = createAssignedQuest(owner, worker, "Paint fence");
        Instant originalStart = quest.getScheduledAt();

        questService.updateQuest(
                quest.getId(),
                QuestRequestDTO.builder()
                        .title(quest.getTitle())
                        .description(quest.getDescription())
                        .awardAmount(quest.getAwardAmount())
                        .scheduledAt(Instant.parse("2026-07-06T16:00:00Z"))
                        .endsAt(Instant.parse("2026-07-06T18:00:00Z"))
                        .termFixed(true)
                        .build(),
                owner
        );

        questService.rejectQuestTermChange(quest.getId(), worker);

        Quest rejectedQuest = questRepository.findForQuestDetail(quest.getId()).orElseThrow();
        assertEquals(QuestStatus.ASSIGNED, rejectedQuest.getStatus());
        assertEquals(originalStart, rejectedQuest.getScheduledAt());
        assertNull(rejectedQuest.getPendingScheduledAt());
    }

    @Test
    void deleteQuestScenarioFailsClosedWhenTargetDoesNotExist() {
        AppUser owner = saveUser("owner-delete-missing");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> questService.deleteQuest(999_999L, owner));

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Quest not found"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void questDetailResponseLoadsMyApplicationOutsideOuterTransaction() {
        AppUser owner = saveUser("owner-detail-outside-tx");
        AppUser worker = saveUser("worker-detail-outside-tx");

        Quest quest = questService.createQuest(questRequest("Free pickup", Instant.now().plus(Duration.ofDays(4))), owner);
        quest.setAwardAmount(BigDecimal.ZERO);
        quest = questRepository.save(quest);
        questApplicationService.applyForQuest(
                quest.getId(),
                QuestApplicationRequestDTO.builder().message("I can help").build(),
                worker
        );

        var detail = questService.getQuestDetailResponseById(quest.getId(), worker);

        assertNotNull(detail.getMyApplication());
        assertEquals(worker.getId(), detail.getMyApplication().getApplicantId());
        assertEquals(worker.getUsername(), detail.getMyApplication().getApplicantUsername());
        assertEquals("Free pickup", detail.getMyApplication().getQuestTitle());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void questApplicationFetchProfilesMapOutsideOuterTransaction() {
        AppUser owner = saveUser("owner-application-fetch");
        AppUser worker = saveUser("worker-application-fetch");

        Quest quest = questService.createQuest(questRequest("Fetch profile quest", Instant.now().plus(Duration.ofDays(5))), owner);
        questApplicationService.applyForQuest(
                quest.getId(),
                QuestApplicationRequestDTO.builder().message("I can help").proposedPrice(BigDecimal.valueOf(50)).build(),
                worker
        );
        QuestApplication pendingApplication = questApplicationRepository.findForViewerApplication(quest.getId(), worker.getId()).orElseThrow();
        questApplicationService.approveApplication(quest.getId(), pendingApplication.getId(), owner);

        var byQuestDto = questApplicationMgr.toDto(questApplicationRepository.findForQuestApplicationManagement(quest.getId()).getFirst());
        var byStatusDto = questApplicationMgr.toDto(questApplicationRepository.findForQuestApplicationsByStatus(quest.getId(), QuestApplicationStatus.APPROVED).getFirst());
        var adminDto = questApplicationMgr.toDto(questApplicationRepository.findForAdminApplicationList().stream()
                .filter(application -> application.getId().equals(pendingApplication.getId()))
                .findFirst()
                .orElseThrow());

        assertEquals(owner.getUsername(), byQuestDto.getQuestCreatorUsername());
        assertEquals(worker.getUsername(), byStatusDto.getApplicantUsername());
        assertEquals("Fetch profile quest", adminDto.getQuestTitle());
    }

    private Quest createAssignedQuest(AppUser owner, AppUser worker, String title) {
        Quest quest = questService.createQuest(questRequest(title, Instant.now().plus(Duration.ofDays(1))), owner);
        questApplicationService.applyForQuest(
                quest.getId(),
                QuestApplicationRequestDTO.builder().message("Ready to help").proposedPrice(BigDecimal.valueOf(50)).build(),
                worker
        );
        QuestApplication application = questApplicationRepository.findForViewerApplication(quest.getId(), worker.getId()).orElseThrow();
        questApplicationService.approveApplication(quest.getId(), application.getId(), owner);
        return questRepository.findForQuestDetail(quest.getId()).orElseThrow();
    }

    private QuestRequestDTO questRequest(String title, Instant scheduledAt) {
        return QuestRequestDTO.builder()
                .title(title)
                .description("Scenario flow for " + title)
                .awardAmount(BigDecimal.valueOf(50))
                .scheduledAt(scheduledAt)
                .endsAt(scheduledAt.plusSeconds(7200))
                .termFixed(true)
                .audience(com.themuffinman.app.vision.model.QuestAudience.EVERYONE)
                .build();
    }

    private AppUser saveUser(String slug) {
        AppUser user = new AppUser();
        user.setEmail(slug + "@example.com");
        user.setUsername(slug);
        user.setPasswordHash("encoded-password");
        user.setRole(AppUserRole.USER);
        return appUserRepository.save(user);
    }
}
