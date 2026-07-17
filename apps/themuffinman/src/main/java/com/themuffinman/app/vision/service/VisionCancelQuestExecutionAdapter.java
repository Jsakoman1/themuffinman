package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.service.WorkmarketQuestUpdateService;
import org.springframework.stereotype.Service;

@Service
public class VisionCancelQuestExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "cancel_quest";
    private final WorkmarketQuestUpdateService questUpdateService;

    public VisionCancelQuestExecutionAdapter(WorkmarketQuestUpdateService questUpdateService) {
        this.questUpdateService = questUpdateService;
    }

    @Override
    public String capabilityId() {
        return CAPABILITY_ID;
    }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return VisionExecutionResult.blocked("Conversation owner is required to cancel a quest.");
        }
        String rawQuestId = conversation.getSlotData().get("quest_id");
        if (rawQuestId == null || rawQuestId.isBlank()) {
            return VisionExecutionResult.blocked("A quest id is required.");
        }
        try {
            Quest quest = questUpdateService.cancelQuestForVision(Long.parseLong(rawQuestId), conversation.getOwner());
            return VisionExecutionResult.executed(CAPABILITY_ID, quest);
        } catch (NumberFormatException exception) {
            return VisionExecutionResult.blocked("The quest id is invalid.");
        }
    }
}
