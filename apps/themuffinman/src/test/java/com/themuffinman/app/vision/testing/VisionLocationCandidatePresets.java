package com.themuffinman.app.vision.testing;

import com.themuffinman.app.location.dto.LocationLookupCandidateDTO;

import java.util.List;

public final class VisionLocationCandidatePresets {

    public static final String ILICA_TYPED = "Ilica 10, Zagreb";
    public static final String ILICA_RESOLVED = "Ilica 10, 10000 Zagreb, Croatia";
    public static final String BAN_JELACIC = "Ban Jelacic Square, Zagreb";

    private VisionLocationCandidatePresets() {
    }

    public static LocationLookupCandidateDTO ilicaResolvedCandidate() {
        return LocationLookupCandidateDTO.builder()
                .label(ILICA_RESOLVED)
                .countryCode("HR")
                .country("Croatia")
                .locality("Zagreb")
                .postalCode("10000")
                .street("Ilica")
                .houseNumber("10")
                .provider("openai-geo")
                .build();
    }

    public static List<LocationLookupCandidateDTO> dualIlicaCandidates() {
        return List.of(
                LocationLookupCandidateDTO.builder()
                        .label("Ilica 8, 10000 Zagreb, Croatia")
                        .street("Ilica")
                        .houseNumber("8")
                        .locality("Zagreb")
                        .postalCode("10000")
                        .country("Croatia")
                        .build(),
                ilicaResolvedCandidate()
        );
    }
}
