package com.themuffinman.app.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationPreferenceResponseDTO {
    private List<NotificationPreferenceItemDTO> items;
}
