package com.themuffinman.app.vision.service;

import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionWithdrawApplicationExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "withdraw_application";

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public VisionWithdrawApplicationExecutionAdapter(VisionCapabilityPreviewService visionCapabilityPreviewService) {
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

        QuestApplicationResponseDTO application = visionCapabilityPreviewService.withdrawApplication(
                conversation.getOwner(),
                Long.parseLong(questId)
        );
        return VisionExecutionResult.executedApplication(CAPABILITY_ID, application);
    }
}
