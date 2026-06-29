package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.workmarket.dto.DashboardNavigationItemDTO;
import com.themuffinman.app.workmarket.dto.DashboardNavigationSectionDTO;
import com.themuffinman.app.workmarket.dto.DashboardNotificationItemDTO;
import com.themuffinman.app.workmarket.dto.DashboardNotificationsSectionDTO;
import com.themuffinman.app.workmarket.dto.DashboardOpenWorkSectionDTO;
import com.themuffinman.app.workmarket.dto.DashboardPlannerItemDTO;
import com.themuffinman.app.workmarket.dto.DashboardPlannerSectionDTO;
import com.themuffinman.app.workmarket.dto.DashboardSectionsDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardSectionsFactory {

    private final DashboardSectionGrouper sectionGrouper;
    private final DashboardPlannerAssembler dashboardPlannerAssembler;
    private final DashboardNotificationAssembler dashboardNotificationAssembler;

    public DashboardSectionsFactory(
            DashboardSectionGrouper sectionGrouper,
            DashboardPlannerAssembler dashboardPlannerAssembler,
            DashboardNotificationAssembler dashboardNotificationAssembler
    ) {
        this.sectionGrouper = sectionGrouper;
        this.dashboardPlannerAssembler = dashboardPlannerAssembler;
        this.dashboardNotificationAssembler = dashboardNotificationAssembler;
    }

    public DashboardSectionsDTO buildSections(
            List<QuestResponseDTO> myQuestDtos,
            List<QuestApplicationResponseDTO> sortedApplications,
            List<QuestNewsItemResponseDTO> recentNews,
            List<CircleRequestResponseDTO> incomingCircleRequests
    ) {
        List<QuestApplicationResponseDTO> activeWorkApplications = sortedApplications.stream()
                .filter(application -> application.getStatus() == QuestApplicationStatus.APPROVED)
                .filter(application -> {
                    QuestStatus questStatus = application.getQuestStatus();
                    return questStatus != null && questStatus.isActiveForWorker();
                })
                .toList();
        List<QuestResponseDTO> incomingWorkQuests = myQuestDtos.stream()
                .filter(quest -> quest.getStatus() != QuestStatus.COMPLETED && quest.getStatus() != QuestStatus.CANCELLED)
                .toList();
        List<QuestApplicationResponseDTO> outgoingWorkApplications = sortedApplications.stream()
                .filter(application -> application.getStatus() == QuestApplicationStatus.PENDING || application.getStatus() == QuestApplicationStatus.APPROVED)
                .toList();
        List<QuestResponseDTO> visibleMyQuests = myQuestDtos.stream()
                .filter(quest -> quest.getStatus() == QuestStatus.OPEN || quest.getStatus() == QuestStatus.WAITING_CONFIRMATION)
                .toList();
        List<QuestApplicationResponseDTO> visibleMyApplications = sortedApplications.stream()
                .filter(application -> application.getStatus() == QuestApplicationStatus.PENDING)
                .toList();
        List<QuestResponseDTO> waitingOpenWorkQuests = myQuestDtos.stream()
                .filter(quest -> quest.getStatus() == QuestStatus.WAITING_CONFIRMATION)
                .toList();
        List<QuestResponseDTO> openWorkQuests = myQuestDtos.stream()
                .filter(quest -> quest.getStatus() == QuestStatus.OPEN)
                .toList();

        return DashboardSectionsDTO.builder()
                .navigation(buildNavigationSection())
                .recentMyQuests(myQuestDtos.stream().limit(3).toList())
                .recentMyApplications(sortedApplications.stream().limit(3).toList())
                .activeWorkApplications(activeWorkApplications)
                .incomingWorkQuests(incomingWorkQuests)
                .outgoingWorkApplications(outgoingWorkApplications)
                .visibleMyQuests(visibleMyQuests)
                .visibleMyApplications(visibleMyApplications)
                .myQuestGroups(sectionGrouper.buildQuestGroups(myQuestDtos))
                .myApplicationGroups(sectionGrouper.buildApplicationGroups(sortedApplications))
                .recentIncomingCircleRequests(incomingCircleRequests.stream().limit(4).toList())
                .openWork(DashboardOpenWorkSectionDTO.builder()
                        .waitingQuests(waitingOpenWorkQuests)
                        .openQuests(openWorkQuests)
                        .build())
                .planner(dashboardPlannerAssembler.buildPlannerSection(incomingWorkQuests, outgoingWorkApplications))
                .notifications(buildNotificationsSection(recentNews))
                .build();
    }

    public DashboardSectionsDTO emptySections() {
        return DashboardSectionsDTO.builder()
                .navigation(buildNavigationSection())
                .recentMyQuests(List.of())
                .recentMyApplications(List.of())
                .activeWorkApplications(List.of())
                .incomingWorkQuests(List.of())
                .outgoingWorkApplications(List.of())
                .visibleMyQuests(List.of())
                .visibleMyApplications(List.of())
                .myQuestGroups(List.of())
                .myApplicationGroups(List.of())
                .recentIncomingCircleRequests(List.of())
                .openWork(DashboardOpenWorkSectionDTO.builder().waitingQuests(List.of()).openQuests(List.of()).build())
                .planner(DashboardPlannerSectionDTO.builder().scheduledItems(List.of()).flexibleItems(List.of()).build())
                .notifications(DashboardNotificationsSectionDTO.builder().unreadItems(List.of()).recentItems(List.of()).build())
                .build();
    }

    private DashboardNavigationSectionDTO buildNavigationSection() {
        return DashboardNavigationSectionDTO.builder()
                .tabs(List.of(
                        DashboardNavigationItemDTO.builder()
                                .id("calendar")
                                .title("Calendar")
                                .description("See scheduled work and open days")
                                .build(),
                        DashboardNavigationItemDTO.builder()
                                .id("side-job")
                                .title("SideJob")
                                .description("Offer jobs, track applications, and open the right flow when needed")
                                .build()
                ))
                .build();
    }

    private List<com.themuffinman.app.workmarket.dto.DashboardQuestGroupDTO> buildQuestGroups(List<QuestResponseDTO> quests) {
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

    private List<com.themuffinman.app.workmarket.dto.DashboardApplicationGroupDTO> buildApplicationGroups(List<QuestApplicationResponseDTO> applications) {
        return List.of(
                        buildApplicationGroup("APPROVED", "success", applications),
                        buildApplicationGroup("PENDING", "accent", applications),
                        buildApplicationGroup("DECLINED", "muted", applications),
                        buildApplicationGroup("WITHDRAWN", "muted", applications)
                ).stream()
                .filter(group -> group.getCount() > 0)
                .toList();
    }

    private com.themuffinman.app.workmarket.dto.DashboardQuestGroupDTO buildQuestGroup(
            String statusKey,
            String tone,
            List<QuestResponseDTO> quests
    ) {
        List<QuestResponseDTO> items = quests.stream()
                .filter(quest -> quest.getStatus() != null && quest.getStatus().name().equals(statusKey))
                .toList();
        String label = resolveQuestGroupLabel(statusKey, items);

        return com.themuffinman.app.workmarket.dto.DashboardQuestGroupDTO.builder()
                .key(statusKey)
                .label(label)
                .tone(tone)
                .count(items.size())
                .items(items)
                .build();
    }

    private com.themuffinman.app.workmarket.dto.DashboardApplicationGroupDTO buildApplicationGroup(
            String statusKey,
            String tone,
            List<QuestApplicationResponseDTO> applications
    ) {
        List<QuestApplicationResponseDTO> items = applications.stream()
                .filter(application -> application.getStatus() != null && application.getStatus().name().equals(statusKey))
                .toList();
        String label = resolveApplicationGroupLabel(statusKey, items);

        return com.themuffinman.app.workmarket.dto.DashboardApplicationGroupDTO.builder()
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
            default -> statusKey;
        };
    }

    private DashboardPlannerSectionDTO buildPlannerSection(
            List<QuestResponseDTO> incomingWorkQuests,
            List<QuestApplicationResponseDTO> outgoingWorkApplications
    ) {
        return dashboardPlannerAssembler.buildPlannerSection(incomingWorkQuests, outgoingWorkApplications);
    }

    private DashboardNotificationsSectionDTO buildNotificationsSection(List<QuestNewsItemResponseDTO> recentNews) {
        List<DashboardNotificationItemDTO> recentItems = dashboardNotificationAssembler.toRecentItems(recentNews);
        List<DashboardNotificationItemDTO> unreadItems = recentItems.stream()
                .filter(DashboardNotificationItemDTO::isUnread)
                .toList();

        return DashboardNotificationsSectionDTO.builder()
                .unreadItems(unreadItems)
                .recentItems(recentItems)
                .build();
    }
}
