package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionIntent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionIntentSignalSupportTest {

    private final VisionIntentSignalSupport support = new VisionIntentSignalSupport();

    @Test
    void overridesSnapshotIntentsForEmbeddedActions() {
        assertEquals(VisionIntent.CREATE_CIRCLE, support.overrideSnapshotIntent(VisionIntent.VIEW_CIRCLES, "create circle Neighbours"));
        assertEquals(VisionIntent.UPDATE_APPLICATION, support.overrideSnapshotIntent(VisionIntent.VIEW_APPLICATION_DETAIL, "update my application"));
        assertEquals(VisionIntent.UPDATE_PROFILE_LOCATION, support.overrideSnapshotIntent(VisionIntent.VIEW_SETTINGS, "turn off my location"));
        assertEquals(VisionIntent.OPEN_CHAT, support.overrideSnapshotIntent(VisionIntent.VIEW_CHAT_WORKSPACE, "chat with Josip"));
    }

    @Test
    void detectsCommonSignalFamilies() {
        assertTrue(support.containsNotificationsSignals("show inbox"));
        assertTrue(support.containsUserProfileDetailSignals("open profile of Josip"));
        assertTrue(support.containsSearchSignals("find people who can help"));
        assertTrue(support.containsDiscoverySignals("looking for work"));
    }

    @Test
    void decidesWhenToKeepTheExistingConversation() {
        assertTrue(support.shouldKeepExistingConversation("continue", VisionIntent.VIEW_CIRCLES, 0.20d));
        assertFalse(support.shouldKeepExistingConversation("switch to applications", VisionIntent.VIEW_CIRCLES, 0.20d));
        assertFalse(support.shouldKeepExistingConversation("show applications", VisionIntent.VIEW_APPLICATIONS, 0.90d));
    }

    @Test
    void groupsWorkspaceFamilies() {
        assertTrue(support.sameIntentWorkspaceFamily(VisionIntent.VIEW_PROFILE, VisionIntent.UPDATE_PROFILE));
        assertTrue(support.sameIntentWorkspaceFamily(VisionIntent.VIEW_APPLICATIONS, VisionIntent.CREATE_APPLICATION));
        assertFalse(support.sameIntentWorkspaceFamily(VisionIntent.VIEW_PROFILE, VisionIntent.VIEW_APPLICATIONS));
    }
}
