package com.themuffinman.app.location.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.model.ExactLocationVisibilityScope;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.testing.TestFixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocationViewerRedactionTest {
    @Test
    void authorizedViewerReceivesExactAddressWithoutOwnerPolicyMetadata() {
        LocationGeoService geoService = mock(LocationGeoService.class);
        LocationAccessPolicyService policyService = mock(LocationAccessPolicyService.class);
        LocationSettingsViewService viewService = new LocationSettingsViewService(geoService, policyService);
        AppUser owner = TestFixtures.user(1L, "owner");
        AppUser viewer = TestFixtures.user(2L, "viewer");
        owner.setLocationMode(UserLocationMode.EXACT);
        owner.setLocationRadiusKm(10);
        owner.setExactLocationVisibilityScope(ExactLocationVisibilityScope.CIRCLES);
        owner.setLocationLatitude(new BigDecimal("47.3769"));
        owner.setLocationLongitude(new BigDecimal("8.5417"));
        owner.setLocationProvider("geoapify");
        owner.setLocationProviderPlaceId("private-provider-id");
        owner.setLocationLabel("Private address");
        when(policyService.canViewExactLocation(owner, viewer)).thenReturn(true);
        when(geoService.normalizeRadius(10)).thenReturn(10);
        when(geoService.hasCoordinates(owner.getLocationLatitude(), owner.getLocationLongitude())).thenReturn(true);

        var dto = viewService.toViewerDto(owner, viewer);

        assertThat(dto.getLabel()).isEqualTo("Private address");
        assertThat(dto.getExactVisibilityScope()).isEqualTo(ExactLocationVisibilityScope.NOBODY);
        assertThat(dto.getExactVisibleCircleIds()).isEmpty();
        assertThat(dto.getProvider()).isNull();
        assertThat(dto.getProviderPlaceId()).isNull();
        assertThat(dto.getLatitude()).isNull();
        assertThat(dto.getLongitude()).isNull();
    }

    @Test
    void nullOwnerReturnsNoLocationView() {
        LocationSettingsViewService viewService = new LocationSettingsViewService(mock(LocationGeoService.class), mock(LocationAccessPolicyService.class));

        assertThat(viewService.toViewerDto(null, TestFixtures.user(2L, "viewer"))).isNull();
    }
}
