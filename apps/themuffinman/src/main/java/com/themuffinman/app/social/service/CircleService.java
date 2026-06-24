package com.themuffinman.app.social.service;

import com.themuffinman.app.social.dto.CircleRequestCreateDTO;
import com.themuffinman.app.social.dto.CircleBlockCreateDTO;
import com.themuffinman.app.social.dto.CircleRelationDTO;
import com.themuffinman.app.social.dto.CircleRelationStatus;
import com.themuffinman.app.social.dto.CircleOverviewDTO;
import com.themuffinman.app.social.dto.CircleContactDTO;
import com.themuffinman.app.social.dto.CircleContactListResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestListResponseDTO;
import com.themuffinman.app.social.dto.CircleSearchResultDTO;
import com.themuffinman.app.social.dto.CircleSearchResultListResponseDTO;
import com.themuffinman.app.social.dto.AdminCircleOverviewDTO;
import com.themuffinman.app.social.dto.AdminCircleGroupResponseDTO;
import com.themuffinman.app.social.dto.ConnectionCircleUpdateDTO;
import com.themuffinman.app.social.dto.CircleGroupRequestDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.mapper.CircleRequestMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.social.model.CircleRequest;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.model.CircleMembership;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.social.repository.CircleRequestRepository;
import com.themuffinman.app.social.repository.CircleGroupRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CircleService {
    private final CircleRequestRepository circleRequestRepository;
    private final AppUserRepository appUserRepository;
    private final AppUserLookupService appUserLookupService;
    private final CircleGroupRepository circleGroupRepository;
    private final CircleMembershipService circleMembershipService;
    private final CircleRelationService circleRelationService;
    private final CircleRequestMgr circleRequestMgr;
    private final CircleViewAssembler circleViewAssembler;

    public List<CircleRequestResponseDTO> getMyCircles(AppUser currentUser) {
        return circleRequestRepository.findAcceptedByUserId(currentUser.getId())
                .stream()
                .map(circleRequestMgr::toDto)
                .toList();
    }

    public AdminCircleOverviewDTO getAdminOverview(AppUser currentUser, String query) {
        validateAdmin(currentUser);
        return circleViewAssembler.buildAdminOverview(
                getAllCirclesForAdmin(),
                circleRequestRepository.findAllDetailed(),
                query
        );
    }

    public CircleOverviewDTO getOverview(AppUser currentUser) {
        List<CircleContactDTO> connections = loadConnections(currentUser);
        List<CircleRequestResponseDTO> incomingRequests = loadIncomingRequests(currentUser);
        List<CircleRequestResponseDTO> outgoingRequests = loadOutgoingRequests(currentUser);
        return circleViewAssembler.buildOverview(connections, incomingRequests, outgoingRequests);
    }

    public List<CircleGroupResponseDTO> getCircles(AppUser currentUser) {
        return circleGroupRepository.findAllDetailedByOwnerId(currentUser.getId()).stream()
                .map(circleViewAssembler::toCircleDto)
                .toList();
    }

    public List<AdminCircleGroupResponseDTO> getAllCirclesForAdmin() {
        return circleGroupRepository.findAllDetailed().stream()
                .map(circleViewAssembler::toAdminCircleDto)
                .sorted(Comparator.comparing(AdminCircleGroupResponseDTO::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<CircleContactDTO> getConnections(AppUser currentUser) {
        return loadConnections(currentUser);
    }

    public CircleContactListResponseDTO getConnections(AppUser currentUser, String query, Long circleId, boolean unassigned, int page, int size) {
        List<CircleContactDTO> connections = loadConnections(currentUser).stream()
                .filter(connection -> circleViewAssembler.matchesConnectionQuery(connection, query))
                .filter(connection -> circleViewAssembler.matchesConnectionFilter(connection, circleId, unassigned))
                .toList();
        return circleViewAssembler.buildCircleContactListResponse(connections, page, size);
    }

    private List<CircleContactDTO> loadConnections(AppUser currentUser) {
        Map<Long, List<CircleMembership>> membershipsByUserId = circleMembershipService.getMembershipsByUserIdForOwner(currentUser.getId());

        return circleRequestRepository.findAcceptedByUserId(currentUser.getId()).stream()
                .map(relation -> circleViewAssembler.toContact(currentUser, relation, membershipsByUserId))
                .toList();
    }

    public List<CircleRequestResponseDTO> getIncomingRequests(AppUser currentUser) {
        return loadIncomingRequests(currentUser);
    }

    public CircleRequestListResponseDTO getIncomingRequests(AppUser currentUser, String query, int page, int size) {
        List<CircleRequestResponseDTO> requests = loadIncomingRequests(currentUser).stream()
                .filter(request -> circleViewAssembler.matchesRequestQuery(request.getCounterpartUsername(), request.getCounterpartProfileDescription(), query))
                .toList();
        return circleViewAssembler.buildCircleRequestListResponse(requests, page, size);
    }

    private List<CircleRequestResponseDTO> loadIncomingRequests(AppUser currentUser) {
        return circleRequestRepository.findIncomingPendingByRecipientId(currentUser.getId())
                .stream()
                .map(circleRequestMgr::toDto)
                .map(request -> circleViewAssembler.toViewerRequest(request, true))
                .toList();
    }

    public List<CircleRequestResponseDTO> getOutgoingRequests(AppUser currentUser) {
        return loadOutgoingRequests(currentUser);
    }

    public CircleRequestListResponseDTO getOutgoingRequests(AppUser currentUser, String query, int page, int size) {
        List<CircleRequestResponseDTO> requests = loadOutgoingRequests(currentUser).stream()
                .filter(request -> circleViewAssembler.matchesRequestQuery(request.getCounterpartUsername(), request.getCounterpartProfileDescription(), query))
                .toList();
        return circleViewAssembler.buildCircleRequestListResponse(requests, page, size);
    }

    private List<CircleRequestResponseDTO> loadOutgoingRequests(AppUser currentUser) {
        return circleRequestRepository.findOutgoingPendingByRequesterId(currentUser.getId())
                .stream()
                .map(circleRequestMgr::toDto)
                .map(request -> circleViewAssembler.toViewerRequest(request, false))
                .toList();
    }

    public List<CircleSearchResultDTO> getInviteCandidates(AppUser currentUser) {
        return getInviteCandidatesPage(currentUser, 0, 12).getItems();
    }

    public CircleSearchResultListResponseDTO searchCircleUsers(AppUser currentUser, String query, int page, int size) {
        String normalizedQuery = circleViewAssembler.normalizeSearchQuery(query);
        if (normalizedQuery.length() < 2) {
            return circleViewAssembler.buildCircleSearchResultListResponse(List.of(), page, size);
        }

        List<CircleSearchResultDTO> results = appUserRepository.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(currentUser.getId()))
                .map(candidate -> circleViewAssembler.toSearchResult(currentUser, candidate, findRelation(currentUser, candidate)))
                .filter(candidate -> circleViewAssembler.matchesCandidateQuery(candidate, normalizedQuery))
                .sorted(Comparator.comparing(CircleSearchResultDTO::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return circleViewAssembler.buildCircleSearchResultListResponse(results, page, size);
    }

    public CircleSearchResultListResponseDTO getInviteCandidatesPage(AppUser currentUser, int page, int size) {
        List<CircleSearchResultDTO> results = appUserRepository.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(currentUser.getId()))
                .map(candidate -> circleViewAssembler.toSearchResult(currentUser, candidate, findRelation(currentUser, candidate)))
                .filter(candidate -> candidate.getRelationStatus() == CircleRelationStatus.NONE)
                .sorted(Comparator.comparing(CircleSearchResultDTO::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return circleViewAssembler.buildCircleSearchResultListResponse(results, page, size);
    }

    public CircleRelationDTO getRelationWithUser(AppUser currentUser, Long otherUserId) {
        if (currentUser == null || otherUserId == null) {
            return circleViewAssembler.toRelationDto(CircleRelationStatus.NONE, false);
        }

        AppUser otherUser = appUserLookupService.requireById(otherUserId);
        Optional<CircleRequest> relation = findRelation(currentUser, otherUser);
        CircleRelationStatus relationStatus = circleViewAssembler.resolveRelationStatus(relation, currentUser.getId());
        return circleViewAssembler.toRelationDto(relationStatus, circleViewAssembler.isBlockedByCurrentUser(relation, currentUser.getId()));
    }

    public List<CircleSearchResultDTO> searchCircleUsers(AppUser currentUser, String query) {
        return searchCircleUsers(currentUser, query, 0, Integer.MAX_VALUE).getItems();
    }

    public CircleRequestResponseDTO createCircleRequest(CircleRequestCreateDTO dto, AppUser currentUser) {
        return circleRelationService.createCircleRequest(dto, currentUser);
    }

    public CircleRequestResponseDTO acceptCircleRequest(Long requestId, AppUser currentUser) {
        return circleRelationService.acceptCircleRequest(requestId, currentUser);
    }

    public void deleteCircleRequest(Long requestId, AppUser currentUser) {
        circleRelationService.deleteCircleRequest(requestId, currentUser);
    }

    public CircleRequestResponseDTO blockCircleUser(CircleBlockCreateDTO dto, AppUser currentUser) {
        return circleRelationService.blockCircleUser(dto, currentUser);
    }

    public void unblockCircleUser(Long blockedUserId, AppUser currentUser) {
        circleRelationService.unblockCircleUser(blockedUserId, currentUser);
    }

    public boolean isCircleBetween(AppUser leftUser, AppUser rightUser) {
        return circleRelationService.isCircleBetween(leftUser, rightUser);
    }

    public boolean isCircleMember(Long circleId, Long memberUserId) {
        return circleMembershipService.isCircleMember(circleId, memberUserId);
    }

    public List<CircleGroup> getOwnedCirclesByIds(AppUser owner, List<Long> circleIds) {
        return circleMembershipService.getOwnedCirclesByIds(owner, circleIds);
    }

    public CircleGroupResponseDTO createCircle(CircleGroupRequestDTO dto, AppUser currentUser) {
        String normalizedName = normalizeCircleName(dto.getName());
        if (circleGroupRepository.existsByOwnerIdAndNameIgnoreCase(currentUser.getId(), normalizedName)) {
            throw ServiceErrors.conflict("You already have a circle with this name");
        }

        CircleGroup circle = new CircleGroup();
        circle.setOwner(currentUser);
        circle.setName(normalizedName);
        return circleViewAssembler.toCircleDto(circleGroupRepository.save(circle));
    }

    public CircleGroupResponseDTO updateCircle(Long circleId, CircleGroupRequestDTO dto, AppUser currentUser) {
        CircleGroup circle = requireCircleOwnedByCurrentUser(circleId, currentUser);
        String normalizedName = normalizeCircleName(dto.getName());

        if (!circle.getName().equalsIgnoreCase(normalizedName)
                && circleGroupRepository.existsByOwnerIdAndNameIgnoreCase(currentUser.getId(), normalizedName)) {
            throw ServiceErrors.conflict("You already have a circle with this name");
        }

        circle.setName(normalizedName);
        return circleViewAssembler.toCircleDto(circleGroupRepository.save(circle));
    }

    public void deleteCircle(Long circleId, AppUser currentUser) {
        CircleGroup circle = requireCircleOwnedByCurrentUser(circleId, currentUser);
        circleGroupRepository.delete(circle);
    }

    public void deleteCircleAsAdmin(Long circleId, AppUser currentUser) {
        validateAdmin(currentUser);

        CircleGroup circle = circleGroupRepository.findById(circleId)
                .orElseThrow(() -> ServiceErrors.notFound("Circle not found with id " + circleId));
        circleGroupRepository.delete(circle);
    }

    public CircleContactDTO updateConnectionCircles(Long userId, ConnectionCircleUpdateDTO dto, AppUser currentUser) {
        AppUser contact = appUserLookupService.requireById(userId);
        if (!isCircleBetween(currentUser, contact)) {
            throw ServiceErrors.badRequest("You can only organize connected users into circles");
        }

        circleMembershipService.syncConnectionCircles(currentUser, contact, dto.getCircleIds());

        CircleRequest relation = findRelation(currentUser, contact)
                .orElseThrow(() -> ServiceErrors.notFound("Connection not found"));
        Map<Long, List<CircleMembership>> membershipsByUserId = circleMembershipService.getMembershipsForContact(contact.getId(), currentUser.getId())
                .stream()
                .collect(Collectors.groupingBy(membership -> membership.getMember().getId()));
        return circleViewAssembler.toContact(currentUser, relation, membershipsByUserId);
    }

    private Optional<CircleRequest> findRelation(AppUser leftUser, AppUser rightUser) {
        return circleRequestRepository.findBetweenUsers(leftUser.getId(), rightUser.getId());
    }

    private CircleGroup requireCircleOwnedByCurrentUser(Long circleId, AppUser currentUser) {
        return circleGroupRepository.findByIdAndOwnerId(circleId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Circle not found with id " + circleId));
    }

    private String normalizeCircleName(String name) {
        if (name == null) {
            throw ServiceErrors.badRequest("Circle name is required");
        }

        String normalized = name.trim();
        if (normalized.isBlank()) {
            throw ServiceErrors.badRequest("Circle name is required");
        }

        return normalized;
    }

    private void validateAdmin(AppUser currentUser) {
        if (currentUser == null || currentUser.getRole() != AppUserRole.ADMIN) {
            throw ServiceErrors.forbidden("Admin access is required");
        }
    }

}
