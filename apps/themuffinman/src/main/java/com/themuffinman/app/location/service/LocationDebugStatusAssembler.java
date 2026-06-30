package com.themuffinman.app.location.service;

import com.themuffinman.app.location.dto.DatabaseTableStatusViewDTO;
import com.themuffinman.app.location.dto.LocationDebugStatusViewDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationDebugStatusAssembler {

    public LocationDebugStatusViewDTO buildDebugStatus(
            boolean configured,
            String provider,
            long lookupCacheEntries,
            long reverseLookupCacheEntries,
            long lookupRequests,
            long reverseLookupRequests,
            long providerLookupCalls,
            long providerReverseLookupCalls,
            long lookupCacheHits,
            long reverseLookupCacheHits,
            long rateLimitedRequests,
            long usersWithCoordinates,
            long questsWithCoordinates,
            long usersWithProviderMetadata,
            long questsWithProviderMetadata,
            long currentMonthProviderRequests,
            long currentMonthProviderLookupRequests,
            long currentMonthProviderReverseLookupRequests,
            long databaseSizeBytes,
            List<DatabaseTableStatusViewDTO> tableStatuses
    ) {
        return LocationDebugStatusViewDTO.builder()
                .configured(configured)
                .provider(provider)
                .lookupCacheEntries(lookupCacheEntries)
                .reverseLookupCacheEntries(reverseLookupCacheEntries)
                .lookupRequests(lookupRequests)
                .reverseLookupRequests(reverseLookupRequests)
                .providerLookupCalls(providerLookupCalls)
                .providerReverseLookupCalls(providerReverseLookupCalls)
                .lookupCacheHits(lookupCacheHits)
                .reverseLookupCacheHits(reverseLookupCacheHits)
                .rateLimitedRequests(rateLimitedRequests)
                .usersWithCoordinates(usersWithCoordinates)
                .questsWithCoordinates(questsWithCoordinates)
                .usersWithProviderMetadata(usersWithProviderMetadata)
                .questsWithProviderMetadata(questsWithProviderMetadata)
                .currentMonthProviderRequests(currentMonthProviderRequests)
                .currentMonthProviderLookupRequests(currentMonthProviderLookupRequests)
                .currentMonthProviderReverseLookupRequests(currentMonthProviderReverseLookupRequests)
                .databaseSizeBytes(databaseSizeBytes)
                .tableStatuses(tableStatuses)
                .build();
    }
}
