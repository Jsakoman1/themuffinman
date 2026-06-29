package com.themuffinman.app.location.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.model.ExactLocationVisibilityScope;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.service.CircleMembershipService;
import com.themuffinman.app.testing.TestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocationAccessPolicyServiceTest {

    private final CircleMembershipService circleMembershipService = mock(CircleMembershipService.class);
    private final LocationAccessPolicyService policyService = new LocationAccessPolicyService(circleMembershipService);

    @Test
    void ownerCanAlwaysViewOwnExactLocation() {
        AppUser owner = TestFixtures.user(1L, "owner");
        owner.setExactLocationVisibilityScope(ExactLocationVisibilityScope.NOBODY);

        assertThat(policyService.canViewExactLocation(owner, owner)).isTrue();
    }

    @Test
    void circlesScopeRequiresMembershipInAllowedCircle() {
        AppUser owner = TestFixtures.user(1L, "owner");
        AppUser viewer = TestFixtures.user(2L, "viewer");
        CircleGroup circle = TestFixtures.circle(30L, owner, "Close friends");
        owner.setExactLocationVisibilityScope(ExactLocationVisibilityScope.CIRCLES);
        owner.getExactLocationVisibleToCircles().add(circle);

        when(circleMembershipService.isCircleMember(circle.getId(), viewer.getId())).thenReturn(true);

        assertThat(policyService.canViewExactLocation(owner, viewer)).isTrue();
    }

    @Test
    void usersScopeRequiresExplicitViewerAllowList() {
        AppUser owner = TestFixtures.user(1L, "owner");
        AppUser allowedViewer = TestFixtures.user(2L, "allowed");
        AppUser otherViewer = TestFixtures.user(3L, "other");
        owner.setExactLocationVisibilityScope(ExactLocationVisibilityScope.USERS);
        owner.getExactLocationVisibleToUsers().add(allowedViewer);

        assertThat(policyService.canViewExactLocation(owner, allowedViewer)).isTrue();
        assertThat(policyService.canViewExactLocation(owner, otherViewer)).isFalse();
    }
}
