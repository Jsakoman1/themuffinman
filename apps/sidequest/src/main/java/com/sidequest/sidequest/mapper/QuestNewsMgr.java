package com.sidequest.sidequest.mapper;

import com.sidequest.sidequest.dto.QuestNewsItemResponseDTO;
import com.sidequest.sidequest.model.QuestNewsItem;
import org.springframework.stereotype.Component;

@Component
public class QuestNewsMgr {

    public QuestNewsItemResponseDTO toDto(QuestNewsItem item) {
        if (item == null) {
            return null;
        }

        return QuestNewsItemResponseDTO.builder()
                .id(item.getId())
                .type(item.getType())
                .title(item.getTitle())
                .message(item.getMessage())
                .questId(item.getQuestId())
                .questTitle(item.getQuestTitle())
                .applicationId(item.getApplicationId())
                .actorUserId(item.getActorUserId())
                .actorUsername(item.getActorUsername())
                .readAt(item.getReadAt())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
