package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import org.springframework.stereotype.Service;

@Service
public class VisionMarkNotificationsReadExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "mark_notifications_read";

    private final WorkmarketQuestNewsService questNewsService;

    public VisionMarkNotificationsReadExecutionAdapter(WorkmarketQuestNewsService questNewsService) {
        this.questNewsService = questNewsService;
    }

    @Override
    public String capabilityId() {
        return CAPABILITY_ID;
    }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return VisionExecutionResult.blocked("Conversation owner is required to mark notifications as read.");
        }
        questNewsService.markMyNewsAsRead(conversation.getOwner());
        return VisionExecutionResult.executedAction(CAPABILITY_ID);
    }
}
