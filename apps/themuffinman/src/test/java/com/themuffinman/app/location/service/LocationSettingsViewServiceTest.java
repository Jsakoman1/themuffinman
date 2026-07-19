package com.themuffinman.app.location.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.dto.UserLocationSettingsDTO;
import com.themuffinman.app.location.model.ExactLocationVisibilityScope;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.location.model.LocationResolutionStatus;
import com.themuffinman.app.testing.TestFixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocationSettingsViewServiceTest {

    @Test
    void toDtoUsesLocationGeoAndAccessPolicyServicesForDerivedFields() {
        LocationGeoService locationGeoService = mock(LocationGeoService.class);
        LocationAccessPolicyService locationAccessPolicyService = mock(LocationAccessPolicyService.class);
        LocationSettingsViewService viewService = new LocationSettingsViewService(locationGeoService, locationAccessPolicyService);

        AppUser user = TestFixtures.user(11L, "owner");
        user.setLocationMode(UserLocationMode.EXACT);
        user.setLocationRadiusKm(14);
        user.setLocationLatitude(new BigDecimal("46.948"));
        user.setLocationLongitude(new BigDecimal("7.4474"));
        user.setExactLocationVisibilityScope(ExactLocationVisibilityScope.CIRCLES);
        user.setLocationProvider("geoapify");
        user.setLocationProviderPlaceId("place-123");
        user.setLocationLabel("Bern");
        user.setLocationCountryCode("CH");
        user.setLocationCountry("Switzerland");
        user.setLocationLocality("Bern");
        user.setLocationPostalCode("3000");
        user.setLocationStreet("Kramgasse");
        user.setLocationHouseNumber("5");
        user.setLocationResolvedAt(Instant.parse("2026-07-04T10:00:00Z"));
        user.setLocationUpdatedAt(Instant.parse("2026-07-04T10:05:00Z"));

        when(locationGeoService.normalizeRadius(14)).thenReturn(14);
        when(locationGeoService.hasCoordinates(user.getLocationLatitude(), user.getLocationLongitude())).thenReturn(true);
        when(locationAccessPolicyService.describeUserLocationSharingSummary(user)).thenReturn("Exact location enabled");
        when(locationAccessPolicyService.describeUserLocationVisibilitySummary(user)).thenReturn("Visible to circles");

        UserLocationSettingsDTO dto = viewService.toDto(user);

        assertThat(dto.getMode()).isEqualTo(UserLocationMode.EXACT);
        assertThat(dto.getDefaultRadiusKm()).isEqualTo(14);
        assertThat(dto.isHasCoordinates()).isTrue();
        assertThat(dto.getResolutionStatus()).isEqualTo(LocationResolutionStatus.RESOLVED);
        assertThat(dto.getSharingSummary()).isEqualTo("Exact location enabled");
        assertThat(dto.getVisibilitySummary()).isEqualTo("Visible to circles");
        assertThat(dto.getExactVisibilityScope()).isEqualTo(ExactLocationVisibilityScope.CIRCLES);
        assertThat(dto.getProvider()).isEqualTo("geoapify");
        assertThat(dto.getLabel()).isEqualTo("Bern");
        assertThat(dto.getResolvedAt()).isEqualTo(Instant.parse("2026-07-04T10:00:00Z"));
        assertThat(dto.getUpdatedAt()).isEqualTo(Instant.parse("2026-07-04T10:05:00Z"));
    }
}
