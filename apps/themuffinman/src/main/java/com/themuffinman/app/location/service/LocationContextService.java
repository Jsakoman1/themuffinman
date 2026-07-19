package com.themuffinman.app.location.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.dto.UserLocationContextDTO;
import com.themuffinman.app.location.model.LocationResolutionStatus;
import com.themuffinman.app.location.model.UserLocationMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationContextService {
    private final LocationGeoService locationGeoService;

    public UserLocationContextDTO buildUserContext(AppUser user) {
        if (user == null) {
            return UserLocationContextDTO.builder()
                    .mode(UserLocationMode.OFF)
                    .resolutionStatus(LocationResolutionStatus.OFF)
                    .hasCoordinates(false)
                    .build();
        }

        UserLocationMode mode = user.getLocationMode() == null ? UserLocationMode.OFF : user.getLocationMode();
        boolean hasCoordinates = locationGeoService.hasCoordinates(user.getLocationLatitude(), user.getLocationLongitude());
        return UserLocationContextDTO.builder()
                .mode(mode)
                .resolutionStatus(mode == UserLocationMode.OFF
                        ? LocationResolutionStatus.OFF
                        : hasCoordinates ? LocationResolutionStatus.RESOLVED : LocationResolutionStatus.NEEDS_RESOLUTION)
                .hasCoordinates(hasCoordinates)
                .countryCode(clean(user.getLocationCountryCode()))
                .country(clean(user.getLocationCountry()))
                .locality(clean(user.getLocationLocality()))
                .locationLabel(clean(user.getLocationLabel()))
                .approximateLocationLabel(locationGeoService.resolveUserApproximateLocationLabel(user))
                .build();
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
