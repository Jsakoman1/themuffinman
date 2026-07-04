package com.themuffinman.app.location.service;

import com.themuffinman.app.common.concepts.ModuleOwnership;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.model.ExactLocationVisibilityScope;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.service.CircleMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationAccessPolicyService {

    private final CircleMembershipService circleMembershipService;

    public boolean canViewExactLocation(AppUser owner, AppUser viewer) {
        if (owner == null) {
            return false;
        }

        if (ModuleOwnership.isOwner(owner.getId(), viewer)) {
            return true;
        }

        ExactLocationVisibilityScope scope = owner.getExactLocationVisibilityScope() == null
                ? ExactLocationVisibilityScope.NOBODY
                : owner.getExactLocationVisibilityScope();

        return switch (scope) {
            case NOBODY -> false;
            case EVERYONE -> true;
            case CIRCLES -> viewer != null && owner.getExactLocationVisibleToCircles().stream()
                    .map(CircleGroup::getId)
                    .anyMatch(circleId -> circleMembershipService.isCircleMember(circleId, viewer.getId()));
            case USERS -> viewer != null && owner.getExactLocationVisibleToUsers().stream()
                    .map(AppUser::getId)
                    .anyMatch(userId -> userId.equals(viewer.getId()));
        };
    }

    public String describeUserLocationSharingSummary(AppUser user) {
        if (user == null || user.getLocationMode() == null || user.getLocationMode() == UserLocationMode.OFF) {
            return "Hidden";
        }

        if (user.getLocationMode() == UserLocationMode.APPROXIMATE) {
            return "Approximate area only";
        }

        return "Exact location enabled";
    }

    public String describeUserLocationVisibilitySummary(AppUser user) {
        if (user == null || user.getLocationMode() == null || user.getLocationMode() == UserLocationMode.OFF) {
            return "Hidden";
        }

        if (user.getLocationMode() == UserLocationMode.APPROXIMATE) {
            return "Approximate area only";
        }

        ExactLocationVisibilityScope scope = user.getExactLocationVisibilityScope() == null
                ? ExactLocationVisibilityScope.NOBODY
                : user.getExactLocationVisibilityScope();

        return switch (scope) {
            case NOBODY -> "Private";
            case EVERYONE -> "Visible to everyone";
            case CIRCLES -> "Visible to circles";
            case USERS -> "Visible to selected people";
        };
    }
}
