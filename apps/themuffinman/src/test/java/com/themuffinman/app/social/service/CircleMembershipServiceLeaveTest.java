package com.themuffinman.app.social.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.model.CircleMembership;
import com.themuffinman.app.social.repository.CircleGroupRepository;
import com.themuffinman.app.social.repository.CircleMembershipRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CircleMembershipServiceLeaveTest {
    @Test
    void removesOnlyAuthenticatedMemberMembership() {
        CircleGroupRepository circles = mock(CircleGroupRepository.class);
        CircleMembershipRepository memberships = mock(CircleMembershipRepository.class);
        CircleMembershipService service = new CircleMembershipService(circles, memberships);
        AppUser owner = new AppUser(); owner.setId(1L);
        AppUser member = new AppUser(); member.setId(2L);
        CircleGroup circle = new CircleGroup(); circle.setId(7L); circle.setOwner(owner);
        CircleMembership membership = new CircleMembership(); membership.setCircle(circle); membership.setMember(member);
        when(circles.findById(7L)).thenReturn(Optional.of(circle));
        when(memberships.findByCircleIdAndMemberId(7L, 2L)).thenReturn(Optional.of(membership));
        service.leaveCircle(7L, member);
        verify(memberships).delete(eq(membership));
    }

    @Test
    void preventsCircleOwnerFromLeavingOwnCircle() {
        CircleGroupRepository circles = mock(CircleGroupRepository.class);
        CircleMembershipRepository memberships = mock(CircleMembershipRepository.class);
        CircleMembershipService service = new CircleMembershipService(circles, memberships);
        AppUser owner = new AppUser(); owner.setId(1L);
        CircleGroup circle = new CircleGroup(); circle.setId(7L); circle.setOwner(owner);
        when(circles.findById(7L)).thenReturn(Optional.of(circle));
        assertThrows(RuntimeException.class, () -> service.leaveCircle(7L, owner));
        verifyNoInteractions(memberships);
    }
}
