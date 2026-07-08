package com.themuffinman.app.workmarket.mapper;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.common.dto.NavigationTargetType;
import com.themuffinman.app.workmarket.dto.QuestNewsDestinationTypeDTO;
import com.themuffinman.app.workmarket.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketPresentationHelper;
import com.themuffinman.app.workmarket.model.QuestNewsItem;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkmarketQuestNewsMgr {
    private final WorkmarketPresentationHelper presentationHelper;

    public QuestNewsItemResponseDTO toDto(QuestNewsItem item) {
        if (item == null) {
            return null;
        }

        QuestNewsDestinationTypeDTO destinationType = QuestNewsDestinationTypeDTO.QUEST_LIST;
        Long destinationId = null;
        NavigationTargetDTO navigation = NavigationTargetDTO.builder()
                .type(NavigationTargetType.QUEST_LIST)
                .entityId(null)
                .build();
        if (item.getApplicationId() != null) {
            destinationType = QuestNewsDestinationTypeDTO.APPLICATION;
            destinationId = item.getApplicationId();
            navigation = NavigationTargetDTO.builder()
                    .type(NavigationTargetType.APPLICATION_DETAIL)
                    .entityId(item.getApplicationId())
                    .build();
        } else if (item.getType() == com.themuffinman.app.workmarket.model.QuestNewsType.CIRCLE_REQUEST_RECEIVED
                || item.getType() == com.themuffinman.app.workmarket.model.QuestNewsType.CIRCLE_REQUEST_ACCEPTED) {
            navigation = NavigationTargetDTO.builder()
                    .type(NavigationTargetType.CIRCLES)
                    .entityId(null)
                    .build();
        } else if (item.getQuestId() != null) {
            destinationType = QuestNewsDestinationTypeDTO.QUEST;
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
                .iconGlyph(presentationHelper.iconGlyphForQuestNewsType(item.getType()))
                .title(item.getTitle())
                .message(item.getMessage())
                .questId(item.getQuestId())
                .questTitle(item.getQuestTitle())
                .applicationId(item.getApplicationId())
                .circleRequestId(item.getCircleRequestId())
                .destinationType(destinationType)
                .destinationId(destinationId)
                .resolutionKey("news:" + item.getId())
                .resolutionLabel(item.getTitle())
                .exactResolutionEligible(item.getId() != null)
                .navigation(navigation)
                .actorUserId(item.getActorUserId())
                .actorUsername(item.getActorUsername())
                .canAcceptCircleRequest(item.getType() == QuestNewsType.CIRCLE_REQUEST_RECEIVED && item.getCircleRequestId() != null)
                .canDeclineCircleRequest(item.getType() == QuestNewsType.CIRCLE_REQUEST_RECEIVED && item.getCircleRequestId() != null)
                .readAt(item.getReadAt())
                .createdAt(item.getCreatedAt())
                .build();
    }

}
