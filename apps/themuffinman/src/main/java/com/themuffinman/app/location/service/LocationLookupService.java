package com.themuffinman.app.location.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.location.dto.LocationLookupCandidateDTO;
import com.themuffinman.app.location.dto.LocationLookupResponseDTO;
import com.themuffinman.app.location.model.LocationLookupEvent;
import com.themuffinman.app.location.model.LocationLookupEventType;
import com.themuffinman.app.location.repository.LocationLookupEventRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import com.themuffinman.app.vision.repository.QuestRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationLookupService {
    private static final int MIN_QUERY_LENGTH = 3;
    private static final int LOOKUP_LIMIT_PER_MINUTE = 30;
    private static final int REVERSE_LOOKUP_LIMIT_PER_MINUTE = 20;

    private final GeoapifyLocationLookupClient geoapifyLocationLookupClient;
    private final DisabledLocationLookupClient disabledLocationLookupClient;
    private final AppUserRepository appUserRepository;
    private final QuestRepository questRepository;
    private final LocationLookupEventRepository locationLookupEventRepository;
    private final AdminDatabaseMetricsService adminDatabaseMetricsService;
    private final LocationDebugStatusAssembler locationDebugStatusAssembler;
    private final Cache<String, LocationLookupResponseDTO> lookupCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(14))
            .maximumSize(2_000)
            .build();
    private final Cache<String, LocationLookupCandidateDTO> reverseLookupCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(60))
            .maximumSize(2_000)
            .build();
    private final Cache<String, AtomicInteger> lookupRateLimitCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(1))
            .maximumSize(5_000)
            .build();
    private final Cache<String, AtomicInteger> reverseLookupRateLimitCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(1))
            .maximumSize(5_000)
            .build();
    private final AtomicLong lookupRequests = new AtomicLong();
    private final AtomicLong reverseLookupRequests = new AtomicLong();
    private final AtomicLong providerLookupCalls = new AtomicLong();
    private final AtomicLong providerReverseLookupCalls = new AtomicLong();
    private final AtomicLong lookupCacheHits = new AtomicLong();
    private final AtomicLong reverseLookupCacheHits = new AtomicLong();
    private final AtomicLong rateLimitedRequests = new AtomicLong();

    public LocationLookupResponseDTO lookup(String query, String actorKey) {
        lookupRequests.incrementAndGet();
        LocationLookupClient client = geoapifyLocationLookupClient.isConfigured()
                ? geoapifyLocationLookupClient
                : disabledLocationLookupClient;

        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery == null) {
            return emptyResponse(client);
        }

        if (!client.isConfigured()) {
            return emptyResponse(client);
        }

        String cacheKey = client.providerName() + "::" + normalizedQuery;
        LocationLookupResponseDTO cached = lookupCache.getIfPresent(cacheKey);
        if (cached != null) {
            lookupCacheHits.incrementAndGet();
            return cached;
        }

        assertWithinRateLimit(lookupRateLimitCache, actorKey, LOOKUP_LIMIT_PER_MINUTE, "location search");
        providerLookupCalls.incrementAndGet();
        recordProviderCall(client.providerName(), LocationLookupEventType.LOOKUP);
        LocationLookupResponseDTO response = LocationLookupResponseDTO.builder()
                .configured(true)
                .provider(client.providerName())
                .items(client.lookup(normalizedQuery))
                .build();
        lookupCache.put(cacheKey, response);
        maybeLogMetrics();
        return response;
    }

    public LocationLookupCandidateDTO lookupFirst(String query, String actorKey) {
        LocationLookupResponseDTO response = lookup(query, actorKey);
        if (response.getItems() == null || response.getItems().isEmpty()) {
            return null;
        }
        return response.getItems().getFirst();
    }

    public List<LocationLookupCandidateDTO> lookupTopCandidates(String query, String actorKey, int limit) {
        LocationLookupResponseDTO response = lookup(query, actorKey);
        if (response.getItems() == null || response.getItems().isEmpty() || limit <= 0) {
            return List.of();
        }
        return response.getItems().stream()
                .limit(limit)
                .toList();
    }

    public LocationLookupCandidateDTO reverseLookup(BigDecimal latitude, BigDecimal longitude, String actorKey) {
        reverseLookupRequests.incrementAndGet();
        LocationLookupClient client = geoapifyLocationLookupClient.isConfigured()
                ? geoapifyLocationLookupClient
                : disabledLocationLookupClient;

        if (!client.isConfigured()) {
            throw ServiceErrors.badRequest("Location lookup provider is not configured");
        }

        String cacheKey = buildReverseLookupCacheKey(client.providerName(), latitude, longitude);
        LocationLookupCandidateDTO cached = reverseLookupCache.getIfPresent(cacheKey);
        if (cached != null) {
            reverseLookupCacheHits.incrementAndGet();
            return cached;
        }

        assertWithinRateLimit(reverseLookupRateLimitCache, actorKey, REVERSE_LOOKUP_LIMIT_PER_MINUTE, "reverse location search");
        providerReverseLookupCalls.incrementAndGet();
        recordProviderCall(client.providerName(), LocationLookupEventType.REVERSE_LOOKUP);
        LocationLookupCandidateDTO candidate = client.reverseLookup(latitude, longitude);
        if (candidate == null) {
            throw ServiceErrors.notFound("Could not resolve a location for those coordinates");
        }

        reverseLookupCache.put(cacheKey, candidate);
        maybeLogMetrics();
        return candidate;
    }

    @Transactional(readOnly = true)
    public com.themuffinman.app.location.dto.LocationDebugStatusViewDTO getDebugStatus() {
        LocationLookupClient client = geoapifyLocationLookupClient.isConfigured()
                ? geoapifyLocationLookupClient
                : disabledLocationLookupClient;
        Instant monthStart = OffsetDateTime.now(ZoneOffset.UTC)
                .withDayOfMonth(1)
                .toLocalDate()
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);
        long currentMonthLookupRequests = locationLookupEventRepository.countByCreatedAtGreaterThanEqualAndRequestType(monthStart, LocationLookupEventType.LOOKUP);
        long currentMonthReverseLookupRequests = locationLookupEventRepository.countByCreatedAtGreaterThanEqualAndRequestType(monthStart, LocationLookupEventType.REVERSE_LOOKUP);

        return locationDebugStatusAssembler.buildDebugStatus(
                client.isConfigured(),
                client.providerName(),
                lookupCache.estimatedSize(),
                reverseLookupCache.estimatedSize(),
                lookupRequests.get(),
                reverseLookupRequests.get(),
                providerLookupCalls.get(),
                providerReverseLookupCalls.get(),
                lookupCacheHits.get(),
                reverseLookupCacheHits.get(),
                rateLimitedRequests.get(),
                appUserRepository.countByLocationLatitudeIsNotNullAndLocationLongitudeIsNotNull(),
                questRepository.countByLocationLatitudeIsNotNullAndLocationLongitudeIsNotNull(),
                appUserRepository.countByLocationProviderPlaceIdIsNotNull(),
                questRepository.countByLocationProviderPlaceIdIsNotNull(),
                locationLookupEventRepository.countByCreatedAtGreaterThanEqual(monthStart),
                currentMonthLookupRequests,
                currentMonthReverseLookupRequests,
                adminDatabaseMetricsService.getDatabaseSizeBytes(),
                adminDatabaseMetricsService.getTableStatuses()
        );
    }

    private void recordProviderCall(String provider, LocationLookupEventType requestType) {
        LocationLookupEvent event = new LocationLookupEvent();
        event.setProvider(provider);
        event.setRequestType(requestType);
        event.setCreatedAt(Instant.now());
        locationLookupEventRepository.save(event);
    }

    private LocationLookupResponseDTO emptyResponse(LocationLookupClient client) {
        return LocationLookupResponseDTO.builder()
                .configured(client.isConfigured())
                .provider(client.providerName())
                .items(List.of())
                .build();
    }

    private void assertWithinRateLimit(Cache<String, AtomicInteger> cache, String actorKey, int limit, String actionLabel) {
        String normalizedActorKey = actorKey == null || actorKey.isBlank() ? "anonymous" : actorKey.trim().toLowerCase();
        AtomicInteger counter = cache.get(normalizedActorKey, key -> new AtomicInteger());
        int current = counter.incrementAndGet();
        if (current <= limit) {
            return;
        }

        rateLimitedRequests.incrementAndGet();
        log.warn("Location lookup rate limited for actor={} action={}", normalizedActorKey, actionLabel);
        throw ServiceErrors.badRequest("Too many location requests. Please wait a moment and try again.");
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }

        String normalized = query.trim().replaceAll("\\s+", " ");
        if (normalized.length() < MIN_QUERY_LENGTH) {
            return null;
        }

        return normalized;
    }

    private String buildReverseLookupCacheKey(String provider, BigDecimal latitude, BigDecimal longitude) {
        BigDecimal roundedLatitude = latitude.setScale(4, RoundingMode.HALF_UP);
        BigDecimal roundedLongitude = longitude.setScale(4, RoundingMode.HALF_UP);
        return provider + "::" + roundedLatitude.toPlainString() + "::" + roundedLongitude.toPlainString();
    }

    private void maybeLogMetrics() {
        long totalProviderCalls = providerLookupCalls.get() + providerReverseLookupCalls.get();
        if (totalProviderCalls == 0 || totalProviderCalls % 25 != 0) {
            return;
        }

        log.info(
                "Location lookup metrics: lookupRequests={}, reverseLookupRequests={}, lookupCacheHits={}, reverseLookupCacheHits={}, providerLookupCalls={}, providerReverseLookupCalls={}, rateLimitedRequests={}",
                lookupRequests.get(),
                reverseLookupRequests.get(),
                lookupCacheHits.get(),
                reverseLookupCacheHits.get(),
                providerLookupCalls.get(),
                providerReverseLookupCalls.get(),
                rateLimitedRequests.get()
        );
    }
}
