package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.DashboardApplicationGroupDTO;
import com.themuffinman.app.workmarket.dto.DashboardQuestGroupDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("workmarketDashboardSectionGrouper")
public class WorkmarketDashboardSectionGrouper {

    public List<DashboardQuestGroupDTO> buildQuestGroups(List<QuestResponseDTO> quests) {
        return List.of(
                        buildQuestGroup("OPEN", "accent", quests),
                        buildQuestGroup("WAITING_CONFIRMATION", "warning", quests),
                        buildQuestGroup("ASSIGNED", "success", quests),
                        buildQuestGroup("IN_PROGRESS", "accent", quests),
                        buildQuestGroup("COMPLETED", "success", quests),
                        buildQuestGroup("CANCELLED", "muted", quests)
                ).stream()
                .filter(group -> group.getCount() > 0)
                .toList();
    }

    public List<DashboardApplicationGroupDTO> buildApplicationGroups(List<QuestApplicationResponseDTO> applications) {
        return List.of(
                        buildApplicationGroup("APPROVED", "success", applications),
                        buildApplicationGroup("PENDING", "accent", applications),
                        buildApplicationGroup("DECLINED", "muted", applications),
                        buildApplicationGroup("WITHDRAWN", "muted", applications)
                ).stream()
                .filter(group -> group.getCount() > 0)
                .toList();
    }

    private DashboardQuestGroupDTO buildQuestGroup(String statusKey, String tone, List<QuestResponseDTO> quests) {
        List<QuestResponseDTO> items = quests.stream()
                .filter(quest -> quest.getStatus() != null && quest.getStatus().name().equals(statusKey))
                .toList();
        String label = resolveQuestGroupLabel(statusKey, items);

        return DashboardQuestGroupDTO.builder()
                .key(statusKey)
                .label(label)
                .tone(tone)
                .count(items.size())
                .items(items)
                .build();
    }

    private DashboardApplicationGroupDTO buildApplicationGroup(
            String statusKey,
            String tone,
            List<QuestApplicationResponseDTO> applications
    ) {
        List<QuestApplicationResponseDTO> items = applications.stream()
                .filter(application -> application.getStatus() != null && application.getStatus().name().equals(statusKey))
                .toList();
        String label = resolveApplicationGroupLabel(statusKey, items);

        return DashboardApplicationGroupDTO.builder()
                .key(statusKey)
                .label(label)
                .tone(tone)
                .count(items.size())
                .items(items)
                .build();
    }

    private String resolveQuestGroupLabel(String statusKey, List<QuestResponseDTO> items) {
        if (!items.isEmpty()
                && items.getFirst().getPresentation() != null
                && items.getFirst().getPresentation().getStatusLabel() != null) {
            return items.getFirst().getPresentation().getStatusLabel();
        }

        return switch (statusKey) {
            case "OPEN" -> "Open";
            case "WAITING_CONFIRMATION" -> "Waiting confirmation";
            case "ASSIGNED" -> "Assigned";
            case "IN_PROGRESS" -> "In progress";
            case "COMPLETED" -> "Completed";
            case "CANCELLED" -> "Cancelled";
            default -> statusKey;
        };
    }

    private String resolveApplicationGroupLabel(String statusKey, List<QuestApplicationResponseDTO> items) {
        if (!items.isEmpty()
                && items.getFirst().getPresentation() != null
                && items.getFirst().getPresentation().getStatusLabel() != null) {
            return items.getFirst().getPresentation().getStatusLabel();
        }

        return switch (statusKey) {
            case "APPROVED" -> "Approved";
            case "PENDING" -> "Pending";
            case "DECLINED" -> "Declined";
            case "WITHDRAWN" -> "Withdrawn";
            case "RELEASED" -> "Released";
            default -> statusKey;
        };
    }
}
