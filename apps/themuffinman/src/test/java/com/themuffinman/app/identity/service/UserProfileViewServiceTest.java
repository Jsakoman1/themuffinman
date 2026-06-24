package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.common.dto.NavigationTargetType;
import com.themuffinman.app.social.dto.CircleRelationDTO;
import com.themuffinman.app.social.dto.CircleRelationStatus;
import com.themuffinman.app.social.service.SocialRelationActionHelper;
import com.themuffinman.app.social.service.CircleService;
import com.themuffinman.app.social.service.SocialPresentationHelper;
import com.themuffinman.app.workmarket.dto.UserRatingSummaryDTO;
import com.themuffinman.app.workmarket.dto.UserReviewResponseDTO;
import com.themuffinman.app.identity.dto.UserProfileViewDTO;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.ReviewRole;
import com.themuffinman.app.workmarket.service.UserReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileViewServiceTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private CircleService circleService;

    @Mock
    private AppUserMgr appUserMgr;

    @Mock
    private UserReviewService userReviewService;

    @Spy
    private SocialPresentationHelper socialPresentationHelper = new SocialPresentationHelper();

    @Spy
    private SocialRelationActionHelper socialRelationActionHelper = new SocialRelationActionHelper();

    @InjectMocks
    private UserProfileViewService userProfileViewService;

    @Test
    void getProfileViewMarksOwnProfileAndEditAction() {
        AppUser currentUser = createUser(7L, "owner");
        AppUserResponseDTO profileDto = AppUserResponseDTO.builder().id(7L).username("owner").build();

        when(appUserService.getAppUser(7L)).thenReturn(currentUser);
        when(appUserMgr.toDto(currentUser)).thenReturn(profileDto);
        when(appUserService.countQuestsByCreatorId(7L)).thenReturn(2L);
        when(appUserService.getOpenQuestsByCreatorId(7L)).thenReturn(List.of());
        when(appUserMgr.withProfileStats(profileDto, 2L, List.of())).thenReturn(profileDto);
        mockReviewData(7L);

        UserProfileViewDTO result = userProfileViewService.getProfileView(7L, currentUser);

        assertTrue(result.isOwnProfile());
        assertEquals("EDIT_PROFILE", result.getPrimaryAction().getType());
        assertEquals("Edit profile", result.getPrimaryAction().getLabel());
        assertFalse(result.isShowBlockAction());
        verify(circleService, never()).getRelationWithUser(currentUser, 7L);
    }

    @Test
    void getProfileViewReturnsOpenCirclesActionForIncomingRequest() {
        AppUser currentUser = createUser(1L, "viewer");
        AppUser profileUser = createUser(2L, "target");
        AppUserResponseDTO profileDto = AppUserResponseDTO.builder().id(2L).username("target").build();
        CircleRelationDTO relation = CircleRelationDTO.builder()
                .relationStatus(CircleRelationStatus.INCOMING_REQUEST)
                .blockedByCurrentUser(false)
                .build();

        when(appUserService.getAppUser(2L)).thenReturn(profileUser);
        when(appUserMgr.toDto(profileUser)).thenReturn(profileDto);
        when(appUserService.countQuestsByCreatorId(2L)).thenReturn(0L);
        when(appUserService.getOpenQuestsByCreatorId(2L)).thenReturn(List.of());
        when(appUserMgr.withProfileStats(profileDto, 0L, List.of())).thenReturn(profileDto);
        when(circleService.getRelationWithUser(currentUser, 2L)).thenReturn(relation);
        mockReviewData(2L);

        UserProfileViewDTO result = userProfileViewService.getProfileView(2L, currentUser);

        assertFalse(result.isOwnProfile());
        assertEquals("OPEN_CIRCLES", result.getPrimaryAction().getType());
        assertEquals("Open circles", result.getPrimaryAction().getLabel());
        assertTrue(result.getPrimaryAction().isEnabled());
        assertEquals(NavigationTargetType.CIRCLES, result.getPrimaryAction().getNavigation().getType());
        assertTrue(result.isShowBlockAction());
    }

    @Test
    void getProfileViewReturnsDisabledBlockedActionWhenOtherUserBlockedCurrentUser() {
        AppUser currentUser = createUser(1L, "viewer");
        AppUser profileUser = createUser(4L, "blocked");
        AppUserResponseDTO profileDto = AppUserResponseDTO.builder().id(4L).username("blocked").build();
        CircleRelationDTO relation = CircleRelationDTO.builder()
                .relationStatus(CircleRelationStatus.BLOCKED)
                .blockedByCurrentUser(false)
                .build();

        when(appUserService.getAppUser(4L)).thenReturn(profileUser);
        when(appUserMgr.toDto(profileUser)).thenReturn(profileDto);
        when(appUserService.countQuestsByCreatorId(4L)).thenReturn(0L);
        when(appUserService.getOpenQuestsByCreatorId(4L)).thenReturn(List.of());
        when(appUserMgr.withProfileStats(profileDto, 0L, List.of())).thenReturn(profileDto);
        when(circleService.getRelationWithUser(currentUser, 4L)).thenReturn(relation);
        mockReviewData(4L);

        UserProfileViewDTO result = userProfileViewService.getProfileView(4L, currentUser);

        assertEquals("NONE", result.getPrimaryAction().getType());
        assertEquals("Blocked", result.getPrimaryAction().getLabel());
        assertFalse(result.getPrimaryAction().isEnabled());
        assertFalse(result.isShowBlockAction());
    }

    private AppUser createUser(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private void mockReviewData(Long userId) {
        when(userReviewService.getRatingSummary(userId, ReviewRole.EMPLOYER))
                .thenReturn(UserRatingSummaryDTO.builder().averageStars(4.5).reviewCount(2).build());
        when(userReviewService.getRatingSummary(userId, ReviewRole.WORKER))
                .thenReturn(UserRatingSummaryDTO.builder().averageStars(5.0).reviewCount(1).build());
        when(userReviewService.getRecentReviewsForProfile(userId))
                .thenReturn(List.of(UserReviewResponseDTO.builder().id(1L).stars(5).build()));
    }
}
