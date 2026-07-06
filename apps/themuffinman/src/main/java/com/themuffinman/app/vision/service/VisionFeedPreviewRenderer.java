package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.things.dto.ThingListingListResponseDTO;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.dto.DashboardNotificationItemDTO;
import com.themuffinman.app.vision.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.vision.mapper.QuestNewsMgr;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
class VisionFeedPreviewRenderer {

    private final QuestNewsService questNewsService;
    private final QuestNewsMgr questNewsMgr;
    private final DashboardNotificationAssembler dashboardNotificationAssembler;
    private final ThingSharingService thingSharingService;

    VisionCapabilityPreviewDTO previewQuestNews(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        List<QuestNewsItemResponseDTO> newsItems = questNewsService.getMyNews(currentUser).stream()
                .map(questNewsMgr::toDto)
                .toList();

        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        VisionCapabilityPreviewSupport.addItem(items, "news_count", "Updates", String.valueOf(newsItems.size()));
        long unreadCount = newsItems.stream().filter(item -> item.getReadAt() == null).count();
        VisionCapabilityPreviewSupport.addItem(items, "news_unread", "Unread", String.valueOf(unreadCount));
        for (int index = 0; index < Math.min(newsItems.size(), 4); index++) {
            QuestNewsItemResponseDTO item = newsItems.get(index);
            VisionCapabilityPreviewSupport.addItem(items, "news_" + item.getId(), item.getTitle(), item.getMessage());
        }

        String summary = newsItems.isEmpty()
                ? "No updates."
                : newsItems.size() + " update" + (newsItems.size() == 1 ? "" : "s") + ".";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_quest_news")
                .title("Quest news")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewNotifications(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        List<QuestNewsItemResponseDTO> newsItems = questNewsService.getMyNews(currentUser).stream()
                .map(questNewsMgr::toDto)
                .toList();
        List<DashboardNotificationItemDTO> recentItems = dashboardNotificationAssembler.toRecentItems(newsItems);
        List<DashboardNotificationItemDTO> unreadItems = recentItems.stream()
                .filter(DashboardNotificationItemDTO::isUnread)
                .toList();

        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        VisionCapabilityPreviewSupport.addItem(items, "notifications_count", "Notifications", String.valueOf(recentItems.size()));
        VisionCapabilityPreviewSupport.addItem(items, "notifications_unread", "Unread", String.valueOf(unreadItems.size()));
        for (int index = 0; index < Math.min(unreadItems.size(), 3); index++) {
            DashboardNotificationItemDTO item = unreadItems.get(index);
            VisionCapabilityPreviewSupport.addItem(items, "notification_unread_" + item.getId(), item.getTitle(), item.getMessage());
        }
        for (int index = 0; index < Math.min(recentItems.size(), 3); index++) {
            DashboardNotificationItemDTO item = recentItems.get(index);
            VisionCapabilityPreviewSupport.addItem(items, "notification_recent_" + item.getId(), item.getTypeLabel(), item.getMessage());
        }

        String summary = recentItems.isEmpty()
                ? "No notifications."
                : recentItems.size() + " notification" + (recentItems.size() == 1 ? "" : "s")
                + ", " + unreadItems.size() + " unread.";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_notifications")
                .title("Notifications")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewThings(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        ThingListingListResponseDTO listings = thingSharingService.getAvailableListings(currentUser);
        List<ThingListingResponseDTO> itemsSource = listings == null || listings.getItems() == null ? List.of() : listings.getItems();
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        VisionCapabilityPreviewSupport.addItem(items, "things_count", "Things", String.valueOf(itemsSource.size()));
        long availableCount = itemsSource.stream().filter(ThingListingResponseDTO::isAvailable).count();
        VisionCapabilityPreviewSupport.addItem(items, "things_available", "Available", String.valueOf(availableCount));
        for (int index = 0; index < Math.min(itemsSource.size(), 4); index++) {
            ThingListingResponseDTO listing = itemsSource.get(index);
            VisionCapabilityPreviewSupport.addItem(items, "thing_" + listing.getId(), listing.getTitle(),
                    VisionCapabilityPreviewSupport.thingListingValue(listing));
        }

        String summary = itemsSource.isEmpty()
                ? "No things."
                : availableCount + " of " + itemsSource.size() + " thing" + (itemsSource.size() == 1 ? "" : "s")
                + " available.";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_things")
                .title("Things")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }
}
