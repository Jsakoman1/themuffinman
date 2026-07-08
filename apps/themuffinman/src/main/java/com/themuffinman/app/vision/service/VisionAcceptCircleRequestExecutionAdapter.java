package com.themuffinman.app.vision.service;

import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionAcceptCircleRequestExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "accept_circle_request";

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public VisionAcceptCircleRequestExecutionAdapter(VisionCapabilityPreviewService visionCapabilityPreviewService) {
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
        String requestId = conversation.getSlotData().get("circle_request_id");
        if (requestId == null || requestId.isBlank()) {
            return VisionExecutionResult.blocked("Missing required slot: circle_request_id");
        }

        CircleRequestResponseDTO circleRequest = visionCapabilityPreviewService.acceptCircleRequest(
                conversation.getOwner(),
                Long.parseLong(requestId)
        );
        return VisionExecutionResult.executedCircleRequest(CAPABILITY_ID, circleRequest);
    }
}
