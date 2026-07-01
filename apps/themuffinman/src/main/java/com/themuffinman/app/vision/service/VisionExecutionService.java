package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.Quest;
import org.springframework.stereotype.Service;

@Service
public class VisionExecutionService {

    private final VisionSurfacePolicy visionSurfacePolicy;
    private final VisionCreateQuestExecutionAdapter visionCreateQuestExecutionAdapter;

    public VisionExecutionService(
            VisionSurfacePolicy visionSurfacePolicy,
            VisionCreateQuestExecutionAdapter visionCreateQuestExecutionAdapter
    ) {
        this.visionSurfacePolicy = visionSurfacePolicy;
        this.visionCreateQuestExecutionAdapter = visionCreateQuestExecutionAdapter;
    }

    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null) {
            return VisionExecutionResult.blocked("Conversation is required for execution.");
        }
        if (conversation.getIntent() != VisionIntent.CREATE_QUEST) {
            return VisionExecutionResult.blocked("Execution is only supported for create quest conversations.");
        }
        if (conversation.getStatus() != VisionConversationStatus.REVIEW_READY) {
            return VisionExecutionResult.blocked("Conversation is not ready for execution.");
        }
        if (!visionSurfacePolicy.canExecuteCapability("create_quest")) {
            return VisionExecutionResult.blocked("Execution is disabled by configuration.");
        }

        Quest createdQuest = visionCreateQuestExecutionAdapter.execute(conversation.getSlotData(), conversation.getOwner());
        return VisionExecutionResult.executed(createdQuest);
    }
}
