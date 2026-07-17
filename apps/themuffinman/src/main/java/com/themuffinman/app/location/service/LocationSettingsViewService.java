package com.themuffinman.app.location.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.dto.UserLocationSettingsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.themuffinman.app.common.concepts.ModuleOwnership.isOwner;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationSettingsViewService {
    private final LocationGeoService locationGeoService;
    private final LocationAccessPolicyService locationAccessPolicyService;

    public UserLocationSettingsDTO toDto(AppUser user) {
        return UserLocationSettingsDTO.builder()
                .mode(user.getLocationMode())
                .defaultRadiusKm(locationGeoService.normalizeRadius(user.getLocationRadiusKm()))
                .hasCoordinates(locationGeoService.hasCoordinates(user.getLocationLatitude(), user.getLocationLongitude()))
                .sharingSummary(locationAccessPolicyService.describeUserLocationSharingSummary(user))
                .visibilitySummary(locationAccessPolicyService.describeUserLocationVisibilitySummary(user))
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

    public UserLocationSettingsDTO toViewerDto(AppUser owner, AppUser viewer) {
        if (owner == null || isOwner(owner.getId(), viewer)) {
            return toDto(owner);
        }

        boolean exactAllowed = locationAccessPolicyService.canViewExactLocation(owner, viewer);
        if (owner.getLocationMode() == null || owner.getLocationMode() == com.themuffinman.app.location.model.UserLocationMode.OFF) {
            return UserLocationSettingsDTO.builder()
                    .mode(com.themuffinman.app.location.model.UserLocationMode.OFF)
                    .hasCoordinates(false)
                    .sharingSummary("Hidden")
                    .visibilitySummary("Hidden")
                    .exactVisibilityScope(com.themuffinman.app.location.model.ExactLocationVisibilityScope.NOBODY)
                    .build();
        }

        if (!exactAllowed || owner.getLocationMode() != com.themuffinman.app.location.model.UserLocationMode.EXACT) {
            return UserLocationSettingsDTO.builder()
                    .mode(com.themuffinman.app.location.model.UserLocationMode.APPROXIMATE)
                    .defaultRadiusKm(locationGeoService.normalizeRadius(owner.getLocationRadiusKm()))
                    .hasCoordinates(locationGeoService.hasCoordinates(owner.getLocationLatitude(), owner.getLocationLongitude()))
                    .sharingSummary("Approximate area only")
                    .visibilitySummary("Approximate area only")
                    .exactVisibilityScope(com.themuffinman.app.location.model.ExactLocationVisibilityScope.NOBODY)
                    .label(owner.getLocationLabel())
                    .countryCode(owner.getLocationCountryCode())
                    .country(owner.getLocationCountry())
                    .locality(owner.getLocationLocality())
                    .build();
        }

        return toDto(owner);
    }
}
