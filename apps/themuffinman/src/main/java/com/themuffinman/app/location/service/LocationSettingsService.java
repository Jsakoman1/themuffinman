package com.themuffinman.app.location.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.dto.LocationLookupCandidateDTO;
import com.themuffinman.app.location.dto.UserLocationSettingsDTO;
import com.themuffinman.app.location.dto.UserLocationSettingsRequestDTO;
import com.themuffinman.app.location.model.ExactLocationVisibilityScope;
import com.themuffinman.app.location.model.QuestLocationVisibility;
import com.themuffinman.app.location.model.QuestLocationSource;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.social.dto.CircleRelationStatus;
import com.themuffinman.app.social.service.CircleMembershipService;
import com.themuffinman.app.social.service.CircleService;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LocationSettingsService {
    private static final int DEFAULT_RADIUS_KM = 10;
    private static final int MIN_RADIUS_KM = 1;
    private static final int MAX_RADIUS_KM = 30;
    private final CircleMembershipService circleMembershipService;
    private final CircleService circleService;
    private final AppUserRepository appUserRepository;
    private final LocationLookupService locationLookupService;
    private final LocationAccessPolicyService locationAccessPolicyService;

    public UserLocationSettingsDTO toDto(AppUser user) {
        return UserLocationSettingsDTO.builder()
                .mode(user.getLocationMode())
                .defaultRadiusKm(normalizeRadius(user.getLocationRadiusKm()))
                .hasCoordinates(hasCoordinates(user.getLocationLatitude(), user.getLocationLongitude()))
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

    public void applyUserLocationSettings(AppUser user, UserLocationSettingsRequestDTO dto) {
        if (dto == null) {
            return;
        }

        UserLocationMode mode = dto.getMode() == null ? UserLocationMode.OFF : dto.getMode();
        ExactLocationVisibilityScope exactVisibilityScope = dto.getExactVisibilityScope() == null
                ? ExactLocationVisibilityScope.NOBODY
                : dto.getExactVisibilityScope();
        user.setLocationMode(mode);
        user.setExactLocationVisibilityScope(exactVisibilityScope);
        user.setLocationRadiusKm(normalizeRadius(dto.getDefaultRadiusKm()));
        user.setLocationProvider(normalizeText(dto.getProvider()));
        user.setLocationProviderPlaceId(normalizeText(dto.getProviderPlaceId()));
        user.setLocationLabel(normalizeText(dto.getLabel()));
        user.setLocationCountryCode(normalizeCountryCode(dto.getCountryCode()));
        user.setLocationCountry(normalizeText(dto.getCountry()));
        user.setLocationLocality(normalizeText(dto.getLocality()));
        user.setLocationPostalCode(normalizeText(dto.getPostalCode()));
        user.setLocationStreet(normalizeText(dto.getStreet()));
        user.setLocationHouseNumber(normalizeText(dto.getHouseNumber()));
        user.setLocationLatitude(normalizeCoordinate(dto.getLatitude(), "Latitude"));
        user.setLocationLongitude(normalizeCoordinate(dto.getLongitude(), "Longitude"));
        user.setLocationResolvedAt(dto.getResolvedAt());
        user.setLocationUpdatedAt(java.time.Instant.now());
        syncExactVisibilityTargets(user, dto);

        if (mode == UserLocationMode.OFF) {
            clearUserCoordinates(user);
            return;
        }

        autoResolveUserCoordinates(user);
        requireUsableLocation(user);
    }

    public void applyQuestLocation(Quest quest, QuestRequestDTO dto, AppUser sourceUser) {
        QuestLocationVisibility visibility = dto == null ? null : dto.getLocationVisibility();
        QuestLocationVisibility normalizedVisibility = visibility == null ? QuestLocationVisibility.INHERIT : visibility;
        quest.setLocationVisibility(normalizedVisibility);

        QuestLocationSource locationSource = dto == null || dto.getLocationSource() == null
                ? QuestLocationSource.PROFILE
                : dto.getLocationSource();
        quest.setLocationSource(locationSource);

        if (normalizedVisibility == QuestLocationVisibility.OFF) {
            clearQuestLocation(quest);
            return;
        }

        if (locationSource == QuestLocationSource.CUSTOM) {
            applyCustomQuestLocation(quest, dto);
            return;
        }

        if (sourceUser.getLocationMode() == UserLocationMode.OFF) {
            if (normalizedVisibility == QuestLocationVisibility.INHERIT) {
                clearQuestLocation(quest);
                return;
            }
            throw ServiceErrors.badRequest("Your profile location is off. Add a location in My Profile before enabling quest location.");
        }

        autoResolveUserCoordinates(sourceUser);
        requireUsableLocation(sourceUser);
        copyUserLocationToQuest(sourceUser, quest);
    }

    public void applyLookupCandidateToUser(AppUser user, LocationLookupCandidateDTO candidate) {
        if (candidate == null) {
            return;
        }

        user.setLocationProvider(normalizeText(candidate.getProvider()));
        user.setLocationProviderPlaceId(normalizeText(candidate.getProviderPlaceId()));
        user.setLocationLabel(normalizeText(candidate.getLabel()));
        user.setLocationCountryCode(normalizeCountryCode(candidate.getCountryCode()));
        user.setLocationCountry(normalizeText(candidate.getCountry()));
        user.setLocationLocality(normalizeText(candidate.getLocality()));
        user.setLocationPostalCode(normalizeText(candidate.getPostalCode()));
        user.setLocationStreet(normalizeText(candidate.getStreet()));
        user.setLocationHouseNumber(normalizeText(candidate.getHouseNumber()));
        user.setLocationLatitude(normalizeCoordinate(candidate.getLatitude(), "Latitude"));
        user.setLocationLongitude(normalizeCoordinate(candidate.getLongitude(), "Longitude"));
        user.setLocationResolvedAt(candidate.getResolvedAt() == null ? Instant.now() : candidate.getResolvedAt());
        user.setLocationUpdatedAt(Instant.now());
    }

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
        return quest.getLocationVisibility() != QuestLocationVisibility.OFF
                && hasCoordinates(quest.getLocationLatitude(), quest.getLocationLongitude());
    }

    public String resolveQuestLocationLabel(Quest quest, AppUser viewer) {
        QuestLocationVisibility visibility = resolveEffectiveQuestLocationVisibility(quest);
        return switch (visibility) {
            case OFF -> null;
            case APPROXIMATE -> buildApproximateQuestLocationLabel(quest);
            case EXACT -> locationAccessPolicyService.canViewExactLocation(quest.getCreator(), viewer)
                    ? buildExactQuestLocationLabel(quest)
                    : buildApproximateQuestLocationLabel(quest);
            case INHERIT -> null;
        };
    }

    public String resolveQuestLocationSourceSummary(Quest quest) {
        return quest.getLocationSource() == QuestLocationSource.CUSTOM
                ? "Custom quest location"
                : "Uses creator profile location";
    }

    public String resolveQuestLocationVisibilitySummary(Quest quest, AppUser viewer) {
        QuestLocationVisibility visibility = resolveEffectiveQuestLocationVisibility(quest);
        return switch (visibility) {
            case OFF -> "Location hidden";
            case APPROXIMATE -> "Approximate area shown";
            case EXACT -> locationAccessPolicyService.canViewExactLocation(quest.getCreator(), viewer)
                    ? "Exact address shown"
                    : "Approximate area shown";
            case INHERIT -> "Location follows profile setting";
        };
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

    private void requireUsableLocation(AppUser user) {
        if (!hasCoordinates(user.getLocationLatitude(), user.getLocationLongitude())) {
            throw ServiceErrors.badRequest("Select a valid location from search results or use your current location before saving.");
        }
    }

    private void applyCustomQuestLocation(Quest quest, QuestRequestDTO dto) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Quest location request is required");
        }

        quest.setLocationProvider(normalizeText(null));
        quest.setLocationProviderPlaceId(normalizeText(null));
        quest.setLocationLabel(normalizeText(dto.getLocationLabel()));
        quest.setLocationCountryCode(normalizeCountryCode(dto.getLocationCountryCode()));
        quest.setLocationCountry(normalizeText(dto.getLocationCountry()));
        quest.setLocationLocality(normalizeText(dto.getLocationLocality()));
        quest.setLocationPostalCode(normalizeText(dto.getLocationPostalCode()));
        quest.setLocationStreet(normalizeText(dto.getLocationStreet()));
        quest.setLocationHouseNumber(normalizeText(dto.getLocationHouseNumber()));
        quest.setLocationLatitude(null);
        quest.setLocationLongitude(null);
        quest.setLocationResolvedAt(null);

        autoResolveQuestCoordinates(quest);
        if (!hasCoordinates(quest.getLocationLatitude(), quest.getLocationLongitude())) {
            throw ServiceErrors.badRequest("Resolve the quest address before saving.");
        }
    }

    private void autoResolveUserCoordinates(AppUser user) {
        if (hasCoordinates(user.getLocationLatitude(), user.getLocationLongitude())) {
            return;
        }

        LocationLookupCandidateDTO candidate = resolveLocationCandidate(buildLocationQuery(
                user.getLocationStreet(),
                user.getLocationHouseNumber(),
                user.getLocationPostalCode(),
                user.getLocationLocality(),
                user.getLocationCountry(),
                user.getLocationLabel()
        ), "system:user-location");
        if (candidate == null) {
            return;
        }

        applyLookupCandidateToUser(user, candidate);
    }

    private void autoResolveQuestCoordinates(Quest quest) {
        if (hasCoordinates(quest.getLocationLatitude(), quest.getLocationLongitude())) {
            return;
        }

        LocationLookupCandidateDTO candidate = resolveLocationCandidate(buildLocationQuery(
                quest.getLocationStreet(),
                quest.getLocationHouseNumber(),
                quest.getLocationPostalCode(),
                quest.getLocationLocality(),
                quest.getLocationCountry(),
                quest.getLocationLabel()
        ), "system:quest-location");
        if (candidate == null) {
            return;
        }

        quest.setLocationProvider(normalizeText(candidate.getProvider()));
        quest.setLocationProviderPlaceId(normalizeText(candidate.getProviderPlaceId()));
        quest.setLocationLabel(normalizeText(candidate.getLabel()));
        quest.setLocationCountryCode(normalizeCountryCode(candidate.getCountryCode()));
        quest.setLocationCountry(normalizeText(candidate.getCountry()));
        quest.setLocationLocality(normalizeText(candidate.getLocality()));
        quest.setLocationPostalCode(normalizeText(candidate.getPostalCode()));
        quest.setLocationStreet(normalizeText(candidate.getStreet()));
        quest.setLocationHouseNumber(normalizeText(candidate.getHouseNumber()));
        quest.setLocationLatitude(normalizeCoordinate(candidate.getLatitude(), "Latitude"));
        quest.setLocationLongitude(normalizeCoordinate(candidate.getLongitude(), "Longitude"));
        quest.setLocationResolvedAt(candidate.getResolvedAt() == null ? Instant.now() : candidate.getResolvedAt());
    }

    private LocationLookupCandidateDTO resolveLocationCandidate(String query, String actorKey) {
        if (locationLookupService == null || query == null) {
            return null;
        }
        return locationLookupService.lookupFirst(query, actorKey);
    }

    private String buildLocationQuery(String street, String houseNumber, String postalCode, String locality, String country, String label) {
        String exactAddress = joinParts(", ",
                joinParts(" ", street, houseNumber),
                postalCode,
                locality,
                country
        );
        if (exactAddress != null) {
            return exactAddress;
        }
        return normalizeText(label);
    }

    private void syncExactVisibilityTargets(AppUser user, UserLocationSettingsRequestDTO dto) {
        List<Long> selectedCircleIds = dto.getExactVisibleCircleIds() == null ? List.of() : dto.getExactVisibleCircleIds();
        List<Long> selectedUserIds = dto.getExactVisibleUserIds() == null ? List.of() : dto.getExactVisibleUserIds();
        ExactLocationVisibilityScope scope = dto.getExactVisibilityScope() == null
                ? ExactLocationVisibilityScope.NOBODY
                : dto.getExactVisibilityScope();

        user.getExactLocationVisibleToCircles().clear();
        user.getExactLocationVisibleToUsers().clear();

        if (scope == ExactLocationVisibilityScope.CIRCLES && selectedCircleIds.isEmpty()) {
            throw ServiceErrors.badRequest("Select at least one circle for exact address visibility");
        }
        if (scope == ExactLocationVisibilityScope.USERS && selectedUserIds.isEmpty()) {
            throw ServiceErrors.badRequest("Select at least one person for exact address visibility");
        }

        user.getExactLocationVisibleToCircles().addAll(circleMembershipService.getOwnedCirclesByIds(user, selectedCircleIds));
        user.getExactLocationVisibleToUsers().addAll(resolveExactVisibleUsers(user, selectedUserIds));
    }

    private List<AppUser> resolveExactVisibleUsers(AppUser owner, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        Set<Long> normalizedIds = new LinkedHashSet<>(userIds);
        List<AppUser> users = appUserRepository.findAllById(normalizedIds);
        if (users.size() != normalizedIds.size()) {
            throw ServiceErrors.badRequest("One or more selected people are invalid");
        }

        for (AppUser candidate : users) {
            if (candidate.getId().equals(owner.getId())) {
                continue;
            }
            if (circleService.getRelationWithUser(owner, candidate.getId()).getRelationStatus() != CircleRelationStatus.CIRCLE) {
                throw ServiceErrors.badRequest("Exact address can only be shared with people from your circles");
            }
        }

        return users;
    }

    private void copyUserLocationToQuest(AppUser sourceUser, Quest quest) {
        quest.setLocationProvider(sourceUser.getLocationProvider());
        quest.setLocationProviderPlaceId(sourceUser.getLocationProviderPlaceId());
        quest.setLocationLabel(sourceUser.getLocationLabel());
        quest.setLocationCountryCode(sourceUser.getLocationCountryCode());
        quest.setLocationCountry(sourceUser.getLocationCountry());
        quest.setLocationLocality(sourceUser.getLocationLocality());
        quest.setLocationPostalCode(sourceUser.getLocationPostalCode());
        quest.setLocationStreet(sourceUser.getLocationStreet());
        quest.setLocationHouseNumber(sourceUser.getLocationHouseNumber());
        quest.setLocationLatitude(sourceUser.getLocationLatitude());
        quest.setLocationLongitude(sourceUser.getLocationLongitude());
        quest.setLocationResolvedAt(sourceUser.getLocationResolvedAt());
    }

    private void clearUserCoordinates(AppUser user) {
        user.setLocationProvider(null);
        user.setLocationProviderPlaceId(null);
        user.setLocationLabel(null);
        user.setLocationCountryCode(null);
        user.setLocationCountry(null);
        user.setLocationLocality(null);
        user.setLocationPostalCode(null);
        user.setLocationStreet(null);
        user.setLocationHouseNumber(null);
        user.setLocationLatitude(null);
        user.setLocationLongitude(null);
        user.setLocationResolvedAt(null);
    }

    private void clearQuestLocation(Quest quest) {
        quest.setLocationProvider(null);
        quest.setLocationProviderPlaceId(null);
        quest.setLocationLabel(null);
        quest.setLocationCountryCode(null);
        quest.setLocationCountry(null);
        quest.setLocationLocality(null);
        quest.setLocationPostalCode(null);
        quest.setLocationStreet(null);
        quest.setLocationHouseNumber(null);
        quest.setLocationLatitude(null);
        quest.setLocationLongitude(null);
        quest.setLocationResolvedAt(null);
    }

    private QuestLocationVisibility resolveEffectiveQuestLocationVisibility(Quest quest) {
        QuestLocationVisibility visibility = quest.getLocationVisibility() == null
                ? QuestLocationVisibility.INHERIT
                : quest.getLocationVisibility();

        if (visibility != QuestLocationVisibility.INHERIT) {
            return visibility;
        }

        UserLocationMode creatorMode = quest.getCreator() == null || quest.getCreator().getLocationMode() == null
                ? UserLocationMode.OFF
                : quest.getCreator().getLocationMode();

        return switch (creatorMode) {
            case OFF -> QuestLocationVisibility.OFF;
            case APPROXIMATE -> QuestLocationVisibility.APPROXIMATE;
            case EXACT -> QuestLocationVisibility.EXACT;
        };
    }

    private String buildApproximateQuestLocationLabel(Quest quest) {
        String locality = normalizeText(quest.getLocationLocality());
        String country = normalizeText(quest.getLocationCountry());
        if (locality != null && country != null) {
            return locality + ", " + country;
        }
        if (locality != null) {
            return locality;
        }
        if (country != null) {
            return country;
        }
        return normalizeText(quest.getLocationLabel());
    }

    private String buildExactQuestLocationLabel(Quest quest) {
        String street = normalizeText(quest.getLocationStreet());
        String houseNumber = normalizeText(quest.getLocationHouseNumber());
        String locality = normalizeText(quest.getLocationLocality());
        String country = normalizeText(quest.getLocationCountry());

        String primary = joinParts(" ", street, houseNumber);
        String secondary = joinParts(", ", locality, country);

        if (primary != null && secondary != null) {
            return primary + ", " + secondary;
        }
        if (primary != null) {
            return primary;
        }
        if (secondary != null) {
            return secondary;
        }
        return normalizeText(quest.getLocationLabel());
    }

    private String joinParts(String delimiter, String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            String normalized = normalizeText(part);
            if (normalized == null) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(delimiter);
            }
            builder.append(normalized);
        }
        return builder.isEmpty() ? null : builder.toString();
    }

    private BigDecimal normalizeCoordinate(BigDecimal value, String label) {
        if (value == null) {
            return null;
        }
        BigDecimal normalized = value.setScale(6, RoundingMode.HALF_UP);
        if ("Latitude".equals(label) && (normalized.doubleValue() < -90 || normalized.doubleValue() > 90)) {
            throw ServiceErrors.badRequest("Latitude must be between -90 and 90");
        }
        if ("Longitude".equals(label) && (normalized.doubleValue() < -180 || normalized.doubleValue() > 180)) {
            throw ServiceErrors.badRequest("Longitude must be between -180 and 180");
        }
        return normalized;
    }

    private String normalizeCountryCode(String value) {
        String normalized = normalizeText(value);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
