package com.themuffinman.app.vision.service;

import com.themuffinman.app.location.service.LocationLookupService;
import com.themuffinman.app.vision.testing.VisionLocationCandidatePresets;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class VisionLocationResolutionServiceTest {

    @Test
    void storesPendingLookupCandidateWhenAvailable() {
        VisionLocationParserService parserService = new VisionLocationParserService();
        LocationLookupService lookupService = Mockito.mock(LocationLookupService.class);
        VisionLocationResolutionService resolutionService = new VisionLocationResolutionService(parserService, lookupService);

        when(lookupService.lookupTopCandidates(VisionLocationCandidatePresets.ILICA_TYPED, "vision:user:7", 3))
                .thenReturn(List.of(VisionLocationCandidatePresets.ilicaResolvedCandidate()));

        Map<String, String> resolved = resolutionService.resolveCustomLocation(
                VisionLocationCandidatePresets.ILICA_TYPED,
                "vision:user:7"
        );

        assertEquals(VisionLocationCandidatePresets.ILICA_TYPED, resolved.get("location_label"));
        assertEquals("1", resolved.get("pending_location_candidate_count"));
        assertEquals(VisionLocationCandidatePresets.ILICA_RESOLVED, resolved.get("pending_location_candidate_1_label"));
        assertEquals("1", resolved.get("pending_location_candidate_1_rank"));
        assertEquals("Top ranked match with the same street, house number, and locality as your typed place.",
                resolved.get("pending_location_candidate_1_match_note"));
        assertEquals("HR", resolved.get("pending_location_candidate_1_country_code"));
        assertEquals("Croatia", resolved.get("pending_location_candidate_1_country"));
        assertEquals("10000", resolved.get("pending_location_candidate_1_postal_code"));
        assertEquals("candidate_pending", resolved.get("location_resolution_status"));
    }

    @Test
    void keepsParsedLocationWhenLookupIsUnavailable() {
        VisionLocationParserService parserService = new VisionLocationParserService();
        VisionLocationResolutionService resolutionService = new VisionLocationResolutionService(parserService);

        Map<String, String> resolved = resolutionService.resolveCustomLocation(
                VisionLocationCandidatePresets.BAN_JELACIC,
                "vision:user:7"
        );

        assertEquals(VisionLocationCandidatePresets.BAN_JELACIC, resolved.get("location_label"));
        assertEquals("Ban Jelacic Square", resolved.get("location_street"));
        assertEquals("Zagreb", resolved.get("location_locality"));
        assertEquals("parsed_only", resolved.get("location_resolution_status"));
    }
}
