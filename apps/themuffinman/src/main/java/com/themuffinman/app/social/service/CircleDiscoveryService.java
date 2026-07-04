package com.themuffinman.app.social.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.location.service.LocationGeoService;
import com.themuffinman.app.social.dto.CircleRelationStatusDTO;
import com.themuffinman.app.social.dto.CircleRelationDTO;
import com.themuffinman.app.social.dto.CircleSearchResultDTO;
import com.themuffinman.app.social.dto.CircleSearchResultListResponseDTO;
import com.themuffinman.app.social.model.CircleRequest;
import com.themuffinman.app.social.repository.CircleRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CircleDiscoveryService {
    private final AppUserRepository appUserRepository;
    private final CircleRequestRepository circleRequestRepository;
    private final CircleViewAssembler circleViewAssembler;
    private final CircleSearchQueryService circleSearchQueryService;
    private final LocationGeoService locationGeoService;

    public CircleSearchResultListResponseDTO getNearbyUsers(AppUser currentUser, Integer radiusKm, int page, int size) {
        if (!locationGeoService.isUserDiscoverableNearby(currentUser)) {
            return circleViewAssembler.buildCircleSearchResultListResponse(List.of(), page, size);
        }

        int normalizedRadiusKm = locationGeoService.normalizeRadius(radiusKm == null ? 2 : radiusKm);
        List<CircleSearchResultDTO> results = appUserRepository.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(currentUser.getId()))
                .filter(locationGeoService::isUserDiscoverableNearby)
                .map(candidate -> mapNearbyUser(currentUser, candidate))
                .filter(Objects::nonNull)
                .filter(candidate -> candidate.getDistanceKm() != null && candidate.getDistanceKm() <= normalizedRadiusKm)
                .sorted(Comparator
                        .comparing(CircleSearchResultDTO::getDistanceKm, Comparator.nullsLast(Double::compareTo))
                        .thenComparing(CircleSearchResultDTO::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return circleViewAssembler.buildCircleSearchResultListResponse(results, page, size);
    }

    public List<CircleSearchResultDTO> getInviteCandidates(AppUser currentUser) {
        return getInviteCandidatesPage(currentUser, 0, 12).getItems();
    }

    public CircleSearchResultListResponseDTO getInviteCandidatesPage(AppUser currentUser, int page, int size) {
        List<CircleSearchResultDTO> results = appUserRepository.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(currentUser.getId()))
                .map(candidate -> circleViewAssembler.toSearchResult(currentUser, candidate, findRelation(currentUser, candidate)))
                .filter(candidate -> candidate.getRelationStatus() == CircleRelationStatusDTO.NONE)
                .sorted(Comparator.comparing(CircleSearchResultDTO::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return circleViewAssembler.buildCircleSearchResultListResponse(results, page, size);
    }

    public CircleSearchResultListResponseDTO searchCircleUsers(AppUser currentUser, String query, int page, int size) {
        String normalizedQuery = circleSearchQueryService.normalizeSearchQuery(query);
        if (normalizedQuery.length() < 2) {
            return circleViewAssembler.buildCircleSearchResultListResponse(List.of(), page, size);
        }

        List<CircleSearchResultDTO> results = appUserRepository.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(currentUser.getId()))
                .map(candidate -> circleViewAssembler.toSearchResult(currentUser, candidate, findRelation(currentUser, candidate)))
                .filter(candidate -> circleSearchQueryService.matchesCandidateQuery(candidate, normalizedQuery))
                .sorted(Comparator.comparing(CircleSearchResultDTO::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return circleViewAssembler.buildCircleSearchResultListResponse(results, page, size);
    }

    public List<CircleSearchResultDTO> searchCircleUsers(AppUser currentUser, String query) {
        return searchCircleUsers(currentUser, query, 0, Integer.MAX_VALUE).getItems();
    }

    public CircleSearchResultListResponseDTO getBlockedUsers(AppUser currentUser, String query, int page, int size) {
        String normalizedQuery = circleSearchQueryService.normalizeSearchQuery(query);

        List<CircleSearchResultDTO> results = circleRequestRepository.findBlockedByUserId(currentUser.getId()).stream()
                .map(relation -> relation.getRequester().getId().equals(currentUser.getId())
                        ? relation.getRecipient()
                        : relation.getRequester())
                .filter(candidate -> !candidate.getId().equals(currentUser.getId()))
                .map(candidate -> circleViewAssembler.toSearchResult(currentUser, candidate, findRelation(currentUser, candidate)))
                .filter(candidate -> candidate.getRelationStatus() == CircleRelationStatusDTO.BLOCKED && candidate.isBlockedByCurrentUser())
                .filter(candidate -> circleSearchQueryService.matchesCandidateQuery(candidate, normalizedQuery))
                .sorted(Comparator.comparing(CircleSearchResultDTO::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return circleViewAssembler.buildCircleSearchResultListResponse(results, page, size);
    }

    public CircleRelationDTO getRelationWithUser(AppUser currentUser, Long otherUserId) {
        if (currentUser == null || otherUserId == null) {
            return circleViewAssembler.toRelationDto(CircleRelationStatusDTO.NONE, false);
        }

        AppUser otherUser = appUserRepository.findById(otherUserId)
                .orElseThrow(() -> ServiceErrors.notFound("User not found with id " + otherUserId));
        Optional<CircleRequest> relation = findRelation(currentUser, otherUser);
        CircleRelationStatusDTO relationStatus = circleViewAssembler.resolveRelationStatus(relation, currentUser.getId());
        return circleViewAssembler.toRelationDto(relationStatus, circleViewAssembler.isBlockedByCurrentUser(relation, currentUser.getId()));
    }

    private CircleSearchResultDTO mapNearbyUser(AppUser currentUser, AppUser candidate) {
        Optional<CircleRequest> relation = circleRequestRepository.findBetweenUsers(currentUser.getId(), candidate.getId());
        CircleSearchResultDTO result = circleViewAssembler.toSearchResult(currentUser, candidate, relation);
        if (result.getRelationStatus() == CircleRelationStatusDTO.BLOCKED) {
            return null;
        }

        double distanceKm = locationGeoService.distanceKm(
                currentUser.getLocationLatitude(),
                currentUser.getLocationLongitude(),
                candidate.getLocationLatitude(),
                candidate.getLocationLongitude()
        );

        return circleViewAssembler.withLocationDetails(
                result,
                locationGeoService.resolveUserApproximateLocationLabel(candidate),
                distanceKm,
                formatDistanceLabel(distanceKm)
        );
    }

    private Optional<CircleRequest> findRelation(AppUser leftUser, AppUser rightUser) {
        return circleRequestRepository.findBetweenUsers(leftUser.getId(), rightUser.getId());
    }

    private String formatDistanceLabel(double distanceKm) {
        if (distanceKm < 1) {
            return Math.round(distanceKm * 1000) + " m away";
        }
        return String.format(java.util.Locale.US, "%.1f km away", distanceKm);
    }
}
