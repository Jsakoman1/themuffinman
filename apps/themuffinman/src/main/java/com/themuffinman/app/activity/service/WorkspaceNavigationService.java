package com.themuffinman.app.activity.service;

import com.themuffinman.app.activity.dto.ActivityItemDTO;
import com.themuffinman.app.activity.dto.WorkspaceNavigationChildDTO;
import com.themuffinman.app.activity.dto.WorkspaceNavigationModuleDTO;
import com.themuffinman.app.activity.dto.WorkspaceNavigationResponseDTO;
import com.themuffinman.app.identity.dto.PersonalShortcutResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.PersonalShortcutService;
import com.themuffinman.app.notification.dto.AttentionCenterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceNavigationService {
    private static final String CONTRACT_VERSION = "workspace-navigation-v1";
    private static final int REFRESH_AFTER_SECONDS = 30;

    private final ActivityReadService activityReadService;
    private final com.themuffinman.app.notification.service.AttentionCenterService attentionCenterService;
    private final PersonalShortcutService personalShortcutService;

    public WorkspaceNavigationResponseDTO getNavigation(AppUser user) {
        if (user == null) {
            throw new IllegalArgumentException("Authenticated viewer is required for workspace navigation");
        }
        AttentionCenterDTO attention = attentionCenterService.getMine(user);
        List<ActivityItemDTO> activity = activityReadService.getRecent(user);
        List<PersonalShortcutResponseDTO> shortcuts = personalShortcutService.getMine(user);
        Map<String, Long> activityBySource = (activity == null ? List.<ActivityItemDTO>of() : activity).stream()
                .filter(item -> item.getSource() != null)
                .collect(Collectors.groupingBy(ActivityItemDTO::getSource, Collectors.counting()));
        Map<String, Long> unreadBySource = (activity == null ? List.<ActivityItemDTO>of() : activity).stream()
                .filter(item -> item.getSource() != null)
                .filter(item -> "UNREAD".equalsIgnoreCase(item.getReadState()))
                .collect(Collectors.groupingBy(ActivityItemDTO::getSource, Collectors.counting()));

        return WorkspaceNavigationResponseDTO.builder()
                .contractVersion(CONTRACT_VERSION)
                .generatedAt(Instant.now())
                .refreshAfterSeconds(REFRESH_AFTER_SECONDS)
                .unreadCount(attention == null ? 0 : attention.getUnreadCount())
                .modules(buildModules(activityBySource, unreadBySource, shortcuts))
                .build();
    }

    private List<WorkspaceNavigationModuleDTO> buildModules(
            Map<String, Long> activityBySource,
            Map<String, Long> unreadBySource,
            List<PersonalShortcutResponseDTO> shortcuts
    ) {
        return List.of(
                module("home", "Home", "home", "/home", 1, "workspace orientation", activityBySource, unreadBySource, List.of()),
                module("work", "Work", "work", "/work", 2, reason("workmarket", shortcuts, "primary work workspace"), activityBySource, unreadBySource,
                        List.of(child("work-find", "Find work", "/work/find", 1, "work discovery"), child("work-quests", "My work", "/work/quests", 2, "owned work"), child("work-applications", "My applications", "/work/applications", 3, "submitted work applications"))),
                module("chat", "Chat", "chat", "/chat", 3, "conversations and coordination", activityBySource, unreadBySource,
                        List.of()),
                module("calendar", "Calendar", "calendar", "/calendar", 4, "time coordination", activityBySource, unreadBySource, List.of()),
                module("business", "Business", "business", "/business", 5, "owner operations and discovery", activityBySource, unreadBySource,
                        List.of(child("business-profile", "Profile", "/business/profile", 1, "business identity"), child("business-bookings", "Bookings", "/business/bookings", 2, "booking operations"), child("business-calendar", "Calendar", "/business/calendar", 3, "business availability"))),
                module("circles", "Circles", "circles", "/circles", 6, "trust and visibility", activityBySource, unreadBySource,
                        List.of(child("circles-groups", "Groups", "/circles", 1, "circle membership"), child("circles-people", "People", "/people", 2, "trusted people"))),
                module("things", "Things", "things", "/things", 7, "lending and borrowing", activityBySource, unreadBySource,
                        List.of(child("things-discover", "Discover", "/things", 1, "available listings"), child("things-mine", "My things", "/things/mine", 2, "owned listings"))),
                module("rides", "Rides", "rides", "/rides", 8, "circle-scoped ride coordination", activityBySource, unreadBySource,
                        List.of(child("rides-discover", "Discover", "/rides", 1, "available rides"), child("rides-mine", "My rides", "/rides/mine", 2, "owned rides")))
        );
    }

    private WorkspaceNavigationModuleDTO module(
            String id,
            String label,
            String iconKey,
            String route,
            int order,
            String relevanceReason,
            Map<String, Long> activityBySource,
            Map<String, Long> unreadBySource,
            List<ChildDefinition> children
    ) {
        String source = sourceFor(id);
        long attention = activityBySource.getOrDefault(source, 0L);
        long unread = unreadBySource.getOrDefault(source, 0L);
        return WorkspaceNavigationModuleDTO.builder()
                .id(id)
                .label(label)
                .iconKey(iconKey)
                .route(route)
                .order(order)
                .visible(true)
                .attentionCount(attention)
                .unreadCount(unread)
                .relevanceReason(relevanceReason)
                .children(children.stream().map(child -> WorkspaceNavigationChildDTO.builder()
                        .id(child.id())
                        .label(child.label())
                        .route(child.route())
                        .order(child.order())
                        .visible(true)
                        .attentionCount(attention)
                        .unreadCount(unread)
                        .relevanceReason(child.reason())
                        .build()).toList())
                .build();
    }

    private String sourceFor(String moduleId) {
        return switch (moduleId) {
            case "work" -> "workmarket";
            case "chat" -> "chat";
            case "things" -> "things";
            case "home" -> "workmarket";
            default -> moduleId;
        };
    }

    private String reason(String source, List<PersonalShortcutResponseDTO> shortcuts, String fallback) {
        return shortcuts.stream().anyMatch(shortcut -> shortcut.route() != null && shortcut.route().startsWith("/work/"))
                ? "pinned work and recent activity"
                : fallback;
    }

    private ChildDefinition child(String id, String label, String route, int order, String reason) {
        return new ChildDefinition(id, label, route, order, reason);
    }

    private record ChildDefinition(String id, String label, String route, int order, String reason) { }
}
