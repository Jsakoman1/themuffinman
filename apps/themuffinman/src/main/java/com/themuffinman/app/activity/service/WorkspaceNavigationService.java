package com.themuffinman.app.activity.service;

import com.themuffinman.app.activity.dto.ActivityItemDTO;
import com.themuffinman.app.activity.dto.WorkspaceNavigationChildDTO;
import com.themuffinman.app.activity.dto.WorkspaceNavigationModuleDTO;
import com.themuffinman.app.activity.dto.WorkspaceNavigationResponseDTO;
import com.themuffinman.app.business.dto.BusinessFavoriteResponseDTO;
import com.themuffinman.app.business.service.BusinessFavoriteService;
import com.themuffinman.app.business.service.BusinessProfileService;
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
    private final BusinessFavoriteService businessFavoriteService;
    private final BusinessProfileService businessProfileService;

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
        List<BusinessFavoriteResponseDTO> favoriteBusinesses = businessFavoriteService.getMine(user);
        List<com.themuffinman.app.business.dto.BusinessProfileResponseDTO> ownedBusinesses = businessProfileService.getMyProfiles(user);
        List<ChildDefinition> serviceChildren = new java.util.ArrayList<>(List.of(
                child("services-find", "Find service", "/business/find", 1, "search public businesses")
        ));
        favoriteBusinesses.forEach(favorite -> serviceChildren.add(child(
                "service-favorite-" + favorite.getBusinessProfileId(),
                favorite.getBusinessName(),
                "/business/public/" + favorite.getSlug(),
                serviceChildren.size() + 1,
                "favorite business"
        )));
        return List.of(
                module("home", "Home", "home", "/home", 1, "workspace orientation", activityBySource, unreadBySource, List.of()),
                module("work", "Work", "work", "/work", 2, reason("workmarket", shortcuts, "primary work workspace"), activityBySource, unreadBySource,
                        List.of(child("work-my-work", "My work", "/work/quests", 1, "owned work and submitted work"), child("work-find", "Find work", "/work/find", 2, "work discovery"), child("work-applications", "Applications", "/work/applications", 3, "all incoming and outgoing applications"))),
                module("business", "Business", "business", "/business", 3, "your business spaces", activityBySource, unreadBySource,
                        ownedBusinesses.stream().map(business -> child(
                                "business-owned-" + business.getId(),
                                business.getBusinessName(),
                                "/business/profile?businessId=" + business.getId(),
                                ownedBusinesses.indexOf(business) + 1,
                                "owned business"
                        )).toList()),
                module("services", "Services", "business", "/business/find", 4, "discover businesses and services", activityBySource, unreadBySource,
                        serviceChildren),
                module("things", "Things", "things", "/things/mine", 5, "lending and borrowing", activityBySource, unreadBySource,
                        List.of(child("things-mine", "My things", "/things/mine", 1, "owned listings and borrow requests"), child("things-find", "Find things", "/things", 2, "search available listings"))),
                module("people", "People", "people", "/people", 6, "connections and circles", activityBySource, unreadBySource,
                        List.of(child("people-overview", "Overview", "/people", 1, "requests, friends, groups, and quick actions"), child("people-circles", "Circles", "/people/circles", 2, "manage groups"))),
                module("rides", "Rides", "rides", "/rides/mine", 7, "circle-scoped ride coordination", activityBySource, unreadBySource,
                        List.of(child("rides-mine", "My rides", "/rides/mine", 1, "rides you offer and riders who joined"), child("rides-find", "Find ride", "/rides", 2, "find an available ride")))
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
