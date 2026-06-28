package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.workmarket.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.dto.UserReviewRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.QuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.QuestRepository;
import com.themuffinman.app.workmarket.repository.UserReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;

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

        Quest quest = questService.createQuest(questRequest("Fence repair", Instant.parse("2026-07-03T10:00:00Z")), owner);
        questApplicationService.applyForQuest(
                quest.getId(),
                QuestApplicationRequestDTO.builder().message("I can do this tomorrow").proposedPrice(BigDecimal.valueOf(50)).build(),
                worker
        );

        QuestApplication application = questApplicationRepository.findByQuestIdAndApplicantId(quest.getId(), worker.getId()).orElseThrow();
        questApplicationService.approveApplication(quest.getId(), application.getId(), owner);
        assertEquals(QuestStatus.ASSIGNED, questRepository.findByIdWithCreator(quest.getId()).orElseThrow().getStatus());

        questService.startQuest(quest.getId(), owner);
        assertEquals(QuestStatus.IN_PROGRESS, questRepository.findByIdWithCreator(quest.getId()).orElseThrow().getStatus());

        questService.completeQuest(quest.getId(), worker);
        Quest completedQuest = questRepository.findByIdWithCreator(quest.getId()).orElseThrow();
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
        Instant updatedStart = Instant.parse("2026-07-05T15:00:00Z");
        Instant updatedEnd = Instant.parse("2026-07-05T17:00:00Z");

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

        Quest waitingQuest = questRepository.findByIdWithCreator(quest.getId()).orElseThrow();
        assertEquals(QuestStatus.WAITING_CONFIRMATION, waitingQuest.getStatus());
        assertNotNull(waitingQuest.getPendingScheduledAt());

        questService.confirmQuestTermChange(quest.getId(), worker);

        Quest confirmedQuest = questRepository.findByIdWithCreator(quest.getId()).orElseThrow();
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

        Quest rejectedQuest = questRepository.findByIdWithCreator(quest.getId()).orElseThrow();
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

        Quest quest = questService.createQuest(questRequest("Free pickup", Instant.parse("2026-07-07T10:00:00Z")), owner);
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

    private Quest createAssignedQuest(AppUser owner, AppUser worker, String title) {
        Quest quest = questService.createQuest(questRequest(title, Instant.parse("2026-07-04T10:00:00Z")), owner);
        questApplicationService.applyForQuest(
                quest.getId(),
                QuestApplicationRequestDTO.builder().message("Ready to help").proposedPrice(BigDecimal.valueOf(50)).build(),
                worker
        );
        QuestApplication application = questApplicationRepository.findByQuestIdAndApplicantId(quest.getId(), worker.getId()).orElseThrow();
        questApplicationService.approveApplication(quest.getId(), application.getId(), owner);
        return questRepository.findByIdWithCreator(quest.getId()).orElseThrow();
    }

    private QuestRequestDTO questRequest(String title, Instant scheduledAt) {
        return QuestRequestDTO.builder()
                .title(title)
                .description("Scenario flow for " + title)
                .awardAmount(BigDecimal.valueOf(50))
                .scheduledAt(scheduledAt)
                .endsAt(scheduledAt.plusSeconds(7200))
                .termFixed(true)
                .audience(com.themuffinman.app.workmarket.model.QuestAudience.EVERYONE)
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
