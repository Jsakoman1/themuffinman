package com.themuffinman.app.social.service;

import com.themuffinman.app.common.search.SearchTextSupport;
import com.themuffinman.app.social.dto.CircleContactDTO;
import com.themuffinman.app.social.dto.CircleSearchResultDTO;
import org.springframework.stereotype.Service;

@Service
public class CircleSearchQueryService {

    public String normalizeSearchQuery(String query) {
        return SearchTextSupport.normalizeQuery(query);
    }

    public boolean matchesConnectionQuery(CircleContactDTO connection, String query) {
        String normalizedQuery = normalizeSearchQuery(query);
        if (normalizedQuery.isBlank()) {
            return true;
        }

        return SearchTextSupport.containsAnyNormalized(normalizedQuery,
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
        return SearchTextSupport.containsAnyNormalized(normalizedQuery, username, profileDescription);
    }

    public boolean matchesCandidateQuery(CircleSearchResultDTO candidate, String normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            return true;
        }
        return SearchTextSupport.containsAnyNormalized(normalizedQuery,
                candidate.getUsername(),
                candidate.getEmail(),
                candidate.getProfileDescription()
        );
    }
}
