package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.service.WorkmarketQuestUpdateService;
import org.springframework.stereotype.Service;

@Service
public class VisionUpdateQuestExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final WorkmarketQuestUpdateService service;
    public VisionUpdateQuestExecutionAdapter(WorkmarketQuestUpdateService service) { this.service = service; }
    public String capabilityId() { return "update_quest"; }
    public VisionExecutionResult execute(VisionConversation conversation) {
        try {
            service.updateQuestTitleForVision(Long.valueOf(conversation.getSlotData().get("quest_id")), conversation.getSlotData().get("quest_title"), conversation.getOwner());
            return VisionExecutionResult.executedAction(capabilityId());
        } catch (RuntimeException exception) { return VisionExecutionResult.blocked("The quest could not be updated."); }
    }
}
