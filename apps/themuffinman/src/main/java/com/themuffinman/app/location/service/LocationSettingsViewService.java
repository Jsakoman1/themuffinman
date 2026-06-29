package com.themuffinman.app.location.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.dto.UserLocationSettingsDTO;
import com.themuffinman.app.location.model.ExactLocationVisibilityScope;
import com.themuffinman.app.location.model.UserLocationMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationSettingsViewService {
    private final LocationGeoService locationGeoService;

    public UserLocationSettingsDTO toDto(AppUser user) {
        return UserLocationSettingsDTO.builder()
                .mode(user.getLocationMode())
                .defaultRadiusKm(locationGeoService.normalizeRadius(user.getLocationRadiusKm()))
                .hasCoordinates(locationGeoService.hasCoordinates(user.getLocationLatitude(), user.getLocationLongitude()))
                .sharingSummary(resolveUserLocationSharingSummary(user))
                .visibilitySummary(resolveUserLocationVisibilitySummary(user))
                .exactVisibilityScope(user.getExactLocationVisibilityScope())
                .exactVisibleCircleIds(user.getExactLocationVisibleToCircles().stream().map(circle -> circle.getId()).toList())
                .exactVisibleUserIds(user.getExactLocationVisibleToUsers().stream().map(AppUser::getId).toList())
                .provider(user.getLocationProvider())
                .providerPlaceId(user.getLocationProviderPlaceId())
                .label(user.getLocationLabel())
                .countryCode(user.getLocationCountryCode())
                .country(user.getLocationCountry())
                .locality(user.getLocationLocality())
                .postalCode(user.getLocationPostalCode())
                .street(user.getLocationStreet())
                .houseNumber(user.getLocationHouseNumber())
                .latitude(user.getLocationLatitude())
                .longitude(user.getLocationLongitude())
                .resolvedAt(user.getLocationResolvedAt())
                .updatedAt(user.getLocationUpdatedAt())
                .build();
    }

    public String resolveUserLocationSharingSummary(AppUser user) {
        if (user == null || user.getLocationMode() == null || user.getLocationMode() == UserLocationMode.OFF) {
            return "Hidden";
        }

        if (user.getLocationMode() == UserLocationMode.APPROXIMATE) {
            return "Approximate area only";
        }

        return "Exact location enabled";
    }

    public String resolveUserLocationVisibilitySummary(AppUser user) {
        if (user == null || user.getLocationMode() == null || user.getLocationMode() == UserLocationMode.OFF) {
            return "Hidden";
        }

        if (user.getLocationMode() == UserLocationMode.APPROXIMATE) {
            return "Approximate area only";
        }

        ExactLocationVisibilityScope scope = user.getExactLocationVisibilityScope() == null
                ? ExactLocationVisibilityScope.NOBODY
                : user.getExactLocationVisibilityScope();

        return switch (scope) {
            case NOBODY -> "Private";
            case EVERYONE -> "Visible to everyone";
            case CIRCLES -> "Visible to circles";
            case USERS -> "Visible to selected people";
        };
    }

}
