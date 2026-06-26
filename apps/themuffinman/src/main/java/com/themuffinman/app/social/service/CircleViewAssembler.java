package com.themuffinman.app.social.service;

import com.themuffinman.app.common.normalization.SearchQueryNormalizer;
import com.themuffinman.app.common.pagination.PageResponseFactory;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.dto.AdminCircleGroupResponseDTO;
import com.themuffinman.app.social.dto.AdminCircleOverviewDTO;
import com.themuffinman.app.social.dto.AdminCircleRelationRowDTO;
import com.themuffinman.app.social.dto.CircleContactDTO;
import com.themuffinman.app.social.dto.CircleContactListResponseDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleMemberDTO;
import com.themuffinman.app.social.dto.CircleOverviewDTO;
import com.themuffinman.app.social.dto.CircleRelationDTO;
import com.themuffinman.app.social.dto.CircleRelationStatus;
import com.themuffinman.app.social.dto.CircleRequestListResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.dto.CircleSearchResultDTO;
import com.themuffinman.app.social.dto.CircleSearchResultListResponseDTO;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.model.CircleMembership;
import com.themuffinman.app.social.model.CircleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CircleViewAssembler {

    private final SocialPresentationHelper socialPresentationHelper;
    private final SocialRelationActionHelper socialRelationActionHelper;

    public CircleOverviewDTO buildOverview(
            List<CircleContactDTO> connections,
            List<CircleRequestResponseDTO> incomingRequests,
            List<CircleRequestResponseDTO> outgoingRequests
    ) {
        return CircleOverviewDTO.builder()
                .connectionCount(connections.size())
                .unassignedConnectionCount(connections.stream().filter(connection -> connection.getCircleIds().isEmpty()).count())
                .incomingRequestCount(incomingRequests.size())
                .outgoingRequestCount(outgoingRequests.size())
                .build();
    }

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

    public CircleContactDTO toContact(AppUser currentUser, CircleRequest relation, Map<Long, List<CircleMembership>> membershipsByUserId) {
        AppUser contact = relation.getRequester().getId().equals(currentUser.getId())
                ? relation.getRecipient()
                : relation.getRequester();
        List<CircleMembership> memberships = membershipsByUserId.getOrDefault(contact.getId(), List.of());
        List<String> circleNames = memberships.stream()
                .map(membership -> membership.getCircle().getName())
                .toList();

        return CircleContactDTO.builder()
                .relationId(relation.getId())
                .userId(contact.getId())
                .username(contact.getUsername())
                .profileDescription(RichTextInputValidator.sanitize(contact.getProfileDescription()))
                .profileAvatarDataUrl(contact.getProfileAvatarDataUrl())
                .circleIds(memberships.stream().map(membership -> membership.getCircle().getId()).toList())
                .circleNames(circleNames)
                .circleSummaryLabel(socialPresentationHelper.circleSummaryLabel(circleNames))
                .build();
    }

    public CircleGroupResponseDTO toCircleDto(CircleGroup circle) {
        List<CircleMemberDTO> members = circle.getMemberships().stream()
                .map(CircleMembership::getMember)
                .sorted(Comparator.comparing(AppUser::getUsername, String.CASE_INSENSITIVE_ORDER))
                .map(this::toCircleMemberDto)
                .toList();
        List<String> memberUsernames = members.stream()
                .map(CircleMemberDTO::getUsername)
                .toList();

        return CircleGroupResponseDTO.builder()
                .id(circle.getId())
                .name(circle.getName())
                .memberCount(members.size())
                .memberPreviewLabel(socialPresentationHelper.memberPreviewLabel(memberUsernames))
                .members(members)
                .build();
    }

    public AdminCircleGroupResponseDTO toAdminCircleDto(CircleGroup circle) {
        List<CircleMemberDTO> members = circle.getMemberships().stream()
                .map(CircleMembership::getMember)
                .sorted(Comparator.comparing(AppUser::getUsername, String.CASE_INSENSITIVE_ORDER))
                .map(this::toCircleMemberDto)
                .toList();
        List<String> memberUsernames = members.stream()
                .map(CircleMemberDTO::getUsername)
                .toList();

        return AdminCircleGroupResponseDTO.builder()
                .id(circle.getId())
                .name(circle.getName())
                .ownerId(circle.getOwner().getId())
                .ownerUsername(circle.getOwner().getUsername())
                .memberCount(members.size())
                .memberPreviewLabel(socialPresentationHelper.memberPreviewLabel(memberUsernames))
                .members(members)
                .build();
    }

    public CircleRequestResponseDTO toViewerRequest(CircleRequestResponseDTO request, boolean incoming) {
        if (incoming) {
            request.setCounterpartUserId(request.getRequesterId());
            request.setCounterpartUsername(request.getRequesterUsername());
            request.setCounterpartProfileDescription(request.getRequesterProfileDescription());
            request.setCounterpartProfileAvatarDataUrl(request.getRequesterProfileAvatarDataUrl());
        } else {
            request.setCounterpartUserId(request.getRecipientId());
            request.setCounterpartUsername(request.getRecipientUsername());
            request.setCounterpartProfileDescription(request.getRecipientProfileDescription());
            request.setCounterpartProfileAvatarDataUrl(request.getRecipientProfileAvatarDataUrl());
        }

        request.setRequestSummaryLabel(socialPresentationHelper.requestSummaryLabel(incoming));
        return request;
    }

    public CircleSearchResultDTO toSearchResult(AppUser currentUser, AppUser candidate, Optional<CircleRequest> relation) {
        CircleRelationStatus relationStatus = resolveRelationStatus(relation, currentUser.getId());
        boolean blockedByCurrentUser = isBlockedByCurrentUser(relation, currentUser.getId());
        SocialRelationActionHelper.SearchActions actions = socialRelationActionHelper.searchActions(relationStatus, blockedByCurrentUser);

        return CircleSearchResultDTO.builder()
                .id(candidate.getId())
                .username(candidate.getUsername())
                .profileDescription(RichTextInputValidator.sanitize(candidate.getProfileDescription()))
                .profileAvatarDataUrl(candidate.getProfileAvatarDataUrl())
                .email(candidate.getEmail())
                .relationStatus(relationStatus)
                .relationLabel(socialPresentationHelper.relationLabel(relationStatus))
                .relationBadgeClass(socialPresentationHelper.relationBadgeClass(relationStatus))
                .primaryAction(actions.getPrimaryAction())
                .secondaryAction(actions.getSecondaryAction())
                .blockedByCurrentUser(blockedByCurrentUser)
                .build();
    }

    public CircleSearchResultDTO withLocationDetails(
            CircleSearchResultDTO candidate,
            String locationLabel,
            Double distanceKm,
            String distanceLabel
    ) {
        candidate.setLocationLabel(locationLabel);
        candidate.setDistanceKm(distanceKm);
        candidate.setDistanceLabel(distanceLabel);
        return candidate;
    }

    public CircleRelationDTO toRelationDto(CircleRelationStatus relationStatus, boolean blockedByCurrentUser) {
        return CircleRelationDTO.builder()
                .relationStatus(relationStatus)
                .relationLabel(socialPresentationHelper.relationLabel(relationStatus))
                .relationBadgeClass(socialPresentationHelper.relationBadgeClass(relationStatus))
                .blockedByCurrentUser(blockedByCurrentUser)
                .build();
    }

    public CircleContactListResponseDTO buildCircleContactListResponse(List<CircleContactDTO> items, int page, int size) {
        return PageResponseFactory.fromItems(items, page, size, pageWindow -> CircleContactListResponseDTO.builder()
                .items(pageWindow.items())
                .page(pageWindow.page())
                .size(pageWindow.size())
                .totalItems(pageWindow.totalItems())
                .totalPages(pageWindow.totalPages())
                .build());
    }

    public CircleRequestListResponseDTO buildCircleRequestListResponse(List<CircleRequestResponseDTO> items, int page, int size) {
        return PageResponseFactory.fromItems(items, page, size, pageWindow -> CircleRequestListResponseDTO.builder()
                .items(pageWindow.items())
                .page(pageWindow.page())
                .size(pageWindow.size())
                .totalItems(pageWindow.totalItems())
                .totalPages(pageWindow.totalPages())
                .build());
    }

    public CircleSearchResultListResponseDTO buildCircleSearchResultListResponse(List<CircleSearchResultDTO> items, int page, int size) {
        return PageResponseFactory.fromItems(items, page, size, pageWindow -> CircleSearchResultListResponseDTO.builder()
                .items(pageWindow.items())
                .page(pageWindow.page())
                .size(pageWindow.size())
                .totalItems(pageWindow.totalItems())
                .totalPages(pageWindow.totalPages())
                .build());
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

    public boolean matchesConnectionFilter(CircleContactDTO connection, Long circleId, boolean unassigned) {
        List<Long> circleIds = connection.getCircleIds();
        if (circleId != null) {
            return circleIds.contains(circleId);
        }
        if (unassigned) {
            return circleIds.isEmpty();
        }
        return true;
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

    public String normalizeSearchQuery(String query) {
        return SearchQueryNormalizer.normalize(query).toLowerCase();
    }

    public CircleRelationStatus resolveRelationStatus(Optional<CircleRequest> relation, Long currentUserId) {
        return relation
                .map(circleRequest -> {
                    if (circleRequest.getBlockedAt() != null) {
                        return CircleRelationStatus.BLOCKED;
                    }
                    if (circleRequest.getAcceptedAt() != null) {
                        return CircleRelationStatus.CIRCLE;
                    }
                    if (Objects.equals(circleRequest.getRequester().getId(), currentUserId)) {
                        return CircleRelationStatus.OUTGOING_REQUEST;
                    }
                    return CircleRelationStatus.INCOMING_REQUEST;
                })
                .orElse(CircleRelationStatus.NONE);
    }

    public boolean isBlockedByCurrentUser(Optional<CircleRequest> relation, Long currentUserId) {
        return relation
                .map(circleRequest -> circleRequest.getBlockedAt() != null
                        && circleRequest.getBlockedBy() != null
                        && Objects.equals(circleRequest.getBlockedBy().getId(), currentUserId))
                .orElse(false);
    }

    private CircleMemberDTO toCircleMemberDto(AppUser member) {
        return CircleMemberDTO.builder()
                .userId(member.getId())
                .username(member.getUsername())
                .profileDescription(RichTextInputValidator.sanitize(member.getProfileDescription()))
                .profileAvatarDataUrl(member.getProfileAvatarDataUrl())
                .build();
    }

    private boolean containsNormalized(String normalizedQuery, String... values) {
        return normalizedHaystack(values).contains(normalizedQuery);
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
