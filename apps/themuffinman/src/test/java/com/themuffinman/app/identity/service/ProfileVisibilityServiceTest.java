package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.ProfileFieldVisibility;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.repository.CircleMembershipRepository;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileVisibilityServiceTest {
    private final CircleMembershipRepository membershipRepository = mock(CircleMembershipRepository.class);
    private final ProfileVisibilityService service = new ProfileVisibilityService(membershipRepository);

    @Test
    void appliesPublicPrivateAndCirclePolicyAcrossViewerRelationships() {
        AppUser owner = user(1L);
        AppUser unrelated = user(2L);
        AppUser circleMember = user(3L);
        CircleGroup circle = new CircleGroup();
        circle.setId(10L);
        when(membershipRepository.existsByCircleIdAndMemberId(10L, 3L)).thenReturn(true);

        assertTrue(service.mayView(owner, null, ProfileFieldVisibility.PUBLIC, Set.of()));
        assertTrue(service.mayView(owner, owner, ProfileFieldVisibility.PRIVATE, Set.of()));
        assertFalse(service.mayView(owner, unrelated, ProfileFieldVisibility.PRIVATE, Set.of()));
        assertTrue(service.mayView(owner, circleMember, ProfileFieldVisibility.CIRCLES, Set.of(circle)));
        assertFalse(service.mayView(owner, unrelated, ProfileFieldVisibility.CIRCLES, Set.of(circle)));
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername("user-" + id);
        return user;
    }
}
