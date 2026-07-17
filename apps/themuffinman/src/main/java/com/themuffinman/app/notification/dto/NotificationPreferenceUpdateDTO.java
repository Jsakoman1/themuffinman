package com.themuffinman.app.notification.dto;

import com.themuffinman.app.notification.model.NotificationPreferenceCategory;
import com.themuffinman.app.notification.model.NotificationPreferenceLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationPreferenceUpdateDTO {
    @NotNull
    private NotificationPreferenceCategory category;
    @NotNull
    private NotificationPreferenceLevel level;
    @NotNull
    private Boolean enabled;
}
