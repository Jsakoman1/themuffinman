package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import org.springframework.stereotype.Service;

@Service
public class VisionMarkNotificationReadExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "mark_notification_read";

    private final WorkmarketQuestNewsService questNewsService;

    public VisionMarkNotificationReadExecutionAdapter(WorkmarketQuestNewsService questNewsService) {
        this.questNewsService = questNewsService;
    }

    @Override
    public String capabilityId() {
        return CAPABILITY_ID;
    }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return VisionExecutionResult.blocked("Conversation owner is required to mark a notification as read.");
        }
        String rawNotificationId = conversation.getSlotData().get("notification_id");
        if (rawNotificationId == null || rawNotificationId.isBlank()) {
            return VisionExecutionResult.blocked("A notification id is required.");
        }
        try {
            questNewsService.markMyNewsItemAsRead(Long.parseLong(rawNotificationId), conversation.getOwner());
        } catch (NumberFormatException exception) {
            return VisionExecutionResult.blocked("The notification id is invalid.");
        }
        return VisionExecutionResult.executedAction(CAPABILITY_ID);
    }
}
