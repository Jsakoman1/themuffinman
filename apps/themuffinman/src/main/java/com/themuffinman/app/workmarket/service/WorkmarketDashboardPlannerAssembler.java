package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.vision.dto.DashboardPlannerItemDTO;
import com.themuffinman.app.vision.dto.DashboardPlannerSectionDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service("workmarketDashboardPlannerAssembler")
public class WorkmarketDashboardPlannerAssembler {

    public DashboardPlannerSectionDTO buildPlannerSection(
            List<QuestResponseDTO> incomingWorkQuests,
            List<QuestApplicationResponseDTO> outgoingWorkApplications
    ) {
        List<DashboardPlannerItemDTO> scheduledItems = new java.util.ArrayList<>();
        List<DashboardPlannerItemDTO> flexibleItems = new java.util.ArrayList<>();

        for (QuestResponseDTO quest : incomingWorkQuests) {
            addPlannerItem(scheduledItems, flexibleItems, buildQuestPlannerItem(quest, "managed"));
        }

        for (QuestApplicationResponseDTO application : outgoingWorkApplications) {
            addPlannerItem(scheduledItems, flexibleItems, buildApplicationPlannerItem(application, "accepted"));
        }

        scheduledItems.sort(Comparator
                .comparing((DashboardPlannerItemDTO item) -> item.getScheduledAt() == null ? Instant.MAX : item.getScheduledAt())
                .thenComparing(DashboardPlannerItemDTO::getId));

        return DashboardPlannerSectionDTO.builder()
                .scheduledItems(scheduledItems)
                .flexibleItems(flexibleItems)
                .build();
    }

    private DashboardPlannerItemDTO buildQuestPlannerItem(QuestResponseDTO quest, String kind) {
        return buildPlannerItem(
                "quest-" + quest.getId(),
                quest.getId(),
                quest.getTitle(),
                quest.getQuestNavigation(),
                quest.getScheduledAt(),
                quest.getEndsAt(),
                kind
        );
    }

    private DashboardPlannerItemDTO buildApplicationPlannerItem(QuestApplicationResponseDTO application, String kind) {
        return buildPlannerItem(
                "application-" + application.getId(),
                application.getQuestId(),
                application.getQuestTitle(),
                application.getQuestNavigation(),
                application.getQuestScheduledAt(),
                application.getQuestEndsAt(),
                kind
        );
    }

    private DashboardPlannerItemDTO buildPlannerItem(
            String id,
            Long questId,
            String title,
            NavigationTargetDTO navigation,
            Instant scheduledAt,
            Instant endsAt,
            String kind
    ) {
        if (scheduledAt == null) {
            return DashboardPlannerItemDTO.builder()
                    .id(id)
                    .questId(questId)
                    .title(title)
                    .navigation(navigation)
                    .scheduledAt(null)
                    .endsAt(null)
                    .kind(kind)
                    .kindLabel(resolvePlannerKindLabel(kind))
                    .tone(resolvePlannerTone(kind))
                    .hasRange(false)
                    .build();
        }

        return DashboardPlannerItemDTO.builder()
                .id(id)
                .questId(questId)
                .title(title)
                .navigation(navigation)
                .scheduledAt(scheduledAt)
                .endsAt(endsAt)
                .kind(kind)
                .kindLabel(resolvePlannerKindLabel(kind))
                .tone(resolvePlannerTone(kind))
                .hasRange(endsAt != null)
                .build();
    }

    private void addPlannerItem(
            List<DashboardPlannerItemDTO> scheduledItems,
            List<DashboardPlannerItemDTO> flexibleItems,
            DashboardPlannerItemDTO item
    ) {
        if (item.getScheduledAt() == null) {
            flexibleItems.add(item);
            return;
        }

        scheduledItems.add(item);
    }

    private String resolvePlannerKindLabel(String kind) {
        return switch (kind) {
            case "managed" -> "My job";
            case "accepted" -> "My application";
            default -> "Planned";
        };
    }

    private String resolvePlannerTone(String kind) {
        return switch (kind) {
            case "accepted" -> "incoming";
            case "managed" -> "outgoing";
            default -> "outgoing";
        };
    }
}
