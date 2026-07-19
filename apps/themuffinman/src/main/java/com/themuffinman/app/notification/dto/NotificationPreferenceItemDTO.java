package com.themuffinman.app.notification.dto;

import com.themuffinman.app.notification.model.NotificationPreferenceCategory;
import com.themuffinman.app.notification.model.NotificationPreferenceLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationPreferenceItemDTO {
    private NotificationPreferenceCategory category;
    private NotificationPreferenceLevel level;
    private boolean enabled;
    private boolean required;
    private boolean available;
    private boolean effectiveEnabled;
    private String unavailableReason;
}
