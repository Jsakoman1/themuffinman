package com.themuffinman.app.identity.mapper;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.service.LocationSettingsService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppUserMgrTest {

    @Test
    void toDtoAddsDeterministicResolutionMetadata() {
        LocationSettingsService locationSettingsService = mock(LocationSettingsService.class);
        AppUserMgr appUserMgr = new AppUserMgr(locationSettingsService);

        AppUser user = new AppUser();
        user.setId(4L);
        user.setUsername("tom");
        user.setEmail("tom@example.com");

        when(locationSettingsService.toDto(user)).thenReturn(null);

        var dto = appUserMgr.toDto(user);

        assertEquals("user:4", dto.getResolutionKey());
        assertEquals("tom <tom@example.com>", dto.getResolutionLabel());
        assertTrue(dto.isExactResolutionEligible());
    }
}
