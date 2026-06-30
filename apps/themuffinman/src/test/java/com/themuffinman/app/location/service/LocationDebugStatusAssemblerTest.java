package com.themuffinman.app.location.service;

import com.themuffinman.app.location.dto.DatabaseTableStatusViewDTO;
import com.themuffinman.app.location.dto.LocationDebugStatusViewDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationDebugStatusAssemblerTest {

    private final LocationDebugStatusAssembler locationDebugStatusAssembler = new LocationDebugStatusAssembler();

    @Test
    void buildDebugStatusPackagesLocationMetricsIntoSingleDto() {
        LocationDebugStatusViewDTO status = locationDebugStatusAssembler.buildDebugStatus(
                true,
                "geoapify",
                3L,
                4L,
                5L,
                6L,
                7L,
                8L,
                9L,
                10L,
                11L,
                12L,
                13L,
                14L,
                15L,
                16L,
                17L,
                18L,
                19L,
                List.of(DatabaseTableStatusViewDTO.builder().tableName("app_user").rowCount(10L).build())
        );

        assertEquals(true, status.isConfigured());
        assertEquals("geoapify", status.getProvider());
        assertEquals(16L, status.getCurrentMonthProviderRequests());
        assertEquals(1, status.getTableStatuses().size());
    }
}
