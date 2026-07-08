package com.themuffinman.app.workmarket.mapper;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.common.dto.NavigationTargetType;
import com.themuffinman.app.workmarket.dto.UserReviewResponseDTO;
import com.themuffinman.app.workmarket.model.UserReview;
import org.springframework.stereotype.Component;

@Component
public class WorkmarketUserReviewMgr {

    public UserReviewResponseDTO toDto(UserReview review) {
        if (review == null) {
            return null;
        }

        return UserReviewResponseDTO.builder()
                .id(review.getId())
                .questId(review.getQuest().getId())
                .questTitle(review.getQuest().getTitle())
                .questNavigation(NavigationTargetDTO.builder()
                        .type(NavigationTargetType.QUEST_DETAIL)
                        .entityId(review.getQuest().getId())
                        .build())
                .reviewerUserId(review.getReviewer().getId())
                .reviewerUsername(review.getReviewer().getUsername())
                .reviewedUserId(review.getReviewedUser().getId())
                .reviewedRole(review.getReviewedRole())
                .stars(review.getStars().intValue())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
