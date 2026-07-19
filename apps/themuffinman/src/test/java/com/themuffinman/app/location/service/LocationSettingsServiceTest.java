package com.themuffinman.app.location.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.location.dto.UserLocationSettingsRequestDTO;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.social.service.CircleMembershipService;
import com.themuffinman.app.social.service.CircleRelationshipReadService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class LocationSettingsServiceTest {
    private final LocationSettingsService service = new LocationSettingsService(
            mock(CircleMembershipService.class),
            mock(CircleRelationshipReadService.class),
            mock(AppUserRepository.class),
            mock(LocationLookupService.class),
            mock(LocationAccessPolicyService.class)
    );

    @Test
    void disablingLocationClearsStoredCoordinatesAndProviderContext() {
        AppUser user = new AppUser();
        user.setLocationLatitude(new BigDecimal("47.3769"));
        user.setLocationLongitude(new BigDecimal("8.5417"));
        user.setLocationLabel("Zurich");
        user.setLocationProvider("geoapify");

        service.applyUserLocationSettings(user, UserLocationSettingsRequestDTO.builder()
                .mode(UserLocationMode.OFF).defaultRadiusKm(10).build());

        assertNull(user.getLocationLatitude());
        assertNull(user.getLocationLongitude());
        assertNull(user.getLocationLabel());
        assertNull(user.getLocationProvider());
    }

    @Test
    void rejectsRadiusOutsideSupportedPreferenceRange() {
        assertThrows(ResponseStatusException.class, () -> service.applyUserLocationSettings(
                new AppUser(),
                UserLocationSettingsRequestDTO.builder().mode(UserLocationMode.OFF).defaultRadiusKm(31).build()
        ));
    }
}
