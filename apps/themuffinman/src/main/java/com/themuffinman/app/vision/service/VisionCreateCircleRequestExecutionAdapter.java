package com.themuffinman.app.vision.service;

import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionCreateCircleRequestExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "create_circle_request";

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public VisionCreateCircleRequestExecutionAdapter(VisionCapabilityPreviewService visionCapabilityPreviewService) {
        this.visionCapabilityPreviewService = visionCapabilityPreviewService;
    }

    @Override
    public String capabilityId() {
        return CAPABILITY_ID;
    }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return VisionExecutionResult.blocked("Conversation owner is required for circle request execution.");
        }
        String targetUserId = conversation.getSlotData().get("circle_request_target_user_id");
        if (targetUserId == null || targetUserId.isBlank()) {
            return VisionExecutionResult.blocked("Missing required slot: circle_request_target_user_id");
        }

        CircleRequestResponseDTO circleRequest = visionCapabilityPreviewService.createCircleRequest(
                conversation.getOwner(),
                Long.parseLong(targetUserId)
        );
        return VisionExecutionResult.executedCircleRequest(CAPABILITY_ID, circleRequest);
    }
}
