package com.themuffinman.app.location.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.location.dto.DatabaseTableStatusViewDTO;
import com.themuffinman.app.location.dto.LocationDebugStatusViewDTO;
import com.themuffinman.app.location.dto.LocationLookupCandidateDTO;
import com.themuffinman.app.location.dto.LocationLookupResponseDTO;
import com.themuffinman.app.location.model.LocationLookupEvent;
import com.themuffinman.app.location.model.LocationLookupEventType;
import com.themuffinman.app.location.repository.LocationLookupEventRepository;
import com.themuffinman.app.workmarket.repository.QuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationLookupServiceTest {

    @Mock
    private GeoapifyLocationLookupClient geoapifyLocationLookupClient;

    @Mock
    private DisabledLocationLookupClient disabledLocationLookupClient;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private QuestRepository questRepository;

    @Mock
    private LocationLookupEventRepository locationLookupEventRepository;

    @Mock
    private AdminDatabaseMetricsService adminDatabaseMetricsService;

    @Mock
    private LocationDebugStatusAssembler locationDebugStatusAssembler;

    private LocationLookupService locationLookupService;

    @BeforeEach
    void setUp() {
        locationLookupService = new LocationLookupService(
                geoapifyLocationLookupClient,
                disabledLocationLookupClient,
                appUserRepository,
                questRepository,
                locationLookupEventRepository,
                adminDatabaseMetricsService,
                locationDebugStatusAssembler
        );
    }

    @Test
    void lookupRecordsProviderCallAndCachesResponse() {
        LocationLookupCandidateDTO candidate = LocationLookupCandidateDTO.builder()
                .provider("geoapify")
                .label("Villigen")
                .latitude(BigDecimal.ONE)
                .longitude(BigDecimal.TEN)
                .resolvedAt(Instant.now())
                .build();
        when(geoapifyLocationLookupClient.isConfigured()).thenReturn(true);
        when(geoapifyLocationLookupClient.providerName()).thenReturn("geoapify");
        when(geoapifyLocationLookupClient.lookup("villigen")).thenReturn(List.of(candidate));

        LocationLookupResponseDTO first = locationLookupService.lookup("villigen", "jsak");
        LocationLookupResponseDTO second = locationLookupService.lookup("villigen", "jsak");

        assertSame(first, second);
        verify(locationLookupEventRepository).save(any(LocationLookupEvent.class));
    }

    @Test
    void reverseLookupRecordsReverseProviderCall() {
        LocationLookupCandidateDTO candidate = LocationLookupCandidateDTO.builder()
                .provider("geoapify")
                .label("Villigen")
                .latitude(BigDecimal.ONE)
                .longitude(BigDecimal.TEN)
                .resolvedAt(Instant.now())
                .build();
        when(geoapifyLocationLookupClient.isConfigured()).thenReturn(true);
        when(geoapifyLocationLookupClient.providerName()).thenReturn("geoapify");
        when(geoapifyLocationLookupClient.reverseLookup(BigDecimal.ONE, BigDecimal.TEN)).thenReturn(candidate);

        locationLookupService.reverseLookup(BigDecimal.ONE, BigDecimal.TEN, "jsak");

        ArgumentCaptor<LocationLookupEvent> captor = ArgumentCaptor.forClass(LocationLookupEvent.class);
        verify(locationLookupEventRepository).save(captor.capture());
        assertEquals(LocationLookupEventType.REVERSE_LOOKUP, captor.getValue().getRequestType());
        assertEquals("geoapify", captor.getValue().getProvider());
    }

    @Test
    void getDebugStatusIncludesMonthlyAndDatabaseMetrics() {
        when(geoapifyLocationLookupClient.isConfigured()).thenReturn(true);
        when(geoapifyLocationLookupClient.providerName()).thenReturn("geoapify");
        when(appUserRepository.countByLocationLatitudeIsNotNullAndLocationLongitudeIsNotNull()).thenReturn(2L);
        when(questRepository.countByLocationLatitudeIsNotNullAndLocationLongitudeIsNotNull()).thenReturn(3L);
        when(appUserRepository.countByLocationProviderPlaceIdIsNotNull()).thenReturn(4L);
        when(questRepository.countByLocationProviderPlaceIdIsNotNull()).thenReturn(5L);
        when(locationLookupEventRepository.countByCreatedAtGreaterThanEqual(any())).thenReturn(12L);
        when(locationLookupEventRepository.countByCreatedAtGreaterThanEqualAndRequestType(any(), any(LocationLookupEventType.class)))
                .thenAnswer(invocation -> invocation.getArgument(1) == LocationLookupEventType.LOOKUP ? 7L : 5L);
        when(adminDatabaseMetricsService.getDatabaseSizeBytes()).thenReturn(9_437_184L);
        when(adminDatabaseMetricsService.getTableStatuses()).thenReturn(List.of(
                DatabaseTableStatusViewDTO.builder().tableName("app_user").rowCount(10L).build(),
                DatabaseTableStatusViewDTO.builder().tableName("quest").rowCount(20L).build()
        ));
        LocationDebugStatusViewDTO expected = LocationDebugStatusViewDTO.builder().provider("geoapify").build();
        when(locationDebugStatusAssembler.buildDebugStatus(
                anyBoolean(),
                anyString(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                any()
        )).thenReturn(expected);

        LocationDebugStatusViewDTO status = locationLookupService.getDebugStatus();

        assertSame(expected, status);
    }

    @Test
    void lookupDoesNotRecordProviderCallWhenClientDisabled() {
        when(geoapifyLocationLookupClient.isConfigured()).thenReturn(false);
        when(disabledLocationLookupClient.isConfigured()).thenReturn(false);
        when(disabledLocationLookupClient.providerName()).thenReturn("disabled");

        locationLookupService.lookup("villigen", "jsak");

        verify(locationLookupEventRepository, never()).save(any(LocationLookupEvent.class));
    }
}
