package com.themuffinman.app.workmarket.mapper;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.common.dto.NavigationTargetType;
import com.themuffinman.app.workmarket.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestNewsDestinationType;
import com.themuffinman.app.workmarket.model.QuestNewsItem;
import com.themuffinman.app.workmarket.service.WorkmarketPresentationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestNewsMgr {
    private final WorkmarketPresentationHelper presentationHelper;

    public QuestNewsItemResponseDTO toDto(QuestNewsItem item) {
        if (item == null) {
            return null;
        }

        QuestNewsDestinationType destinationType = QuestNewsDestinationType.QUEST_LIST;
        Long destinationId = null;
        NavigationTargetDTO navigation = NavigationTargetDTO.builder()
                .type(NavigationTargetType.QUEST_LIST)
                .entityId(null)
                .build();
        if (item.getApplicationId() != null) {
            destinationType = QuestNewsDestinationType.APPLICATION;
            destinationId = item.getApplicationId();
            navigation = NavigationTargetDTO.builder()
                    .type(NavigationTargetType.APPLICATION_DETAIL)
                    .entityId(item.getApplicationId())
                    .build();
        } else if (item.getQuestId() != null) {
            destinationType = QuestNewsDestinationType.QUEST;
            destinationId = item.getQuestId();
            navigation = NavigationTargetDTO.builder()
                    .type(NavigationTargetType.QUEST_DETAIL)
                    .entityId(item.getQuestId())
                    .build();
        }

        return QuestNewsItemResponseDTO.builder()
                .id(item.getId())
                .type(item.getType())
                .typeLabel(presentationHelper.formatQuestNewsType(item.getType()))
                .badgeClass(presentationHelper.badgeClassForQuestNewsType(item.getType()))
                .title(item.getTitle())
                .message(item.getMessage())
                .questId(item.getQuestId())
                .questTitle(item.getQuestTitle())
                .applicationId(item.getApplicationId())
                .destinationType(destinationType)
                .destinationId(destinationId)
                .navigation(navigation)
                .actorUserId(item.getActorUserId())
                .actorUsername(item.getActorUsername())
                .readAt(item.getReadAt())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
