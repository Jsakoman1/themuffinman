package com.themuffinman.app.common.concepts;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreConceptsTest {

    @Test
    void actorIdentityCapturesAuthenticatedAdminAndSameUserChecks() {
        AppUser admin = new AppUser();
        admin.setId(7L);
        admin.setRole(AppUserRole.ADMIN);

        ActorIdentity actor = ActorIdentity.from(admin);

        assertTrue(actor.authenticated());
        assertTrue(actor.admin());
        assertTrue(actor.sameUser(7L));
        assertFalse(ActorIdentity.anonymous().authenticated());
    }

    @Test
    void moduleOwnershipAllowsOwnersAndAdminsToManageOwnedResources() {
        AppUser owner = new AppUser();
        owner.setId(10L);
        owner.setRole(AppUserRole.USER);

        AppUser admin = new AppUser();
        admin.setId(20L);
        admin.setRole(AppUserRole.ADMIN);

        assertTrue(ModuleOwnership.isOwner(10L, owner));
        assertTrue(ModuleOwnership.canManage(10L, owner));
        assertTrue(ModuleOwnership.canManage(10L, admin));
        assertFalse(ModuleOwnership.canManage(10L, ActorIdentity.anonymous()));
    }

    @Test
    void schedulingWindowDetectsPastStartsEndWithoutStartAndInvalidRanges() {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");

        assertTrue(new SchedulingWindow(now.minusSeconds(60), null).startsBefore(now));
        assertTrue(new SchedulingWindow(null, now.plusSeconds(60)).hasEndWithoutStart());
        assertTrue(new SchedulingWindow(now.plusSeconds(60), now).hasInvalidRange());
        assertFalse(new SchedulingWindow(now, now.plusSeconds(60)).hasInvalidRange());
    }

    @Test
    void circleVisibilitySelectionNormalizesNullAndCountsDistinctIds() {
        assertTrue(CircleVisibilitySelection.from(null).unrestricted());
        assertFalse(CircleVisibilitySelection.from(List.of(1L, 1L, 2L)).unrestricted());
        assertEquals(2, CircleVisibilitySelection.from(List.of(1L, 1L, 2L)).distinctCount());
    }
}
