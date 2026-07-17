package com.themuffinman.app.social.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.model.CircleMembership;
import com.themuffinman.app.social.repository.CircleGroupRepository;
import com.themuffinman.app.social.repository.CircleMembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CircleMembershipService {

    private final CircleGroupRepository circleGroupRepository;
    private final CircleMembershipRepository circleMembershipRepository;

    public CircleMembershipService(
            CircleGroupRepository circleGroupRepository,
            CircleMembershipRepository circleMembershipRepository
    ) {
        this.circleGroupRepository = circleGroupRepository;
        this.circleMembershipRepository = circleMembershipRepository;
    }

    @Transactional(readOnly = true)
    public boolean isCircleMember(Long circleId, Long memberUserId) {
        return circleMembershipRepository.existsByCircleIdAndMemberId(circleId, memberUserId);
    }

    @Transactional(readOnly = true)
    public List<CircleGroup> getOwnedCirclesByIds(AppUser owner, List<Long> circleIds) {
        if (circleIds == null || circleIds.isEmpty()) {
            return List.of();
        }

        List<CircleGroup> circles = circleGroupRepository.findAllByOwnerIdAndIdIn(owner.getId(), circleIds);
        if (circles.size() != new LinkedHashSet<>(circleIds).size()) {
            throw ServiceErrors.badRequest("One or more selected circles are invalid");
        }

        return circles;
    }

    @Transactional(readOnly = true)
    public List<CircleMembership> getMembershipsByOwner(Long ownerId) {
        return circleMembershipRepository.findByCircleOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public List<CircleMembership> getMembershipsForContact(Long contactId, Long ownerId) {
        return circleMembershipRepository.findByMemberIdAndCircleOwnerId(contactId, ownerId);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<CircleMembership>> getMembershipsByUserIdForOwner(Long ownerId) {
        return getMembershipsByOwner(ownerId).stream()
                .collect(Collectors.groupingBy(membership -> membership.getMember().getId()));
    }

    public void syncConnectionCircles(AppUser owner, AppUser contact, List<Long> circleIds) {
        Set<Long> requestedCircleIds = circleIds == null ? Set.of() : new LinkedHashSet<>(circleIds);
        List<CircleGroup> requestedCircles = requestedCircleIds.isEmpty()
                ? List.of()
                : circleGroupRepository.findAllByOwnerIdAndIdIn(owner.getId(), requestedCircleIds);

        if (requestedCircles.size() != requestedCircleIds.size()) {
            throw ServiceErrors.badRequest("One or more selected circles are invalid");
        }

        Map<Long, CircleGroup> requestedCircleById = requestedCircles.stream()
                .collect(Collectors.toMap(CircleGroup::getId, Function.identity()));

        List<CircleMembership> existingMemberships = getMembershipsForContact(contact.getId(), owner.getId());
        Map<Long, CircleMembership> existingByCircleId = existingMemberships.stream()
                .collect(Collectors.toMap(membership -> membership.getCircle().getId(), Function.identity()));

        for (CircleMembership membership : existingMemberships) {
            if (!requestedCircleIds.contains(membership.getCircle().getId())) {
                circleMembershipRepository.delete(membership);
            }
        }

        for (Long circleId : requestedCircleIds) {
            if (existingByCircleId.containsKey(circleId)) {
                continue;
            }

            CircleMembership membership = new CircleMembership();
            membership.setCircle(requestedCircleById.get(circleId));
            membership.setMember(contact);
            circleMembershipRepository.save(membership);
        }
    }

    @Transactional
    public void leaveCircle(Long circleId, AppUser member) {
        CircleGroup circle = circleGroupRepository.findById(circleId)
                .orElseThrow(() -> ServiceErrors.notFound("Circle not found"));
        if (circle.getOwner() != null && circle.getOwner().getId().equals(member.getId())) {
            throw ServiceErrors.badRequest("Circle owners cannot leave their own circle");
        }
        CircleMembership membership = circleMembershipRepository.findByCircleIdAndMemberId(circleId, member.getId())
                .orElseThrow(() -> ServiceErrors.badRequest("You are not a member of this circle"));
        circleMembershipRepository.delete(membership);
    }
}
