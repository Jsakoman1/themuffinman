package com.themuffinman.app.vision.dto;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.vision.model.ReviewRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReviewResponseDTO {
    private Long id;
    private Long questId;
    private String questTitle;
    private NavigationTargetDTO questNavigation;
    private Long reviewerUserId;
    private String reviewerUsername;
    private Long reviewedUserId;
    private ReviewRole reviewedRole;
    private int stars;
    @Nullable
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;
}
