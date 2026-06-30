package com.themuffinman.app.social.service;

import com.themuffinman.app.common.normalization.SearchQueryNormalizer;
import com.themuffinman.app.social.dto.AdminCircleGroupResponseDTO;
import com.themuffinman.app.social.dto.AdminCircleOverviewDTO;
import com.themuffinman.app.social.dto.AdminCircleRelationRowDTO;
import com.themuffinman.app.social.model.CircleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CircleAdminOverviewAssembler {

    private final SocialPresentationHelper socialPresentationHelper;

    public AdminCircleOverviewDTO buildAdminOverview(
            List<AdminCircleGroupResponseDTO> circles,
            List<CircleRequest> relations,
            String query
    ) {
        String normalizedQuery = normalizeSearchQuery(query);
        return AdminCircleOverviewDTO.builder()
                .circles(circles.stream()
                        .filter(circle -> matchesAdminCircleQuery(circle, normalizedQuery))
                        .toList())
                .acceptedConnections(buildAdminRelationRows(relations, normalizedQuery, AdminRelationType.ACCEPTED))
                .pendingRequests(buildAdminRelationRows(relations, normalizedQuery, AdminRelationType.PENDING))
                .blockedRelations(buildAdminRelationRows(relations, normalizedQuery, AdminRelationType.BLOCKED))
                .build();
    }

    private List<AdminCircleRelationRowDTO> buildAdminRelationRows(
            List<CircleRequest> relations,
            String normalizedQuery,
            AdminRelationType type
    ) {
        return relations.stream()
                .filter(type::matches)
                .filter(relation -> matchesAdminRelationQuery(relation, normalizedQuery))
                .map(relation -> toAdminRelationRow(relation, type))
                .toList();
    }

    private AdminCircleRelationRowDTO toAdminRelationRow(CircleRequest relation, AdminRelationType type) {
        return AdminCircleRelationRowDTO.builder()
                .id(relation.getId())
                .requesterUsername(relation.getRequester().getUsername())
                .recipientUsername(relation.getRecipient().getUsername())
                .statusLabel(type.statusLabel)
                .statusBadgeClass(type.statusBadgeClass)
                .build();
    }

    private boolean matchesAdminCircleQuery(AdminCircleGroupResponseDTO circle, String normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            return true;
        }
        return containsNormalized(normalizedQuery,
                circle.getName(),
                circle.getOwnerUsername(),
                circle.getMemberPreviewLabel()
        );
    }

    private boolean matchesAdminRelationQuery(CircleRequest relation, String normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            return true;
        }
        return containsNormalized(normalizedQuery,
                relation.getRequester().getUsername(),
                relation.getRecipient().getUsername()
        );
    }

    private String normalizeSearchQuery(String query) {
        return SearchQueryNormalizer.normalize(query).toLowerCase();
    }

    private boolean containsNormalized(String normalizedQuery, String... values) {
        return normalizedHaystack(values).contains(normalizedQuery);
    }

    private String normalizedHaystack(String... values) {
        return java.util.Arrays.stream(values)
                .map(value -> value == null ? "" : value)
                .collect(Collectors.joining(" "))
                .toLowerCase();
    }

    private enum AdminRelationType {
        ACCEPTED("Accepted", "badge--success") {
            @Override
            boolean matches(CircleRequest relation) {
                return relation.getAcceptedAt() != null && relation.getBlockedAt() == null;
            }
        },
        PENDING("Pending", "badge--warning") {
            @Override
            boolean matches(CircleRequest relation) {
                return relation.getAcceptedAt() == null && relation.getBlockedAt() == null;
            }
        },
        BLOCKED("Blocked", "badge--danger") {
            @Override
            boolean matches(CircleRequest relation) {
                return relation.getBlockedAt() != null;
            }
        };

        private final String statusLabel;
        private final String statusBadgeClass;

        AdminRelationType(String statusLabel, String statusBadgeClass) {
            this.statusLabel = statusLabel;
            this.statusBadgeClass = statusBadgeClass;
        }

        abstract boolean matches(CircleRequest relation);
    }
}
