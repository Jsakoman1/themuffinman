package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionDeleteCircleRequestExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "delete_circle_request";

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public VisionDeleteCircleRequestExecutionAdapter(VisionCapabilityPreviewService visionCapabilityPreviewService) {
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

        visionCapabilityPreviewService.deleteCircleRequest(
                conversation.getOwner(),
                Long.parseLong(requestId)
        );
        return VisionExecutionResult.builder()
                .executed(true)
                .capabilityId(CAPABILITY_ID)
                .build();
    }
}
