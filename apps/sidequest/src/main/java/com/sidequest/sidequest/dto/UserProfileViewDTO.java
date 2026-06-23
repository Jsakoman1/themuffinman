package com.sidequest.sidequest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileViewDTO {
    private AppUserResponseDTO profile;
    private boolean ownProfile;
    private CircleRelationDTO relation;
    private ProfilePrimaryActionDTO primaryAction;
    private boolean showBlockAction;
    private boolean blockActionEnabled;
    private UserRatingSummaryDTO employerRating;
    private UserRatingSummaryDTO workerRating;
    private List<UserReviewResponseDTO> recentReviews;
}
