package com.themuffinman.app.vision.service;

import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionCreateCircleExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "create_circle";

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public VisionCreateCircleExecutionAdapter(VisionCapabilityPreviewService visionCapabilityPreviewService) {
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
        String circleName = conversation.getSlotData().get("circle_name");
        if (circleName == null || circleName.isBlank()) {
            return VisionExecutionResult.blocked("Missing required slot: circle_name");
        }

        CircleGroupResponseDTO createdCircle = visionCapabilityPreviewService.createCircle(circleName, conversation.getOwner());
        return VisionExecutionResult.executedCircle(CAPABILITY_ID, createdCircle);
    }
}
