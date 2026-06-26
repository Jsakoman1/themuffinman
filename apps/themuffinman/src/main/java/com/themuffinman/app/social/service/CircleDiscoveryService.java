package com.themuffinman.app.social.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.location.service.LocationSettingsService;
import com.themuffinman.app.social.dto.CircleRelationStatus;
import com.themuffinman.app.social.dto.CircleSearchResultDTO;
import com.themuffinman.app.social.dto.CircleSearchResultListResponseDTO;
import com.themuffinman.app.social.model.CircleRequest;
import com.themuffinman.app.social.repository.CircleRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CircleDiscoveryService {
    private final AppUserRepository appUserRepository;
    private final CircleRequestRepository circleRequestRepository;
    private final CircleViewAssembler circleViewAssembler;
    private final LocationSettingsService locationSettingsService;

    public CircleSearchResultListResponseDTO getNearbyUsers(AppUser currentUser, Integer radiusKm, int page, int size) {
        if (!locationSettingsService.isUserDiscoverableNearby(currentUser)) {
            return circleViewAssembler.buildCircleSearchResultListResponse(List.of(), page, size);
        }

        int normalizedRadiusKm = locationSettingsService.normalizeRadius(radiusKm == null ? 2 : radiusKm);
        List<CircleSearchResultDTO> results = appUserRepository.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(currentUser.getId()))
                .filter(locationSettingsService::isUserDiscoverableNearby)
                .map(candidate -> mapNearbyUser(currentUser, candidate))
                .filter(Objects::nonNull)
                .filter(candidate -> candidate.getDistanceKm() != null && candidate.getDistanceKm() <= normalizedRadiusKm)
                .sorted(Comparator
                        .comparing(CircleSearchResultDTO::getDistanceKm, Comparator.nullsLast(Double::compareTo))
                        .thenComparing(CircleSearchResultDTO::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return circleViewAssembler.buildCircleSearchResultListResponse(results, page, size);
    }

    private CircleSearchResultDTO mapNearbyUser(AppUser currentUser, AppUser candidate) {
        Optional<CircleRequest> relation = circleRequestRepository.findBetweenUsers(currentUser.getId(), candidate.getId());
        CircleSearchResultDTO result = circleViewAssembler.toSearchResult(currentUser, candidate, relation);
        if (result.getRelationStatus() == CircleRelationStatus.BLOCKED) {
            return null;
        }

        double distanceKm = locationSettingsService.distanceKm(
                currentUser.getLocationLatitude(),
                currentUser.getLocationLongitude(),
                candidate.getLocationLatitude(),
                candidate.getLocationLongitude()
        );

        return circleViewAssembler.withLocationDetails(
                result,
                locationSettingsService.resolveUserApproximateLocationLabel(candidate),
                distanceKm,
                formatDistanceLabel(distanceKm)
        );
    }

    private String formatDistanceLabel(double distanceKm) {
        if (distanceKm < 1) {
            return Math.round(distanceKm * 1000) + " m away";
        }
        return String.format(java.util.Locale.US, "%.1f km away", distanceKm);
    }
}
