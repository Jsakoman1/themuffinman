package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.workmarket.dto.DashboardNotificationDestinationType;
import com.themuffinman.app.workmarket.dto.DashboardNotificationItemDTO;
import com.themuffinman.app.workmarket.dto.DashboardNotificationsSectionDTO;
import com.themuffinman.app.workmarket.dto.DashboardOpenWorkSectionDTO;
import com.themuffinman.app.workmarket.dto.DashboardOverviewSectionDTO;
import com.themuffinman.app.workmarket.dto.DashboardPlannerItemDTO;
import com.themuffinman.app.workmarket.dto.DashboardPlannerSectionDTO;
import com.themuffinman.app.workmarket.dto.DashboardRailBucketDTO;
import com.themuffinman.app.workmarket.dto.DashboardRailItemDTO;
import com.themuffinman.app.workmarket.dto.DashboardSectionsDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardSectionsFactory {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Zurich");
    private static final DateTimeFormatter RAIL_DAY_FORMATTER = DateTimeFormatter.ofPattern("d MMM").withZone(DEFAULT_ZONE);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm").withZone(DEFAULT_ZONE);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM HH:mm").withZone(DEFAULT_ZONE);
    private static final DateTimeFormatter DATE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(DEFAULT_ZONE);

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
                .recentMyQuests(myQuestDtos.stream().limit(3).toList())
                .recentMyApplications(sortedApplications.stream().limit(3).toList())
                .activeWorkApplications(activeWorkApplications)
                .incomingWorkQuests(incomingWorkQuests)
                .outgoingWorkApplications(outgoingWorkApplications)
                .visibleMyQuests(visibleMyQuests)
                .visibleMyApplications(visibleMyApplications)
                .recentIncomingCircleRequests(incomingCircleRequests.stream().limit(4).toList())
                .overview(buildOverviewSection(myQuestDtos, sortedApplications, activeWorkApplications))
                .openWork(DashboardOpenWorkSectionDTO.builder()
                        .waitingQuests(waitingOpenWorkQuests)
                        .openQuests(openWorkQuests)
                        .build())
                .planner(buildPlannerSection(incomingWorkQuests, outgoingWorkApplications))
                .notifications(buildNotificationsSection(recentNews))
                .build();
    }

    public DashboardSectionsDTO emptySections() {
        return DashboardSectionsDTO.builder()
                .recentMyQuests(List.of())
                .recentMyApplications(List.of())
                .activeWorkApplications(List.of())
                .incomingWorkQuests(List.of())
                .outgoingWorkApplications(List.of())
                .visibleMyQuests(List.of())
                .visibleMyApplications(List.of())
                .recentIncomingCircleRequests(List.of())
                .overview(DashboardOverviewSectionDTO.builder().postedBuckets(List.of()).workBuckets(List.of()).build())
                .openWork(DashboardOpenWorkSectionDTO.builder().waitingQuests(List.of()).openQuests(List.of()).build())
                .planner(DashboardPlannerSectionDTO.builder().scheduledItems(List.of()).flexibleItems(List.of()).build())
                .notifications(DashboardNotificationsSectionDTO.builder().unreadItems(List.of()).recentItems(List.of()).build())
                .build();
    }

    private DashboardOverviewSectionDTO buildOverviewSection(
            List<QuestResponseDTO> myQuestDtos,
            List<QuestApplicationResponseDTO> sortedApplications,
            List<QuestApplicationResponseDTO> activeWorkApplications
    ) {
        return DashboardOverviewSectionDTO.builder()
                .postedBuckets(List.of(
                        buildQuestBucket("posted-open", "Open", "accent", myQuestDtos, QuestStatus.OPEN),
                        buildQuestBucket("posted-waiting", "Need confirmation", "warning", myQuestDtos, QuestStatus.WAITING_CONFIRMATION),
                        buildQuestBucket("posted-assigned", "Ready to start", "success", myQuestDtos, QuestStatus.ASSIGNED),
                        buildQuestBucket("posted-progress", "In progress", "accent", myQuestDtos, QuestStatus.IN_PROGRESS),
                        buildQuestBucket("posted-completed", "Completed", "success", myQuestDtos, QuestStatus.COMPLETED)
                ))
                .workBuckets(List.of(
                        buildApplicationStatusBucket("work-applied", "Applied", "accent", sortedApplications, QuestApplicationStatus.PENDING),
                        buildApplicationBucket("work-waiting", "Waiting on agreement", "warning", activeWorkApplications, QuestStatus.WAITING_CONFIRMATION),
                        buildApplicationBucket("work-assigned", "Agreed and ready", "success", activeWorkApplications, QuestStatus.ASSIGNED),
                        buildApplicationBucket("work-progress", "Doing now", "accent", activeWorkApplications, QuestStatus.IN_PROGRESS),
                        buildApplicationStatusAndQuestBucket("work-completed", "Completed", "success", sortedApplications, QuestApplicationStatus.APPROVED, QuestStatus.COMPLETED)
                ))
                .build();
    }

    private DashboardPlannerSectionDTO buildPlannerSection(
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

    private DashboardNotificationsSectionDTO buildNotificationsSection(List<QuestNewsItemResponseDTO> recentNews) {
        List<DashboardNotificationItemDTO> recentItems = recentNews.stream()
                .map(this::toDashboardNotificationItem)
                .toList();
        List<DashboardNotificationItemDTO> unreadItems = recentItems.stream()
                .filter(DashboardNotificationItemDTO::isUnread)
                .toList();

        return DashboardNotificationsSectionDTO.builder()
                .unreadItems(unreadItems)
                .recentItems(recentItems)
                .build();
    }

    private DashboardRailBucketDTO buildQuestBucket(
            String key,
            String label,
            String tone,
            List<QuestResponseDTO> quests,
            QuestStatus status
    ) {
        return DashboardRailBucketDTO.builder()
                .key(key)
                .label(label)
                .tone(tone)
                .items(quests.stream()
                        .filter(quest -> quest.getStatus() == status)
                        .map(this::toQuestRailItem)
                        .toList())
                .build();
    }

    private DashboardRailBucketDTO buildApplicationBucket(
            String key,
            String label,
            String tone,
            List<QuestApplicationResponseDTO> applications,
            QuestStatus questStatus
    ) {
        return DashboardRailBucketDTO.builder()
                .key(key)
                .label(label)
                .tone(tone)
                .items(applications.stream()
                        .filter(application -> application.getQuestStatus() == questStatus)
                        .map(this::toApplicationRailItem)
                        .toList())
                .build();
    }

    private DashboardRailBucketDTO buildApplicationStatusBucket(
            String key,
            String label,
            String tone,
            List<QuestApplicationResponseDTO> applications,
            QuestApplicationStatus applicationStatus
    ) {
        return DashboardRailBucketDTO.builder()
                .key(key)
                .label(label)
                .tone(tone)
                .items(applications.stream()
                        .filter(application -> application.getStatus() == applicationStatus)
                        .map(this::toApplicationRailItem)
                        .toList())
                .build();
    }

    private DashboardRailBucketDTO buildApplicationStatusAndQuestBucket(
            String key,
            String label,
            String tone,
            List<QuestApplicationResponseDTO> applications,
            QuestApplicationStatus applicationStatus,
            QuestStatus questStatus
    ) {
        return DashboardRailBucketDTO.builder()
                .key(key)
                .label(label)
                .tone(tone)
                .items(applications.stream()
                        .filter(application -> application.getStatus() == applicationStatus)
                        .filter(application -> application.getQuestStatus() == questStatus)
                        .map(this::toApplicationRailItem)
                        .toList())
                .build();
    }

    private DashboardRailItemDTO toQuestRailItem(QuestResponseDTO quest) {
        return DashboardRailItemDTO.builder()
                .id("quest-" + quest.getId())
                .questId(quest.getId())
                .title(quest.getTitle())
                .whenLabel(formatRailDateTime(quest.getScheduledAt(), quest.getEndsAt()))
                .navigation(quest.getQuestNavigation())
                .build();
    }

    private DashboardRailItemDTO toApplicationRailItem(QuestApplicationResponseDTO application) {
        return DashboardRailItemDTO.builder()
                .id("application-" + application.getId())
                .questId(application.getQuestId())
                .title(application.getQuestTitle())
                .whenLabel(formatRailDateTime(application.getQuestScheduledAt(), application.getQuestEndsAt()))
                .navigation(application.getQuestNavigation())
                .build();
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
                    .timeLabel("All day")
                    .dateKey(null)
                    .kind(kind)
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
                .timeLabel(formatTimeLabel(scheduledAt, endsAt))
                .dateKey(DATE_KEY_FORMATTER.format(scheduledAt))
                .kind(kind)
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

    private DashboardNotificationItemDTO toDashboardNotificationItem(QuestNewsItemResponseDTO item) {
        DashboardNotificationDestinationType destinationType = DashboardNotificationDestinationType.QUEST_LIST;
        Long destinationId = null;

        if (item.getApplicationId() != null) {
            destinationType = DashboardNotificationDestinationType.APPLICATION;
            destinationId = item.getApplicationId();
        } else if (item.getQuestId() != null) {
            destinationType = DashboardNotificationDestinationType.QUEST;
            destinationId = item.getQuestId();
        }

        return DashboardNotificationItemDTO.builder()
                .id(item.getId())
                .type(item.getType())
                .typeLabel(item.getTypeLabel())
                .badgeClass(item.getBadgeClass())
                .title(item.getTitle())
                .message(item.getMessage())
                .actorUsername(item.getActorUsername())
                .questTitle(item.getQuestTitle())
                .questId(item.getQuestId())
                .applicationId(item.getApplicationId())
                .createdAt(item.getCreatedAt())
                .readAt(item.getReadAt())
                .destinationType(destinationType)
                .destinationId(destinationId)
                .navigation(item.getNavigation())
                .unread(item.getReadAt() == null)
                .build();
    }

    private String formatRailDateTime(Instant startValue, Instant endValue) {
        if (startValue == null) {
            return "By agreement";
        }

        String dateLabel = RAIL_DAY_FORMATTER.format(startValue);
        String startTimeLabel = TIME_FORMATTER.format(startValue);

        if (endValue == null) {
            return dateLabel + " · " + startTimeLabel;
        }

        boolean sameDay = startValue.atZone(DEFAULT_ZONE).toLocalDate().equals(endValue.atZone(DEFAULT_ZONE).toLocalDate());
        String endLabel = sameDay ? TIME_FORMATTER.format(endValue) : DATE_TIME_FORMATTER.format(endValue);
        return dateLabel + " · " + startTimeLabel + "-" + endLabel;
    }

    private String formatTimeLabel(Instant startValue, Instant endValue) {
        if (startValue == null) {
            return "All day";
        }

        String startLabel = TIME_FORMATTER.format(startValue);
        if (endValue == null) {
            return startLabel;
        }

        return startLabel + "-" + TIME_FORMATTER.format(endValue);
    }
}
