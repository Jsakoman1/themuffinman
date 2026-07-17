package com.themuffinman.app.notification.dto;

import com.themuffinman.app.activity.dto.ActivityItemDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class AttentionCenterDTO {
    private long unreadCount;
    private List<ActivityItemDTO> items;
}
