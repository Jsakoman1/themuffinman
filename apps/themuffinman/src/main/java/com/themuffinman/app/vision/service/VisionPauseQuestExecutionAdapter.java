package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.service.WorkmarketQuestUpdateService;
import org.springframework.stereotype.Service;

@Service
public class VisionPauseQuestExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final WorkmarketQuestUpdateService service;
    public VisionPauseQuestExecutionAdapter(WorkmarketQuestUpdateService service) { this.service = service; }
    @Override public String capabilityId() { return "pause_quest"; }
    @Override public VisionExecutionResult execute(VisionConversation conversation) {
        return execute(conversation, false, service);
    }
    static VisionExecutionResult execute(VisionConversation conversation, boolean resume, WorkmarketQuestUpdateService service) {
        if (conversation == null || conversation.getOwner() == null) return VisionExecutionResult.blocked("Conversation owner is required.");
        String rawQuestId = conversation.getSlotData().get("quest_id");
        if (rawQuestId == null || rawQuestId.isBlank()) return VisionExecutionResult.blocked("A quest id is required.");
        try {
            Quest quest = resume ? service.resumeQuest(Long.parseLong(rawQuestId), conversation.getOwner()) : service.pauseQuest(Long.parseLong(rawQuestId), conversation.getOwner());
            return VisionExecutionResult.executed(resume ? "resume_quest" : "pause_quest", quest);
        } catch (NumberFormatException exception) { return VisionExecutionResult.blocked("The quest id is invalid."); }
    }
}
