package com.themuffinman.app.vision.service;

import com.themuffinman.app.notification.dto.NotificationPreferenceUpdateDTO;
import com.themuffinman.app.notification.model.NotificationPreferenceCategory;
import com.themuffinman.app.notification.model.NotificationPreferenceLevel;
import com.themuffinman.app.notification.service.NotificationPreferenceService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisionNotificationPreferenceExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private static final String CAPABILITY_ID = "update_notification_preferences";
    private final NotificationPreferenceService service;

    public VisionNotificationPreferenceExecutionAdapter(NotificationPreferenceService service) {
        this.service = service;
    }

    @Override
    public String capabilityId() {
        return CAPABILITY_ID;
    }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return VisionExecutionResult.blocked("Conversation owner is required to update notification preferences.");
        }
        try {
            NotificationPreferenceCategory category = NotificationPreferenceCategory.valueOf(conversation.getSlotData().get("notification_category"));
            NotificationPreferenceLevel level = NotificationPreferenceLevel.valueOf(conversation.getSlotData().get("notification_level"));
            boolean enabled = Boolean.parseBoolean(conversation.getSlotData().get("notification_enabled"));
            NotificationPreferenceUpdateDTO update = new NotificationPreferenceUpdateDTO();
            update.setCategory(category);
            update.setLevel(level);
            update.setEnabled(enabled);
            service.update(conversation.getOwner(), List.of(update));
            return VisionExecutionResult.executedAction(CAPABILITY_ID);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return VisionExecutionResult.blocked("The notification preference was not valid.");
        } catch (RuntimeException exception) {
            return VisionExecutionResult.blocked(exception.getMessage() == null
                    ? "The notification preference could not be updated."
                    : exception.getMessage());
        }
    }
}
