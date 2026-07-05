package com.themuffinman.app.social.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.social.dto.AdminCircleGroupResponseDTO;
import com.themuffinman.app.social.dto.AdminCircleOverviewDTO;
import com.themuffinman.app.social.dto.CircleContactDTO;
import com.themuffinman.app.social.dto.CircleContactListResponseDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleOverviewDTO;
import com.themuffinman.app.social.dto.CircleRelationDTO;
import com.themuffinman.app.social.dto.CircleRelationStatusDTO;
import com.themuffinman.app.social.dto.CircleRequestListResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.dto.CircleSearchResultDTO;
import com.themuffinman.app.social.dto.CircleSearchResultListResponseDTO;
import com.themuffinman.app.social.mapper.CircleRequestMgr;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.model.CircleMembership;
import com.themuffinman.app.social.model.CircleRequest;
import com.themuffinman.app.social.repository.CircleGroupRepository;
import com.themuffinman.app.social.repository.CircleRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CircleReadService {

    private final CircleRequestRepository circleRequestRepository;
    private final AppUserRepository appUserRepository;
    private final AppUserLookupService appUserLookupService;
    private final CircleGroupRepository circleGroupRepository;
    private final CircleMembershipService circleMembershipService;
    private final CircleRelationService circleRelationService;
    private final CircleRequestMgr circleRequestMgr;
    private final CircleAdminOverviewAssembler circleAdminOverviewAssembler;
    private final CircleViewAssembler circleViewAssembler;
    private final CircleSearchQueryService circleSearchQueryService;
    private final CircleDiscoveryService circleDiscoveryService;

    @Transactional(readOnly = true)
    public List<CircleRequestResponseDTO> getMyCircles(AppUser currentUser) {
        return circleRequestRepository.findAcceptedByUserId(currentUser.getId())
                .stream()
                .map(circleRequestMgr::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminCircleOverviewDTO getAdminOverview(AppUser currentUser, String query) {
        validateAdmin(currentUser);
        return circleAdminOverviewAssembler.buildAdminOverview(
                getAllCirclesForAdmin(),
                circleRequestRepository.findAllDetailed(),
                query
        );
    }

    @Transactional(readOnly = true)
    public CircleOverviewDTO getOverview(AppUser currentUser) {
        List<CircleContactDTO> connections = loadConnections(currentUser);
        List<CircleRequestResponseDTO> incomingRequests = loadIncomingRequests(currentUser);
        List<CircleRequestResponseDTO> outgoingRequests = loadOutgoingRequests(currentUser);
        return circleViewAssembler.buildOverview(connections, incomingRequests, outgoingRequests);
    }

    @Transactional(readOnly = true)
    public List<CircleGroupResponseDTO> getCircles(AppUser currentUser) {
        return circleGroupRepository.findAllDetailedByOwnerId(currentUser.getId()).stream()
                .map(circleViewAssembler::toCircleDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CircleGroupResponseDTO> getCirclesForUserAsAdmin(Long userId, AppUser currentUser) {
        validateAdmin(currentUser);
        AppUser targetUser = appUserLookupService.requireById(userId);
        return circleGroupRepository.findAllDetailedByOwnerId(targetUser.getId()).stream()
                .map(circleViewAssembler::toCircleDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminCircleGroupResponseDTO> getAllCirclesForAdmin() {
        return circleGroupRepository.findAllDetailed().stream()
                .map(circleViewAssembler::toAdminCircleDto)
                .sorted(Comparator.comparing(AdminCircleGroupResponseDTO::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CircleContactDTO> getConnections(AppUser currentUser) {
        return loadConnections(currentUser);
    }

    @Transactional(readOnly = true)
    public CircleContactListResponseDTO getConnections(AppUser currentUser, String query, Long circleId, boolean unassigned, int page, int size) {
        List<CircleContactDTO> connections = loadConnections(currentUser).stream()
                .filter(connection -> circleSearchQueryService.matchesConnectionQuery(connection, query))
                .filter(connection -> circleViewAssembler.matchesConnectionFilter(connection, circleId, unassigned))
                .toList();
        return circleViewAssembler.buildCircleContactListResponse(connections, page, size);
    }

    @Transactional(readOnly = true)
    public List<CircleContactDTO> getConnectionsForUserAsAdmin(Long userId, AppUser currentUser) {
        validateAdmin(currentUser);
        return loadConnections(appUserLookupService.requireById(userId));
    }

    @Transactional(readOnly = true)
    public List<CircleRequestResponseDTO> getIncomingRequests(AppUser currentUser) {
        return loadIncomingRequests(currentUser);
    }

    @Transactional(readOnly = true)
    public CircleRequestListResponseDTO getIncomingRequests(AppUser currentUser, String query, int page, int size) {
        List<CircleRequestResponseDTO> requests = loadIncomingRequests(currentUser).stream()
                .filter(request -> circleSearchQueryService.matchesRequestQuery(request.getCounterpartUsername(), request.getCounterpartProfileDescription(), query))
                .toList();
        return circleViewAssembler.buildCircleRequestListResponse(requests, page, size);
    }

    @Transactional(readOnly = true)
    public List<CircleRequestResponseDTO> getOutgoingRequests(AppUser currentUser) {
        return loadOutgoingRequests(currentUser);
    }

    @Transactional(readOnly = true)
    public CircleRequestListResponseDTO getOutgoingRequests(AppUser currentUser, String query, int page, int size) {
        List<CircleRequestResponseDTO> requests = loadOutgoingRequests(currentUser).stream()
                .filter(request -> circleSearchQueryService.matchesRequestQuery(request.getCounterpartUsername(), request.getCounterpartProfileDescription(), query))
                .toList();
        return circleViewAssembler.buildCircleRequestListResponse(requests, page, size);
    }

    @Transactional(readOnly = true)
    public boolean isCircleBetween(AppUser leftUser, AppUser rightUser) {
        if (leftUser == null || rightUser == null) {
            return false;
        }

        return circleRelationService.isCircleBetween(leftUser, rightUser);
    }

    @Transactional(readOnly = true)
    public boolean isCircleMember(Long circleId, Long memberUserId) {
        return circleMembershipService.isCircleMember(circleId, memberUserId);
    }

    @Transactional(readOnly = true)
    public List<CircleGroup> getOwnedCirclesByIds(AppUser owner, List<Long> circleIds) {
        return circleMembershipService.getOwnedCirclesByIds(owner, circleIds);
    }

    @Transactional(readOnly = true)
    public Optional<CircleRequest> findRelation(AppUser leftUser, AppUser rightUser) {
        return circleRelationService.findRelation(leftUser, rightUser);
    }

    @Transactional(readOnly = true)
    public CircleRelationDTO getRelationWithUser(AppUser currentUser, Long otherUserId) {
        return circleDiscoveryService.getRelationWithUser(currentUser, otherUserId);
    }

    private List<CircleContactDTO> loadConnections(AppUser currentUser) {
        Map<Long, List<CircleMembership>> membershipsByUserId = circleMembershipService.getMembershipsByUserIdForOwner(currentUser.getId());

        return circleRequestRepository.findAcceptedByUserId(currentUser.getId()).stream()
                .map(relation -> circleViewAssembler.toContact(currentUser, relation, membershipsByUserId))
                .toList();
    }

    private List<CircleRequestResponseDTO> loadIncomingRequests(AppUser currentUser) {
        return circleRequestRepository.findIncomingPendingByRecipientId(currentUser.getId())
                .stream()
                .map(circleRequestMgr::toDto)
                .map(request -> circleViewAssembler.toViewerRequest(request, true))
                .toList();
    }

    private List<CircleRequestResponseDTO> loadOutgoingRequests(AppUser currentUser) {
        return circleRequestRepository.findOutgoingPendingByRequesterId(currentUser.getId())
                .stream()
                .map(circleRequestMgr::toDto)
                .map(request -> circleViewAssembler.toViewerRequest(request, false))
                .toList();
    }

    private void validateAdmin(AppUser currentUser) {
        if (currentUser == null || currentUser.getRole() != AppUserRole.ADMIN) {
            throw ServiceErrors.forbidden("Admin access is required");
        }
    }
}
