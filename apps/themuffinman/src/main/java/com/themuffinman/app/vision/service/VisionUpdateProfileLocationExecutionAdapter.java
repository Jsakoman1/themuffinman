package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionUpdateProfileLocationExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "update_profile_location";

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public VisionUpdateProfileLocationExecutionAdapter(VisionCapabilityPreviewService visionCapabilityPreviewService) {
        this.visionCapabilityPreviewService = visionCapabilityPreviewService;
    }

    @Override
    public String capabilityId() {
        return CAPABILITY_ID;
    }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return VisionExecutionResult.blocked("Conversation owner is required for profile location execution.");
        }

        AppUserResponseDTO updatedProfile = visionCapabilityPreviewService.updateProfileLocation(
                conversation.getOwner(),
                conversation.getSlotData().get("profile_location_mode"),
                conversation.getSlotData().get("profile_location_label")
        );
        return VisionExecutionResult.executedProfile(CAPABILITY_ID, updatedProfile);
    }
}
