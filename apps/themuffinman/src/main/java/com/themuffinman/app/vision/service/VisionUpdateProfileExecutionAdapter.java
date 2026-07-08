package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionUpdateProfileExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "update_profile";

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public VisionUpdateProfileExecutionAdapter(VisionCapabilityPreviewService visionCapabilityPreviewService) {
        this.visionCapabilityPreviewService = visionCapabilityPreviewService;
    }

    @Override
    public String capabilityId() {
        return CAPABILITY_ID;
    }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return VisionExecutionResult.blocked("Conversation owner is required for profile execution.");
        }

        AppUserResponseDTO updatedProfile = visionCapabilityPreviewService.updateProfile(
                conversation.getOwner(),
                conversation.getSlotData().get("profile_username"),
                conversation.getSlotData().get("profile_description")
        );
        return VisionExecutionResult.executedProfile(CAPABILITY_ID, updatedProfile);
    }
}
