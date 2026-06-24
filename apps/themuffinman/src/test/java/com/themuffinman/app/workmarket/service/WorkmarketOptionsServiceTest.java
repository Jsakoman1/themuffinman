package com.themuffinman.app.workmarket.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkmarketOptionsServiceTest {

    private final WorkmarketOptionsService workmarketOptionsService = new WorkmarketOptionsService();

    @Test
    void getOptionsReturnsGeneratedFilterCollections() {
        var result = workmarketOptionsService.getOptions();

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
    }
}
