package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.testing.VisionLocationCandidatePresets;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VisionLocationParserServiceTest {

    private final VisionLocationParserService parserService = new VisionLocationParserService();

    @Test
    void parsesStructuredAddressIntoLocationFields() {
        Map<String, String> parsed = parserService.parseCustomLocation(VisionLocationCandidatePresets.ILICA_RESOLVED);

        assertEquals(VisionLocationCandidatePresets.ILICA_RESOLVED, parsed.get("location_label"));
        assertEquals("Ilica", parsed.get("location_street"));
        assertEquals("10", parsed.get("location_house_number"));
        assertEquals("10000", parsed.get("location_postal_code"));
        assertEquals("Zagreb", parsed.get("location_locality"));
        assertEquals("Croatia", parsed.get("location_country"));
    }

    @Test
    void keepsSimpleLocationAsLabelOnly() {
        Map<String, String> parsed = parserService.parseCustomLocation(VisionLocationCandidatePresets.BAN_JELACIC);

        assertEquals(VisionLocationCandidatePresets.BAN_JELACIC, parsed.get("location_label"));
        assertEquals("Ban Jelacic Square", parsed.get("location_street"));
        assertEquals("Zagreb", parsed.get("location_locality"));
        assertNull(parsed.get("location_country"));
    }
}
