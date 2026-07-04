package com.themuffinman.app.social.service;

import com.themuffinman.app.common.pagination.PageResponseFactory;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.dto.AdminCircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleContactDTO;
import com.themuffinman.app.social.dto.CircleContactListResponseDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleMemberDTO;
import com.themuffinman.app.social.dto.CircleOverviewDTO;
import com.themuffinman.app.social.dto.CircleRelationDTO;
import com.themuffinman.app.social.dto.CircleRelationStatusDTO;
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
                .resolutionKey("circle:" + circle.getId())
                .resolutionLabel(circle.getName())
                .exactResolutionEligible(true)
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
        SocialRelationActionHelper.SearchActions actions = socialRelationActionHelper.requestActions(incoming);

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
        request.setResolutionKey("circle-request:" + request.getId());
        request.setResolutionLabel(request.getCounterpartUsername());
        request.setExactResolutionEligible(request.getId() != null && request.getCounterpartUserId() != null);
        request.setPrimaryAction(actions.getPrimaryAction());
        request.setSecondaryAction(actions.getSecondaryAction());
        return request;
    }

    public CircleSearchResultDTO toSearchResult(AppUser currentUser, AppUser candidate, Optional<CircleRequest> relation) {
        CircleRelationStatusDTO relationStatus = resolveRelationStatus(relation, currentUser.getId());
        boolean blockedByCurrentUser = isBlockedByCurrentUser(relation, currentUser.getId());
        SocialRelationActionHelper.SearchActions actions = socialRelationActionHelper.searchActions(relationStatus, blockedByCurrentUser);

        return CircleSearchResultDTO.builder()
                .id(candidate.getId())
                .username(candidate.getUsername())
                .resolutionKey("user:" + candidate.getId())
                .resolutionLabel(candidate.getUsername() + " <" + candidate.getEmail() + ">")
                .exactResolutionEligible(true)
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

    public CircleRelationDTO toRelationDto(CircleRelationStatusDTO relationStatus, boolean blockedByCurrentUser) {
        return CircleRelationDTO.builder()
                .relationStatus(relationStatus)
                .relationLabel(socialPresentationHelper.relationLabel(relationStatus))
                .relationBadgeClass(socialPresentationHelper.relationBadgeClass(relationStatus))
                .blockedByCurrentUser(blockedByCurrentUser)
                .exactResolutionEligible(true)
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

    public CircleRelationStatusDTO resolveRelationStatus(Optional<CircleRequest> relation, Long currentUserId) {
        return relation
                .map(circleRequest -> {
                    if (circleRequest.getBlockedAt() != null) {
                        return CircleRelationStatusDTO.BLOCKED;
                    }
                    if (circleRequest.getAcceptedAt() != null) {
                        return CircleRelationStatusDTO.CIRCLE;
                    }
                    if (Objects.equals(circleRequest.getRequester().getId(), currentUserId)) {
                        return CircleRelationStatusDTO.OUTGOING_REQUEST;
                    }
                    return CircleRelationStatusDTO.INCOMING_REQUEST;
                })
                .orElse(CircleRelationStatusDTO.NONE);
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
}
