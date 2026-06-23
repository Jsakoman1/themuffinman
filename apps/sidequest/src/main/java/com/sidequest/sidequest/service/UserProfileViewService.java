package com.sidequest.sidequest.service;

import com.sidequest.sidequest.dto.AppUserResponseDTO;
import com.sidequest.sidequest.dto.CircleRelationDTO;
import com.sidequest.sidequest.dto.CircleRelationStatus;
import com.sidequest.sidequest.dto.ProfilePrimaryActionDTO;
import com.sidequest.sidequest.dto.UserProfileViewDTO;
import com.sidequest.sidequest.mapper.AppUserMgr;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.model.ReviewRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileViewService {
    private final AppUserService appUserService;
    private final CircleService circleService;
    private final AppUserMgr appUserMgr;
    private final UserReviewService userReviewService;

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
                .blockedByCurrentUser(false)
                .build()
                : circleService.getRelationWithUser(currentUser, profileUser.getId());

        return UserProfileViewDTO.builder()
                .profile(profile)
                .ownProfile(ownProfile)
                .relation(relation)
                .primaryAction(buildPrimaryAction(ownProfile, relation))
                .showBlockAction(!ownProfile && relation.getRelationStatus() != CircleRelationStatus.BLOCKED)
                .blockActionEnabled(!ownProfile && relation.getRelationStatus() != CircleRelationStatus.BLOCKED)
                .employerRating(userReviewService.getRatingSummary(profileUser.getId(), ReviewRole.EMPLOYER))
                .workerRating(userReviewService.getRatingSummary(profileUser.getId(), ReviewRole.WORKER))
                .recentReviews(userReviewService.getRecentReviewsForProfile(profileUser.getId()))
                .build();
    }

    private ProfilePrimaryActionDTO buildPrimaryAction(boolean ownProfile, CircleRelationDTO relation) {
        if (ownProfile) {
            return action("EDIT_PROFILE", "Edit profile", true);
        }

        if (relation.getRelationStatus() == CircleRelationStatus.BLOCKED) {
            if (relation.isBlockedByCurrentUser()) {
                return action("UNBLOCK", "Unblock", true);
            }

            return action("NONE", "Blocked", false);
        }

        if (relation.getRelationStatus() == CircleRelationStatus.CIRCLE) {
            return action("NONE", "Connected", false);
        }

        if (relation.getRelationStatus() == CircleRelationStatus.OUTGOING_REQUEST) {
            return action("NONE", "Invite sent", false);
        }

        if (relation.getRelationStatus() == CircleRelationStatus.INCOMING_REQUEST) {
            return action("OPEN_CIRCLES", "Open circles", true);
        }

        return action("SEND_INVITE", "Send invite", true);
    }

    private ProfilePrimaryActionDTO action(String type, String label, boolean enabled) {
        return ProfilePrimaryActionDTO.builder()
                .type(type)
                .label(label)
                .enabled(enabled)
                .build();
    }
}
