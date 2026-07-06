package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionIntent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisionConversationSnapshotSupportTest {

    @Mock
    private VisionCapabilityPreviewService visionCapabilityPreviewService;

    @Test
    void rendersReadOnlySnapshotMessages() {
        assertEquals("Profile.", VisionConversationSnapshotSupport.readOnlySnapshotMessage(VisionIntent.VIEW_PROFILE));
        assertEquals("The current view was reset. Chat.", VisionConversationSnapshotSupport.resetReadOnlySnapshotMessage(VisionIntent.VIEW_CHAT_WORKSPACE));
        assertEquals("The current view was reset.", VisionConversationSnapshotSupport.resetReadOnlySnapshotMessage(VisionIntent.VIEW_CIRCLE_DETAIL));
    }

    @Test
    void delegatesCapabilityPreviewForReadOnlyViews() {
        AppUser currentUser = new AppUser();
        VisionConversation conversation = new VisionConversation();
        conversation.setIntent(VisionIntent.VIEW_PROFILE);
        conversation.setSlotData(Map.of());

        VisionCapabilityPreviewDTO preview = VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_profile")
                .title("Profile")
                .summary("Profile.")
                .build();
        when(visionCapabilityPreviewService.previewProfile(currentUser)).thenReturn(preview);

        assertEquals(preview, VisionConversationSnapshotSupport.capabilityPreview(conversation, currentUser, visionCapabilityPreviewService));
    }

    @Test
    void returnsNullWhenPreviewCannotBeBuilt() {
        VisionConversation conversation = new VisionConversation();
        conversation.setIntent(VisionIntent.DISCOVER_QUESTS);
        conversation.setSlotData(Map.of());

        assertNull(VisionConversationSnapshotSupport.capabilityPreview(conversation, new AppUser(), visionCapabilityPreviewService));
    }
}
