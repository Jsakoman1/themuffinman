package com.themuffinman.app.vision.service;

import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionCreateApplicationExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "create_application";

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public VisionCreateApplicationExecutionAdapter(VisionCapabilityPreviewService visionCapabilityPreviewService) {
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
        String questId = conversation.getSlotData().get("application_quest_id");
        if (questId == null || questId.isBlank()) {
            return VisionExecutionResult.blocked("Missing required slot: application_quest_id");
        }

        QuestApplicationResponseDTO application = visionCapabilityPreviewService.createApplication(
                conversation.getOwner(),
                Long.parseLong(questId),
                conversation.getSlotData().get("application_message"),
                conversation.getSlotData().get("application_proposed_price")
        );
        return VisionExecutionResult.executedApplication(CAPABILITY_ID, application);
    }
}
