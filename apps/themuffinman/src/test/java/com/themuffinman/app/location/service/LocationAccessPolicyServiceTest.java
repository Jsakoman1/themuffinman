package com.themuffinman.app.location.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.model.ExactLocationVisibilityScope;
import com.themuffinman.app.location.model.UserLocationMode;
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

    @Test
    void describesHiddenAndApproximateLocationSummaries() {
        AppUser hidden = TestFixtures.user(1L, "hidden");
        hidden.setLocationMode(UserLocationMode.OFF);
        AppUser approximate = TestFixtures.user(2L, "approximate");
        approximate.setLocationMode(UserLocationMode.APPROXIMATE);

        assertThat(policyService.describeUserLocationSharingSummary(hidden)).isEqualTo("Hidden");
        assertThat(policyService.describeUserLocationVisibilitySummary(hidden)).isEqualTo("Hidden");
        assertThat(policyService.describeUserLocationSharingSummary(approximate)).isEqualTo("Approximate area only");
        assertThat(policyService.describeUserLocationVisibilitySummary(approximate)).isEqualTo("Approximate area only");
    }

    @Test
    void describesExactVisibilityScopeUsingReadableLabels() {
        AppUser user = TestFixtures.user(1L, "owner");
        user.setLocationMode(UserLocationMode.EXACT);

        user.setExactLocationVisibilityScope(ExactLocationVisibilityScope.NOBODY);
        assertThat(policyService.describeUserLocationVisibilitySummary(user)).isEqualTo("Private");

        user.setExactLocationVisibilityScope(ExactLocationVisibilityScope.EVERYONE);
        assertThat(policyService.describeUserLocationVisibilitySummary(user)).isEqualTo("Visible to everyone");

        user.setExactLocationVisibilityScope(ExactLocationVisibilityScope.CIRCLES);
        assertThat(policyService.describeUserLocationVisibilitySummary(user)).isEqualTo("Visible to circles");

        user.setExactLocationVisibilityScope(ExactLocationVisibilityScope.USERS);
        assertThat(policyService.describeUserLocationVisibilitySummary(user)).isEqualTo("Visible to selected people");
    }
}
