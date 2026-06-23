package com.sidequest.sidequest.mapper;

import com.sidequest.sidequest.dto.UserReviewResponseDTO;
import com.sidequest.sidequest.model.UserReview;
import org.springframework.stereotype.Component;

@Component
public class UserReviewMgr {

    public UserReviewResponseDTO toDto(UserReview review) {
        if (review == null) {
            return null;
        }

        return UserReviewResponseDTO.builder()
                .id(review.getId())
                .questId(review.getQuest().getId())
                .questTitle(review.getQuest().getTitle())
                .reviewerUserId(review.getReviewer().getId())
                .reviewerUsername(review.getReviewer().getUsername())
                .reviewedUserId(review.getReviewedUser().getId())
                .reviewedRole(review.getReviewedRole())
                .stars(review.getStars())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
