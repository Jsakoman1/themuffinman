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
                .modules(buildModules(user, activityBySource, unreadBySource, shortcuts))
                .build();
    }

    private List<WorkspaceNavigationModuleDTO> buildModules(
            AppUser user,
            Map<String, Long> activityBySource,
            Map<String, Long> unreadBySource,
            List<PersonalShortcutResponseDTO> shortcuts
    ) {
        return List.of(
                module("home", "Home", "home", "/home", 1, "workspace orientation", activityBySource, unreadBySource, List.of()),
                module("work", "Work", "work", "/work", 2, reason("workmarket", shortcuts, "primary work workspace"), activityBySource, unreadBySource,
                        List.of()),
                module("business", "Business", "business", "/business", 3, "your business spaces", activityBySource, unreadBySource,
                        List.of()),
                module("services", "Services", "business", "/business/find", 4, "discover businesses and services", activityBySource, unreadBySource,
                        List.of()),
                module("things", "Things", "things", "/things/mine", 5, "lending and borrowing", activityBySource, unreadBySource,
                        List.of()),
                module("people", "People", "people", "/people", 6, "connections and circles", activityBySource, unreadBySource,
                        List.of()),
                module("rides", "Rides", "rides", "/rides/mine", 7, "circle-scoped ride coordination", activityBySource, unreadBySource,
                        List.of())
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
