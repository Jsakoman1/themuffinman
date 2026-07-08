package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.UserRatingSummaryDTO;
import com.themuffinman.app.workmarket.dto.UserReviewResponseDTO;
import com.themuffinman.app.workmarket.dto.UserReviewRequestDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketUserReviewMgr;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.model.ReviewRole;
import com.themuffinman.app.workmarket.model.UserReview;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketUserReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service("workmarketUserReviewService")
@RequiredArgsConstructor
public class WorkmarketUserReviewService {

    private static final int MAX_COMMENT_LENGTH = 500;
    private static final int PROFILE_REVIEW_LIMIT = 10;

    private final WorkmarketQuestRepository questRepository;
    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketUserReviewRepository userReviewRepository;
    private final WorkmarketUserReviewMgr userReviewMgr;

    @Transactional(readOnly = true)
    public UserRatingSummaryDTO getRatingSummary(Long reviewedUserId, ReviewRole reviewedRole) {
        WorkmarketUserReviewRepository.UserRatingSummaryProjection summary = userReviewRepository
                .summarizeByReviewedUserIdAndReviewedRole(reviewedUserId, reviewedRole);

        if (summary == null || summary.getReviewCount() == 0) {
            return UserRatingSummaryDTO.builder()
                    .averageStars(0.0)
                    .reviewCount(0)
                    .build();
        }

        double roundedAverage = Math.round(summary.getAverageStars() * 10.0) / 10.0;
        return UserRatingSummaryDTO.builder()
                .averageStars(roundedAverage)
                .reviewCount(summary.getReviewCount())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserReviewResponseDTO> getRecentReviewsForProfile(Long reviewedUserId) {
        return userReviewRepository.findRecentByReviewedUserId(reviewedUserId, PageRequest.of(0, PROFILE_REVIEW_LIMIT))
                .stream()
                .map(userReviewMgr::toDto)
                .toList();
    }

    @Transactional
    public UserReview createOrUpdateReview(Long questId, UserReviewRequestDTO dto, AppUser currentUser) {
        validateAuthenticatedUser(currentUser);
        validateReviewInput(dto);

        Quest quest = requireQuest(questId);
        requireCompletedQuest(quest);

        ReviewTarget reviewTarget = resolveReviewTarget(quest, dto.getReviewedUserId(), currentUser);
        if (currentUser.getId().equals(reviewTarget.reviewedUser().getId())) {
            throw ServiceErrors.badRequest("You cannot review yourself");
        }

        UserReview review = userReviewRepository
                .findByQuestIdAndReviewerIdAndReviewedUserId(questId, currentUser.getId(), reviewTarget.reviewedUser().getId())
                .orElseGet(UserReview::new);

        if (review.getId() == null) {
            review.setQuest(quest);
            review.setReviewer(currentUser);
            review.setReviewedUser(reviewTarget.reviewedUser());
            review.setCreatedAt(Instant.now());
        }

        review.setReviewedRole(reviewTarget.reviewedRole());
        review.setStars(dto.getStars().shortValue());
        review.setComment(normalizeComment(dto.getComment()));
        review.setUpdatedAt(Instant.now());

        return userReviewRepository.save(review);
    }

    private Quest requireQuest(Long questId) {
        return questRepository.findForQuestDetail(questId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest not found with id " + questId));
    }

    private void validateAuthenticatedUser(AppUser currentUser) {
        if (currentUser == null) {
            throw ServiceErrors.unauthorized("Authentication is required");
        }
    }

    private void requireCompletedQuest(Quest quest) {
        if (quest.getStatus() != QuestStatus.COMPLETED) {
            throw ServiceErrors.badRequest("Reviews are only allowed after a quest is completed");
        }
    }

    private void validateReviewInput(UserReviewRequestDTO dto) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Review request is required");
        }

        if (dto.getReviewedUserId() == null) {
            throw ServiceErrors.badRequest("Reviewed user id is required");
        }

        if (dto.getStars() == null || dto.getStars() < 1 || dto.getStars() > 5) {
            throw ServiceErrors.badRequest("Stars must be between 1 and 5");
        }

        String normalizedComment = normalizeComment(dto.getComment());
        if (normalizedComment != null && normalizedComment.length() > MAX_COMMENT_LENGTH) {
            throw ServiceErrors.badRequest("Comment must be at most 500 characters");
        }
    }

    private String normalizeComment(String comment) {
        String normalizedComment = RichTextInputValidator.extractPlainText(comment);
        if (normalizedComment.isBlank()) {
            return null;
        }

        return normalizedComment;
    }

    private ReviewTarget resolveReviewTarget(Quest quest, Long reviewedUserId, AppUser currentUser) {
        if (quest.getCreator().getId().equals(reviewedUserId)) {
            questApplicationRepository.findForViewerApplicationWithStatus(
                    quest.getId(),
                    currentUser.getId(),
                    QuestApplicationStatus.APPROVED
            ).orElseThrow(() -> ServiceErrors.forbidden("Only the approved worker can review the employer"));

            return new ReviewTarget(quest.getCreator(), ReviewRole.EMPLOYER);
        }

        if (!quest.getCreator().getId().equals(currentUser.getId())) {
            throw ServiceErrors.forbidden("You are not allowed to review this user for the selected quest");
        }

        QuestApplication approvedApplication = questApplicationRepository.findForViewerApplicationWithStatus(
                quest.getId(),
                reviewedUserId,
                QuestApplicationStatus.APPROVED
        ).orElseThrow(() -> ServiceErrors.badRequest("This user was not an approved worker for the selected quest"));

        return new ReviewTarget(approvedApplication.getApplicant(), ReviewRole.WORKER);
    }

    private record ReviewTarget(AppUser reviewedUser, ReviewRole reviewedRole) {
    }
}
