package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Getter
@Builder
public class BusinessReviewResponseDTO {
    private Long id;
    private Long bookingId;
    private String reviewerUsername;
    private int stars;
    @Nullable
    private String comment;
    private String serviceTitle;
    private Instant createdAt;
    private Instant updatedAt;
}
