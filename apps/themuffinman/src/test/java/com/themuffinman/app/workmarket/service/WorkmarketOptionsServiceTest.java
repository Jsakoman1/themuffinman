package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.location.service.LocationGeoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class WorkmarketOptionsServiceTest {

    @Mock
    private LocationGeoService locationGeoService;

    @Test
    void getOptionsReturnsGeneratedFilterCollections() {
        var workmarketOptionsService = new WorkmarketOptionsService(locationGeoService);
        var result = workmarketOptionsService.getOptions(null);

        assertTrue(result.getQuestStatusFilters().stream().anyMatch(option -> "ALL".equals(option.getValue())));
        assertTrue(result.getQuestApplicationStatusFilters().stream().anyMatch(option -> "PENDING".equals(option.getValue())));
        assertTrue(result.getQuestAudienceFilters().stream().anyMatch(option -> "ALL".equals(option.getValue())));
        assertEquals("Everyone", result.getQuestAudienceFilters().stream()
                .filter(option -> "EVERYONE".equals(option.getValue()))
                .findFirst()
                .orElseThrow()
                .getLabel());
        assertEquals("Soonest", result.getQuestSortOptions().stream()
                .filter(option -> "newest".equals(option.getValue()))
                .findFirst()
                .orElseThrow()
                .getLabel());
        assertEquals("Best match", result.getQuestSortOptions().stream()
                .filter(option -> "recommended".equals(option.getValue()))
                .findFirst()
                .orElseThrow()
                .getLabel());
        assertEquals(List.of(5, 10, 20, 30), result.getQuestSearchDefaults().getRadiusOptionsKm());
    }
}
