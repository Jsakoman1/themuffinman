package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.social.dto.CircleRelationDTO;
import com.themuffinman.app.social.dto.CircleRelationStatusDTO;
import com.themuffinman.app.identity.dto.ProfilePrimaryActionDTO;
import com.themuffinman.app.identity.dto.UserProfileViewDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.service.CircleRelationshipReadService;
import com.themuffinman.app.social.service.SocialRelationActionHelper;
import com.themuffinman.app.social.service.SocialPresentationHelper;
import com.themuffinman.app.workmarket.model.ReviewRole;
import com.themuffinman.app.workmarket.service.WorkmarketUserReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileViewService {
    private final AppUserReadService appUserReadService;
    private final CircleRelationshipReadService circleRelationshipReadService;
    private final IdentityUserSummaryAssembler identityUserSummaryAssembler;
    private final WorkmarketUserReviewService userReviewService;
    private final SocialPresentationHelper socialPresentationHelper;
    private final SocialRelationActionHelper socialRelationActionHelper;

    public UserProfileViewDTO getProfileView(Long profileUserId, AppUser currentUser) {
        AppUser profileUser = appUserReadService.getAppUser(profileUserId);
        boolean ownProfile = currentUser != null && currentUser.getId().equals(profileUser.getId());
        long openQuestCount = appUserReadService.countQuestsByCreatorId(profileUser.getId());
        var openQuests = appUserReadService.getOpenQuestsByCreatorId(profileUser.getId());
        AppUserResponseDTO profile = ownProfile
                ? identityUserSummaryAssembler.buildProfileSummary(profileUser, openQuestCount, openQuests)
                : identityUserSummaryAssembler.buildViewerProfileSummary(profileUser, currentUser, openQuestCount, openQuests);

        CircleRelationDTO relation = ownProfile
                ? CircleRelationDTO.builder()
                        .relationStatus(CircleRelationStatusDTO.NONE)
                        .relationLabel(socialPresentationHelper.relationLabel(CircleRelationStatusDTO.NONE))
                        .relationBadgeClass(socialPresentationHelper.relationBadgeClass(CircleRelationStatusDTO.NONE))
                        .blockedByCurrentUser(false)
                        .exactResolutionEligible(true)
                        .build()
                : circleRelationshipReadService.getRelationWithUser(currentUser, profileUser.getId());

        return UserProfileViewDTO.builder()
                .profile(profile)
                .ownProfile(ownProfile)
                .resolutionKey("user-profile:" + profileUser.getId())
                .resolutionLabel(profileUser.getUsername() + " <" + profileUser.getEmail() + ">")
                .exactResolutionEligible(true)
                .relation(relation)
                .primaryAction(socialRelationActionHelper.profilePrimaryAction(
                        ownProfile,
                        relation.getRelationStatus(),
                        relation.isBlockedByCurrentUser()
                ))
                .showBlockAction(!ownProfile && relation.getRelationStatus() != CircleRelationStatusDTO.BLOCKED)
                .blockActionEnabled(!ownProfile && relation.getRelationStatus() != CircleRelationStatusDTO.BLOCKED)
                .blockActionLabel(socialRelationActionHelper.blockActionLabel())
                .employerRating(userReviewService.getRatingSummary(profileUser.getId(), ReviewRole.EMPLOYER))
                .workerRating(userReviewService.getRatingSummary(profileUser.getId(), ReviewRole.WORKER))
                .recentReviews(userReviewService.getRecentReviewsForProfile(profileUser.getId()))
                .build();
    }
}
