package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.social.dto.CircleRelationDTO;
import com.themuffinman.app.social.dto.CircleRelationStatus;
import com.themuffinman.app.identity.dto.ProfilePrimaryActionDTO;
import com.themuffinman.app.identity.dto.UserProfileViewDTO;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.service.CircleService;
import com.themuffinman.app.social.service.SocialRelationActionHelper;
import com.themuffinman.app.social.service.SocialPresentationHelper;
import com.themuffinman.app.workmarket.model.ReviewRole;
import com.themuffinman.app.workmarket.service.UserReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileViewService {
    private final AppUserService appUserService;
    private final CircleService circleService;
    private final AppUserMgr appUserMgr;
    private final UserReviewService userReviewService;
    private final SocialPresentationHelper socialPresentationHelper;
    private final SocialRelationActionHelper socialRelationActionHelper;

    public UserProfileViewDTO getProfileView(Long profileUserId, AppUser currentUser) {
        AppUser profileUser = appUserService.getAppUser(profileUserId);
        AppUserResponseDTO profile = appUserMgr.withProfileStats(
                appUserMgr.toDto(profileUser),
                appUserService.countQuestsByCreatorId(profileUser.getId()),
                appUserService.getOpenQuestsByCreatorId(profileUser.getId())
        );

        boolean ownProfile = currentUser != null && currentUser.getId().equals(profileUser.getId());
        CircleRelationDTO relation = ownProfile
                ? CircleRelationDTO.builder()
                        .relationStatus(CircleRelationStatus.NONE)
                        .relationLabel(socialPresentationHelper.relationLabel(CircleRelationStatus.NONE))
                        .relationBadgeClass(socialPresentationHelper.relationBadgeClass(CircleRelationStatus.NONE))
                        .blockedByCurrentUser(false)
                        .exactResolutionEligible(true)
                        .build()
                : circleService.getRelationWithUser(currentUser, profileUser.getId());

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
                .showBlockAction(!ownProfile && relation.getRelationStatus() != CircleRelationStatus.BLOCKED)
                .blockActionEnabled(!ownProfile && relation.getRelationStatus() != CircleRelationStatus.BLOCKED)
                .blockActionLabel(socialRelationActionHelper.blockActionLabel())
                .employerRating(userReviewService.getRatingSummary(profileUser.getId(), ReviewRole.EMPLOYER))
                .workerRating(userReviewService.getRatingSummary(profileUser.getId(), ReviewRole.WORKER))
                .recentReviews(userReviewService.getRecentReviewsForProfile(profileUser.getId()))
                .build();
    }
}
