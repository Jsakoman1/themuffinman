package com.themuffinman.app.social.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.dto.CircleRelationDTO;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.model.CircleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CircleRelationshipReadService {

    private final CircleMembershipService circleMembershipService;
    private final CircleRelationService circleRelationService;
    private final CircleDiscoveryService circleDiscoveryService;

    public boolean isCircleBetween(AppUser leftUser, AppUser rightUser) {
        if (leftUser == null || rightUser == null) {
            return false;
        }

        return circleRelationService.isCircleBetween(leftUser, rightUser);
    }

    public boolean isCircleMember(Long circleId, Long memberUserId) {
        return circleMembershipService.isCircleMember(circleId, memberUserId);
    }

    public List<CircleGroup> getOwnedCirclesByIds(AppUser owner, List<Long> circleIds) {
        return circleMembershipService.getOwnedCirclesByIds(owner, circleIds);
    }

    public Optional<CircleRequest> findRelation(AppUser leftUser, AppUser rightUser) {
        return circleRelationService.findRelation(leftUser, rightUser);
    }

    public CircleRelationDTO getRelationWithUser(AppUser currentUser, Long otherUserId) {
        if (currentUser == null || otherUserId == null) {
            throw ServiceErrors.badRequest("A viewer and target user are required");
        }
        return circleDiscoveryService.getRelationWithUser(currentUser, otherUserId);
    }
}
