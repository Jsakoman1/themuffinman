package com.themuffinman.app.location.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.dto.UserLocationContextDTO;
import com.themuffinman.app.location.model.LocationResolutionStatus;
import com.themuffinman.app.location.model.UserLocationMode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocationContextServiceTest {

    @Test
    void buildsOneBackendOwnedResolvedContextForVisionAndWebConsumers() {
        LocationGeoService geoService = mock(LocationGeoService.class);
        LocationContextService service = new LocationContextService(geoService);
        AppUser user = new AppUser();
        user.setLocationMode(UserLocationMode.APPROXIMATE);
        user.setLocationLatitude(new BigDecimal("47.3769"));
        user.setLocationLongitude(new BigDecimal("8.5417"));
        user.setLocationCountryCode("ch");
        user.setLocationCountry("Switzerland");
        user.setLocationLocality("Zurich");
        user.setLocationLabel("Zurich address");

        when(geoService.hasCoordinates(user.getLocationLatitude(), user.getLocationLongitude())).thenReturn(true);
        when(geoService.resolveUserApproximateLocationLabel(user)).thenReturn("Zurich, Switzerland");

        UserLocationContextDTO context = service.buildUserContext(user);

        assertThat(context.getMode()).isEqualTo(UserLocationMode.APPROXIMATE);
        assertThat(context.getResolutionStatus()).isEqualTo(LocationResolutionStatus.RESOLVED);
        assertThat(context.isHasCoordinates()).isTrue();
        assertThat(context.getCountryCode()).isEqualTo("ch");
        assertThat(context.getApproximateLocationLabel()).isEqualTo("Zurich, Switzerland");
    }

    @Test
    void keepsEnabledWithoutCoordinatesAsNeedsResolution() {
        LocationGeoService geoService = mock(LocationGeoService.class);
        LocationContextService service = new LocationContextService(geoService);
        AppUser user = new AppUser();
        user.setLocationMode(UserLocationMode.EXACT);
        when(geoService.hasCoordinates(null, null)).thenReturn(false);

        UserLocationContextDTO context = service.buildUserContext(user);

        assertThat(context.getResolutionStatus()).isEqualTo(LocationResolutionStatus.NEEDS_RESOLUTION);
        assertThat(context.isHasCoordinates()).isFalse();
    }
}
