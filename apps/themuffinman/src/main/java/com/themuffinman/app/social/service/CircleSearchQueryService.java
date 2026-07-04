package com.themuffinman.app.social.service;

import com.themuffinman.app.common.normalization.SearchQueryNormalizer;
import com.themuffinman.app.social.dto.CircleContactDTO;
import com.themuffinman.app.social.dto.CircleSearchResultDTO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class CircleSearchQueryService {

    public String normalizeSearchQuery(String query) {
        return SearchQueryNormalizer.normalize(query).toLowerCase();
    }

    public boolean matchesConnectionQuery(CircleContactDTO connection, String query) {
        String normalizedQuery = normalizeSearchQuery(query);
        if (normalizedQuery.isBlank()) {
            return true;
        }

        return containsNormalized(normalizedQuery,
                connection.getUsername(),
                connection.getProfileDescription(),
                String.join(" ", connection.getCircleNames())
        );
    }

    public boolean matchesRequestQuery(String username, String profileDescription, String query) {
        String normalizedQuery = normalizeSearchQuery(query);
        if (normalizedQuery.isBlank()) {
            return true;
        }
        return containsNormalized(normalizedQuery, username, profileDescription);
    }

    public boolean matchesCandidateQuery(CircleSearchResultDTO candidate, String normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            return true;
        }
        return containsNormalized(normalizedQuery,
                candidate.getUsername(),
                candidate.getEmail(),
                candidate.getProfileDescription()
        );
    }

    private boolean containsNormalized(String normalizedQuery, String... values) {
        return normalizedHaystack(values).contains(normalizedQuery);
    }

    private String normalizedHaystack(String... values) {
        return Arrays.stream(values)
                .map(value -> value == null ? "" : value)
                .collect(Collectors.joining(" "))
                .toLowerCase();
    }
}
