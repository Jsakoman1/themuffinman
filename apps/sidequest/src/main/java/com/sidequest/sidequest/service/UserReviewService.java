package com.sidequest.sidequest.service;

import com.sidequest.sidequest.dto.UserRatingSummaryDTO;
import com.sidequest.sidequest.dto.UserReviewRequestDTO;
import com.sidequest.sidequest.dto.UserReviewResponseDTO;
import com.sidequest.sidequest.mapper.UserReviewMgr;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.model.Quest;
import com.sidequest.sidequest.model.QuestApplication;
import com.sidequest.sidequest.model.QuestApplicationStatus;
import com.sidequest.sidequest.model.QuestStatus;
import com.sidequest.sidequest.model.ReviewRole;
import com.sidequest.sidequest.model.UserReview;
import com.sidequest.sidequest.repository.QuestApplicationRepository;
import com.sidequest.sidequest.repository.QuestRepository;
import com.sidequest.sidequest.repository.UserReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserReviewService {
    private static final int MAX_COMMENT_LENGTH = 500;
    private static final int PROFILE_REVIEW_LIMIT = 10;

    private final QuestRepository questRepository;
    private final QuestApplicationRepository questApplicationRepository;
    private final UserReviewRepository userReviewRepository;
    private final UserReviewMgr userReviewMgr;

    @Transactional
    public UserReviewResponseDTO createOrUpdateReview(Long questId, UserReviewRequestDTO dto, AppUser currentUser) {
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
        review.setStars(dto.getStars());
        review.setComment(normalizeComment(dto.getComment()));
        review.setUpdatedAt(Instant.now());

        return userReviewMgr.toDto(userReviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public UserRatingSummaryDTO getRatingSummary(Long reviewedUserId, ReviewRole reviewedRole) {
        UserReviewRepository.UserRatingSummaryProjection summary = userReviewRepository
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

    private Quest requireQuest(Long questId) {
        return questRepository.findByIdWithCreator(questId)
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
            questApplicationRepository.findByQuestIdAndApplicantIdAndStatus(
                    quest.getId(),
                    currentUser.getId(),
                    QuestApplicationStatus.APPROVED
            ).orElseThrow(() -> ServiceErrors.forbidden("Only the approved worker can review the employer"));

            return new ReviewTarget(quest.getCreator(), ReviewRole.EMPLOYER);
        }

        if (!quest.getCreator().getId().equals(currentUser.getId())) {
            throw ServiceErrors.forbidden("You are not allowed to review this user for the selected quest");
        }

        QuestApplication approvedApplication = questApplicationRepository.findByQuestIdAndApplicantIdAndStatus(
                quest.getId(),
                reviewedUserId,
                QuestApplicationStatus.APPROVED
        ).orElseThrow(() -> ServiceErrors.badRequest("This user was not an approved worker for the selected quest"));

        return new ReviewTarget(approvedApplication.getApplicant(), ReviewRole.WORKER);
    }

    private record ReviewTarget(AppUser reviewedUser, ReviewRole reviewedRole) {
    }
}
