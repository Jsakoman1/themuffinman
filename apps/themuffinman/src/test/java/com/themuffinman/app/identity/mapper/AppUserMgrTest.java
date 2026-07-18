package com.themuffinman.app.identity.mapper;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.ProfileFieldVisibility;
import com.themuffinman.app.location.service.LocationSettingsViewService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppUserMgrTest {

    @Test
    void toDtoAddsDeterministicResolutionMetadata() {
        LocationSettingsViewService locationSettingsViewService = mock(LocationSettingsViewService.class);
        AppUserMgr appUserMgr = new AppUserMgr(locationSettingsViewService);

        AppUser user = new AppUser();
        user.setId(4L);
        user.setUsername("tom");
        user.setEmail("tom@example.com");

        when(locationSettingsViewService.toDto(user)).thenReturn(null);

        var dto = appUserMgr.toDto(user);

        assertEquals("user:4", dto.getResolutionKey());
        assertEquals("tom <tom@example.com>", dto.getResolutionLabel());
        assertTrue(dto.isExactResolutionEligible());
    }

    @Test
    void toProfileDtoRedactsPrivateProfileFieldsForOtherViewers() {
        LocationSettingsViewService locationSettingsViewService = mock(LocationSettingsViewService.class);
        AppUserMgr appUserMgr = new AppUserMgr(locationSettingsViewService);
        AppUser owner = new AppUser();
        owner.setId(4L);
        owner.setUsername("tom");
        owner.setEmail("tom@example.com");
        owner.setProfileDescription("private bio");
        owner.setProfileAvatarDataUrl("data:image/png;base64,private");
        owner.setProfileDescriptionVisibility(ProfileFieldVisibility.PRIVATE);
        owner.setProfileAvatarVisibility(ProfileFieldVisibility.PRIVATE);
        AppUser viewer = new AppUser();
        viewer.setId(9L);

        when(locationSettingsViewService.toDto(owner)).thenReturn(null);
        when(locationSettingsViewService.toViewerDto(owner, viewer)).thenReturn(null);

        var dto = appUserMgr.toProfileDto(owner, viewer);

        org.junit.jupiter.api.Assertions.assertNull(dto.getProfileDescription());
        org.junit.jupiter.api.Assertions.assertNull(dto.getProfileAvatarDataUrl());
        org.junit.jupiter.api.Assertions.assertNull(dto.getEmail());
    }
}
