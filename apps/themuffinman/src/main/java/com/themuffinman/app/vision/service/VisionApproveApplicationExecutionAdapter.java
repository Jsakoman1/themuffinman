package com.themuffinman.app.vision.service;

import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionApproveApplicationExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "approve_application";

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public VisionApproveApplicationExecutionAdapter(VisionCapabilityPreviewService visionCapabilityPreviewService) {
        this.visionCapabilityPreviewService = visionCapabilityPreviewService;
    }

    @Override
    public String capabilityId() {
        return CAPABILITY_ID;
    }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return VisionExecutionResult.blocked("Conversation owner is required for application execution.");
        }
        String questId = conversation.getSlotData().get("managed_application_quest_id");
        String applicationId = conversation.getSlotData().get("managed_application_id");
        if (questId == null || questId.isBlank()) {
            return VisionExecutionResult.blocked("Missing required slot: managed_application_quest_id");
        }
        if (applicationId == null || applicationId.isBlank()) {
            return VisionExecutionResult.blocked("Missing required slot: managed_application_id");
        }

        QuestApplicationResponseDTO application = visionCapabilityPreviewService.approveManagedApplication(
                conversation.getOwner(),
                Long.parseLong(questId),
                Long.parseLong(applicationId)
        );
        return VisionExecutionResult.executedApplication(CAPABILITY_ID, application);
    }
}
