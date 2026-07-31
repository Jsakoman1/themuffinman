package com.themuffinman.app.notification.dto;

import com.themuffinman.app.workmarket.dto.QuestNewsItemResponseDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class AttentionCenterDTO {
    private long unreadCount;
    private List<QuestNewsItemResponseDTO> items;
}
