package com.themuffinman.app.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDebugStatusDTO {
    private boolean configured;
    private String provider;
    private long lookupCacheEntries;
    private long reverseLookupCacheEntries;
    private long lookupRequests;
    private long reverseLookupRequests;
    private long providerLookupCalls;
    private long providerReverseLookupCalls;
    private long lookupCacheHits;
    private long reverseLookupCacheHits;
    private long rateLimitedRequests;
    private long usersWithCoordinates;
    private long questsWithCoordinates;
    private long usersWithProviderMetadata;
    private long questsWithProviderMetadata;
    private long currentMonthProviderRequests;
    private long currentMonthProviderLookupRequests;
    private long currentMonthProviderReverseLookupRequests;
    private long databaseSizeBytes;
    private List<DatabaseTableStatusDTO> tableStatuses;
}
