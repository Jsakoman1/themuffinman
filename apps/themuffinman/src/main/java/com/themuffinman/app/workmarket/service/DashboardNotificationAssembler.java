package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.DashboardNotificationDestinationTypeDTO;
import com.themuffinman.app.workmarket.dto.DashboardNotificationItemDTO;
import com.themuffinman.app.workmarket.dto.QuestNewsItemResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardNotificationAssembler {

    public List<DashboardNotificationItemDTO> toRecentItems(List<QuestNewsItemResponseDTO> recentNews) {
        return recentNews.stream()
                .map(this::toDashboardNotificationItem)
                .toList();
    }

    private DashboardNotificationItemDTO toDashboardNotificationItem(QuestNewsItemResponseDTO item) {
        DashboardNotificationDestinationTypeDTO destinationType = DashboardNotificationDestinationTypeDTO.QUEST_LIST;
        Long destinationId = null;

        if (item.getApplicationId() != null) {
            destinationType = DashboardNotificationDestinationTypeDTO.APPLICATION;
            destinationId = item.getApplicationId();
        } else if (item.getQuestId() != null) {
            destinationType = DashboardNotificationDestinationTypeDTO.QUEST;
            destinationId = item.getQuestId();
        }

        return DashboardNotificationItemDTO.builder()
                .id(item.getId())
                .type(item.getType())
                .typeLabel(item.getTypeLabel())
                .badgeClass(item.getBadgeClass())
                .iconGlyph(item.getIconGlyph())
                .title(item.getTitle())
                .message(item.getMessage())
                .actorUsername(item.getActorUsername())
                .questTitle(item.getQuestTitle())
                .questId(item.getQuestId())
                .applicationId(item.getApplicationId())
                .circleRequestId(item.getCircleRequestId())
                .createdAt(item.getCreatedAt())
                .readAt(item.getReadAt())
                .destinationType(destinationType)
                .destinationId(destinationId)
                .navigation(item.getNavigation())
                .unread(item.getReadAt() == null)
                .canAcceptCircleRequest(item.isCanAcceptCircleRequest())
                .canDeclineCircleRequest(item.isCanDeclineCircleRequest())
                .build();
    }
}
