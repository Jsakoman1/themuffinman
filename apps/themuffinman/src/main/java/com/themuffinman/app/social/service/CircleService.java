package com.themuffinman.app.social.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.social.dto.BulkCircleMembershipActionDTO;
import com.themuffinman.app.social.dto.BulkCircleMembershipUpdateDTO;
import com.themuffinman.app.social.dto.CircleBlockCreateDTO;
import com.themuffinman.app.social.dto.CircleContactDTO;
import com.themuffinman.app.social.dto.CircleContactListResponseDTO;
import com.themuffinman.app.social.dto.CircleGroupRequestDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleOverviewDTO;
import com.themuffinman.app.social.dto.CircleRequestCreateDTO;
import com.themuffinman.app.social.dto.CircleRequestListResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.dto.CircleSearchResultDTO;
import com.themuffinman.app.social.dto.CircleSearchResultListResponseDTO;
import com.themuffinman.app.social.dto.CircleRelationDTO;
import com.themuffinman.app.social.dto.ConnectionCircleUpdateDTO;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.model.CircleMembership;
import com.themuffinman.app.social.model.CircleRequest;
import com.themuffinman.app.social.repository.CircleGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CircleService {
    private final AppUserLookupService appUserLookupService;
    private final CircleGroupRepository circleGroupRepository;
    private final CircleMembershipService circleMembershipService;
    private final CircleRelationService circleRelationService;
    private final CircleReadService circleReadService;
    private final CircleDiscoveryService circleDiscoveryService;
    private final CircleViewAssembler circleViewAssembler;

    public CircleRequestResponseDTO createCircleRequest(CircleRequestCreateDTO dto, AppUser currentUser) {
        return circleRelationService.createCircleRequest(dto, currentUser);
    }

    public CircleRequestResponseDTO acceptCircleRequest(Long requestId, AppUser currentUser) {
        return circleRelationService.acceptCircleRequest(requestId, currentUser);
    }

    public void deleteCircleRequest(Long requestId, AppUser currentUser) {
        circleRelationService.deleteCircleRequest(requestId, currentUser);
    }

    @Transactional(readOnly = true)
    public CircleOverviewDTO getOverview(AppUser currentUser) {
        return circleReadService.getOverview(currentUser);
    }

    @Transactional(readOnly = true)
    public List<CircleGroupResponseDTO> getCircles(AppUser currentUser) {
        return circleReadService.getCircles(currentUser);
    }

    @Transactional(readOnly = true)
    public List<CircleContactDTO> getConnections(AppUser currentUser) {
        return circleReadService.getConnections(currentUser);
    }

    @Transactional(readOnly = true)
    public CircleContactListResponseDTO getConnections(AppUser currentUser, String query, Long circleId, boolean unassigned, int page, int size) {
        return circleReadService.getConnections(currentUser, query, circleId, unassigned, page, size);
    }

    @Transactional(readOnly = true)
    public List<CircleRequestResponseDTO> getIncomingRequests(AppUser currentUser) {
        return circleReadService.getIncomingRequests(currentUser);
    }

    @Transactional(readOnly = true)
    public CircleRequestListResponseDTO getIncomingRequests(AppUser currentUser, String query, int page, int size) {
        return circleReadService.getIncomingRequests(currentUser, query, page, size);
    }

    @Transactional(readOnly = true)
    public List<CircleRequestResponseDTO> getOutgoingRequests(AppUser currentUser) {
        return circleReadService.getOutgoingRequests(currentUser);
    }

    @Transactional(readOnly = true)
    public CircleRequestListResponseDTO getOutgoingRequests(AppUser currentUser, String query, int page, int size) {
        return circleReadService.getOutgoingRequests(currentUser, query, page, size);
    }

    @Transactional(readOnly = true)
    public List<CircleSearchResultDTO> getInviteCandidates(AppUser currentUser) {
        return circleDiscoveryService.getInviteCandidates(currentUser);
    }

    @Transactional(readOnly = true)
    public CircleSearchResultListResponseDTO getInviteCandidatesPage(AppUser currentUser, int page, int size) {
        return circleDiscoveryService.getInviteCandidatesPage(currentUser, page, size);
    }

    @Transactional(readOnly = true)
    public CircleRelationDTO getRelationWithUser(AppUser currentUser, Long otherUserId) {
        return circleReadService.getRelationWithUser(currentUser, otherUserId);
    }

    public CircleRequestResponseDTO blockCircleUser(CircleBlockCreateDTO dto, AppUser currentUser) {
        return circleRelationService.blockCircleUser(dto, currentUser);
    }

    public void unblockCircleUser(Long blockedUserId, AppUser currentUser) {
        circleRelationService.unblockCircleUser(blockedUserId, currentUser);
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
        if (!circleReadService.isCircleBetween(currentUser, contact)) {
            throw ServiceErrors.badRequest("You can only organize connected users into circles");
        }

        circleMembershipService.syncConnectionCircles(currentUser, contact, dto.getCircleIds());

        CircleRequest relation = circleReadService.findRelation(currentUser, contact)
                .orElseThrow(() -> ServiceErrors.notFound("Connection not found"));
        Map<Long, List<CircleMembership>> membershipsByUserId = circleMembershipService.getMembershipsForContact(contact.getId(), currentUser.getId())
                .stream()
                .collect(Collectors.groupingBy(membership -> membership.getMember().getId()));
        return circleViewAssembler.toContact(currentUser, relation, membershipsByUserId);
    }

    public void updateConnectionCirclesBulk(BulkCircleMembershipUpdateDTO dto, AppUser currentUser) {
        CircleGroup circle = requireCircleOwnedByCurrentUser(dto.getCircleId(), currentUser);

        for (Long userId : dto.getUserIds()) {
            AppUser contact = appUserLookupService.requireById(userId);
            if (!circleReadService.isCircleBetween(currentUser, contact)) {
                throw ServiceErrors.badRequest("You can only organize connected users into circles");
            }

            List<Long> nextCircleIds = buildBulkCircleUpdate(dto.getAction(), circle.getId(), currentUser, contact);
            circleMembershipService.syncConnectionCircles(currentUser, contact, nextCircleIds);
        }
    }

    private List<Long> buildBulkCircleUpdate(BulkCircleMembershipActionDTO action, Long circleId, AppUser currentUser, AppUser contact) {
        List<Long> currentCircleIds = circleMembershipService.getMembershipsForContact(contact.getId(), currentUser.getId()).stream()
                .map(membership -> membership.getCircle().getId())
                .toList();
        java.util.LinkedHashSet<Long> nextCircleIds = new java.util.LinkedHashSet<>(currentCircleIds);

        if (action == BulkCircleMembershipActionDTO.ADD) {
            nextCircleIds.add(circleId);
        } else {
            nextCircleIds.remove(circleId);
        }

        return List.copyOf(nextCircleIds);
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
