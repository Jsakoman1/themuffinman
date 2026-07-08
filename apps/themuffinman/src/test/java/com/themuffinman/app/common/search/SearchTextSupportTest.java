package com.themuffinman.app.common.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchTextSupportTest {

    @Test
    void normalizeQueryTrimsAndRemovesLeadingAtSign() {
        assertEquals("dog groomer", SearchTextSupport.normalizeQuery("  @Dog Groomer "));
    }

    @Test
    void containsAnyNormalizedMatchesAcrossMultipleValues() {
        assertTrue(SearchTextSupport.containsAnyNormalized("groom", "Dog Groomer", null, "Zurich"));
    }
}
