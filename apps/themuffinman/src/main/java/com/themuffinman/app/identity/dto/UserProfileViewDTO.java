package com.themuffinman.app.identity.dto;

import com.themuffinman.app.social.dto.CircleRelationDTO;
import com.themuffinman.app.vision.dto.UserRatingSummaryDTO;
import com.themuffinman.app.vision.dto.UserReviewResponseDTO;
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
    private String resolutionKey;
    private String resolutionLabel;
    private boolean exactResolutionEligible;
    private CircleRelationDTO relation;
    private ProfilePrimaryActionDTO primaryAction;
    private boolean showBlockAction;
    private boolean blockActionEnabled;
    private String blockActionLabel;
    private UserRatingSummaryDTO employerRating;
    private UserRatingSummaryDTO workerRating;
    private List<UserReviewResponseDTO> recentReviews;
}
