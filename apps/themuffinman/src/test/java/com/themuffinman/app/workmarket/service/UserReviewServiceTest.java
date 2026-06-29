package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.UserReviewRequestDTO;
import com.themuffinman.app.workmarket.dto.UserReviewResponseDTO;
import com.themuffinman.app.workmarket.mapper.UserReviewMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.model.ReviewRole;
import com.themuffinman.app.workmarket.model.UserReview;
import com.themuffinman.app.workmarket.repository.QuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.QuestRepository;
import com.themuffinman.app.workmarket.repository.UserReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserReviewServiceTest {

    @Mock
    private QuestRepository questRepository;

    @Mock
    private QuestApplicationRepository questApplicationRepository;

    @Mock
    private UserReviewRepository userReviewRepository;

    @Mock
    private UserReviewMgr userReviewMgr;

    @InjectMocks
    private UserReviewService userReviewService;

    @Test
    void approvedWorkerCanReviewEmployerAfterCompletedQuest() {
        AppUser employer = createUser(1L, "employer");
        AppUser worker = createUser(2L, "worker");
        Quest quest = createCompletedQuest(50L, employer);
        QuestApplication approvedApplication = createApprovedApplication(quest, worker);

        UserReviewRequestDTO dto = new UserReviewRequestDTO();
        dto.setReviewedUserId(1L);
        dto.setStars(5);
        dto.setComment("Great employer");

        when(questRepository.findForQuestDetail(50L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findForViewerApplicationWithStatus(50L, 2L, QuestApplicationStatus.APPROVED))
                .thenReturn(Optional.of(approvedApplication));
        when(userReviewRepository.save(any(UserReview.class))).thenAnswer(invocation -> {
            UserReview review = invocation.getArgument(0);
            review.setId(7L);
            return review;
        });
        when(userReviewMgr.toDto(any(UserReview.class))).thenAnswer(invocation -> {
            UserReview review = invocation.getArgument(0);
            return UserReviewResponseDTO.builder()
                    .id(review.getId())
                    .reviewedUserId(review.getReviewedUser().getId())
                    .reviewedRole(review.getReviewedRole())
                    .stars(review.getStars())
                    .comment(review.getComment())
                    .build();
        });

        UserReviewResponseDTO result = userReviewService.createOrUpdateReview(50L, dto, worker);

        assertEquals(1L, result.getReviewedUserId());
        assertEquals(ReviewRole.EMPLOYER, result.getReviewedRole());
        assertEquals(5, result.getStars());
        assertEquals("Great employer", result.getComment());
    }

    @Test
    void employerCanReviewApprovedWorker() {
        AppUser employer = createUser(1L, "employer");
        AppUser worker = createUser(2L, "worker");
        Quest quest = createCompletedQuest(80L, employer);
        QuestApplication approvedApplication = createApprovedApplication(quest, worker);

        UserReviewRequestDTO dto = new UserReviewRequestDTO();
        dto.setReviewedUserId(2L);
        dto.setStars(4);
        dto.setComment("Reliable worker");

        when(questRepository.findForQuestDetail(80L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findForViewerApplicationWithStatus(80L, 2L, QuestApplicationStatus.APPROVED))
                .thenReturn(Optional.of(approvedApplication));
        when(userReviewRepository.save(any(UserReview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userReviewMgr.toDto(any(UserReview.class))).thenAnswer(invocation -> {
            UserReview review = invocation.getArgument(0);
            return UserReviewResponseDTO.builder()
                    .reviewedUserId(review.getReviewedUser().getId())
                    .reviewedRole(review.getReviewedRole())
                    .stars(review.getStars())
                    .build();
        });

        UserReviewResponseDTO result = userReviewService.createOrUpdateReview(80L, dto, employer);

        assertEquals(2L, result.getReviewedUserId());
        assertEquals(ReviewRole.WORKER, result.getReviewedRole());
        assertEquals(4, result.getStars());
    }

    @Test
    void reviewIsRejectedBeforeQuestCompletion() {
        AppUser employer = createUser(1L, "employer");
        AppUser worker = createUser(2L, "worker");
        Quest quest = new Quest();
        quest.setId(90L);
        quest.setCreator(employer);
        quest.setStatus(QuestStatus.IN_PROGRESS);

        UserReviewRequestDTO dto = new UserReviewRequestDTO();
        dto.setReviewedUserId(1L);
        dto.setStars(5);

        when(questRepository.findForQuestDetail(90L)).thenReturn(Optional.of(quest));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userReviewService.createOrUpdateReview(90L, dto, worker)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void reviewRejectsUserWhoDidNotWorkOnQuest() {
        AppUser employer = createUser(1L, "employer");
        AppUser randomUser = createUser(3L, "random");
        Quest quest = createCompletedQuest(100L, employer);

        UserReviewRequestDTO dto = new UserReviewRequestDTO();
        dto.setReviewedUserId(1L);
        dto.setStars(2);

        when(questRepository.findForQuestDetail(100L)).thenReturn(Optional.of(quest));
        when(questApplicationRepository.findForViewerApplicationWithStatus(100L, 3L, QuestApplicationStatus.APPROVED))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userReviewService.createOrUpdateReview(100L, dto, randomUser)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    private AppUser createUser(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private Quest createCompletedQuest(Long questId, AppUser employer) {
        Quest quest = new Quest();
        quest.setId(questId);
        quest.setCreator(employer);
        quest.setTitle("Quest");
        quest.setStatus(QuestStatus.COMPLETED);
        return quest;
    }

    private QuestApplication createApprovedApplication(Quest quest, AppUser worker) {
        QuestApplication application = new QuestApplication();
        application.setQuest(quest);
        application.setApplicant(worker);
        application.setStatus(QuestApplicationStatus.APPROVED);
        return application;
    }
}
