package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.ProfileFieldVisibility;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.repository.CircleMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfileVisibilityService {
    private final CircleMembershipRepository circleMembershipRepository;

    public boolean mayView(AppUser owner, AppUser viewer, ProfileFieldVisibility visibility, Set<CircleGroup> circles) {
        if (visibility == ProfileFieldVisibility.PUBLIC) {
            return true;
        }
        if (viewer == null || owner == null || owner.getId() == null || viewer.getId() == null) {
            return false;
        }
        if (owner.getId().equals(viewer.getId())) {
            return true;
        }
        return visibility == ProfileFieldVisibility.CIRCLES
                && circles != null
                && circles.stream().anyMatch(circle -> circle.getId() != null
                && circleMembershipRepository.existsByCircleIdAndMemberId(circle.getId(), viewer.getId()));
    }
}
