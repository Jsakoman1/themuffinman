package com.themuffinman.app.location.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.workmarket.model.Quest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationGeoService {
    private static final int DEFAULT_RADIUS_KM = 10;
    private static final int MIN_RADIUS_KM = 1;
    private static final int MAX_RADIUS_KM = 30;

    public Integer normalizeRadius(Integer radiusKm) {
        if (radiusKm == null) {
            return DEFAULT_RADIUS_KM;
        }
        if (radiusKm < MIN_RADIUS_KM || radiusKm > MAX_RADIUS_KM) {
            throw ServiceErrors.badRequest("Search radius must be between 1 and 30 km");
        }
        return radiusKm;
    }

    public boolean hasCoordinates(BigDecimal latitude, BigDecimal longitude) {
        return latitude != null && longitude != null;
    }

    public boolean isQuestSearchable(Quest quest) {
        return hasCoordinates(quest.getLocationLatitude(), quest.getLocationLongitude());
    }

    public boolean isUserDiscoverableNearby(AppUser user) {
        return user != null
                && user.getLocationMode() != null
                && user.getLocationMode() != UserLocationMode.OFF
                && hasCoordinates(user.getLocationLatitude(), user.getLocationLongitude());
    }

    public String resolveUserApproximateLocationLabel(AppUser user) {
        if (user == null) {
            return null;
        }

        String locality = normalizeText(user.getLocationLocality());
        String country = normalizeText(user.getLocationCountry());
        if (locality != null && country != null) {
            return locality + ", " + country;
        }
        if (locality != null) {
            return locality;
        }
        if (country != null) {
            return country;
        }
        return normalizeText(user.getLocationLabel());
    }

    public double distanceKm(BigDecimal fromLat, BigDecimal fromLng, BigDecimal toLat, BigDecimal toLng) {
        double earthRadiusKm = 6371.0088d;
        double latDistance = Math.toRadians(toLat.doubleValue() - fromLat.doubleValue());
        double lngDistance = Math.toRadians(toLng.doubleValue() - fromLng.doubleValue());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(fromLat.doubleValue())) * Math.cos(Math.toRadians(toLat.doubleValue()))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
