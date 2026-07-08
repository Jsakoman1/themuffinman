package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionDeleteCircleExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "delete_circle";

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public VisionDeleteCircleExecutionAdapter(VisionCapabilityPreviewService visionCapabilityPreviewService) {
        this.visionCapabilityPreviewService = visionCapabilityPreviewService;
    }

    @Override
    public String capabilityId() {
        return CAPABILITY_ID;
    }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return VisionExecutionResult.blocked("Conversation owner is required for circle execution.");
        }
        String circleId = conversation.getSlotData().get("resolved_circle_id");
        if (circleId == null || circleId.isBlank()) {
            return VisionExecutionResult.blocked("Missing required slot: resolved_circle_id");
        }

        visionCapabilityPreviewService.deleteCircle(
                conversation.getOwner(),
                Long.parseLong(circleId)
        );
        return VisionExecutionResult.builder()
                .executed(true)
                .capabilityId(CAPABILITY_ID)
                .build();
    }
}
