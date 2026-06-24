package com.themuffinman.app.social.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.model.CircleMembership;
import com.themuffinman.app.social.repository.CircleGroupRepository;
import com.themuffinman.app.social.repository.CircleMembershipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CircleMembershipServiceTest {

    @Mock
    private CircleGroupRepository circleGroupRepository;

    @Mock
    private CircleMembershipRepository circleMembershipRepository;

    @InjectMocks
    private CircleMembershipService circleMembershipService;

    @Test
    void syncConnectionCirclesSavesMissingMembership() {
        AppUser owner = createUser(1L, "owner");
        AppUser contact = createUser(2L, "contact");

        CircleGroup circle = new CircleGroup();
        circle.setId(10L);
        circle.setOwner(owner);

        when(circleGroupRepository.findAllByOwnerIdAndIdIn(1L, Set.of(10L))).thenReturn(List.of(circle));
        when(circleMembershipRepository.findByMemberIdAndCircleOwnerId(2L, 1L)).thenReturn(List.of());

        circleMembershipService.syncConnectionCircles(owner, contact, List.of(10L));

        verify(circleMembershipRepository).save(any(CircleMembership.class));
    }

    @Test
    void syncConnectionCirclesRejectsUnknownCircleIds() {
        AppUser owner = createUser(1L, "owner");
        AppUser contact = createUser(2L, "contact");

        when(circleGroupRepository.findAllByOwnerIdAndIdIn(1L, Set.of(10L))).thenReturn(List.of());

        assertThrows(ResponseStatusException.class, () ->
                circleMembershipService.syncConnectionCircles(owner, contact, List.of(10L))
        );
    }

    private AppUser createUser(Long id, String username) {
        AppUser appUser = new AppUser();
        appUser.setId(id);
        appUser.setUsername(username);
        return appUser;
    }
}
