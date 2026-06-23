package com.sidequest.sidequest.dto;

import com.sidequest.sidequest.model.ReviewRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReviewResponseDTO {
    private Long id;
    private Long questId;
    private String questTitle;
    private Long reviewerUserId;
    private String reviewerUsername;
    private Long reviewedUserId;
    private ReviewRole reviewedRole;
    private int stars;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;
}
