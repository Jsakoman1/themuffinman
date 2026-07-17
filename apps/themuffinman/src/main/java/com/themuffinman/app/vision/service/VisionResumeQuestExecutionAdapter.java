package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.service.WorkmarketQuestUpdateService;
import org.springframework.stereotype.Service;

@Service
public class VisionResumeQuestExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final WorkmarketQuestUpdateService service;
    public VisionResumeQuestExecutionAdapter(WorkmarketQuestUpdateService service) { this.service = service; }
    @Override public String capabilityId() { return "resume_quest"; }
    @Override public VisionExecutionResult execute(VisionConversation conversation) {
        return VisionPauseQuestExecutionAdapter.execute(conversation, true, service);
    }
}
