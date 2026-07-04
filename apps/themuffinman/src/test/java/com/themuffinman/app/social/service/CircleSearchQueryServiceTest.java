package com.themuffinman.app.social.service;

import com.themuffinman.app.social.dto.CircleSearchResultDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CircleSearchQueryServiceTest {

    private final CircleSearchQueryService queryService = new CircleSearchQueryService();

    @Test
    void normalizesAndMatchesBroadSearchQueries() {
        CircleSearchResultDTO candidate = CircleSearchResultDTO.builder()
                .username("Alice")
                .email("alice@example.com")
                .profileDescription("Friendly designer")
                .build();

        assertThat(queryService.normalizeSearchQuery("  ALIce   "))
                .isEqualTo("alice");
        assertThat(queryService.matchesCandidateQuery(candidate, "alice"))
                .isTrue();
        assertThat(queryService.matchesCandidateQuery(candidate, "designer"))
                .isTrue();
        assertThat(queryService.matchesCandidateQuery(candidate, "blocked"))
                .isFalse();
    }
}
