package com.themuffinman.app.activity;

import com.themuffinman.app.activity.dto.ActivityItemDTO;
import com.themuffinman.app.activity.service.ActivityReadService;
import com.themuffinman.app.activity.service.WorkspaceNavigationService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.PersonalShortcutService;
import com.themuffinman.app.notification.dto.AttentionCenterDTO;
import com.themuffinman.app.notification.service.AttentionCenterService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkspaceNavigationContractTest {
    @Test
    void returnsStableViewerScopedNavigationContract() {
        AppUser user = new AppUser();
        user.setId(7L);
        ActivityReadService activity = mock(ActivityReadService.class);
        AttentionCenterService attention = mock(AttentionCenterService.class);
        PersonalShortcutService shortcuts = mock(PersonalShortcutService.class);
        when(activity.getRecent(user)).thenReturn(List.of(ActivityItemDTO.builder().source("chat").readState("UNREAD").build()));
        when(attention.getMine(user)).thenReturn(AttentionCenterDTO.builder().unreadCount(1).items(List.of()).build());
        when(shortcuts.getMine(user)).thenReturn(List.of());

        WorkspaceNavigationResponseDTOAssertions result = new WorkspaceNavigationResponseDTOAssertions(
                new WorkspaceNavigationService(activity, attention, shortcuts).getNavigation(user)
        );

        assertThat(result.version()).isEqualTo("workspace-navigation-v1");
        assertThat(result.modules()).extracting("id").containsExactly("home", "work", "chat", "calendar", "business", "circles", "things", "rides");
        assertThat(result.module("chat").getRoute()).isEqualTo("/chat");
        assertThat(result.module("chat").getUnreadCount()).isEqualTo(1);
        assertThat(result.module("work").getChildren()).extracting("route").contains("/work/find", "/work/quests", "/work/applications");
        assertThat(result.module("rides").getChildren()).extracting("route").containsExactly("/rides", "/rides/mine");
        assertThat(result.modules()).allMatch(module -> module.isVisible() && module.getRelevanceReason() != null);
        assertThat(result.generatedAt()).isNotNull();
    }

    private record WorkspaceNavigationResponseDTOAssertions(com.themuffinman.app.activity.dto.WorkspaceNavigationResponseDTO value) {
        String version() { return value.getContractVersion(); }
        Instant generatedAt() { return value.getGeneratedAt(); }
        List<com.themuffinman.app.activity.dto.WorkspaceNavigationModuleDTO> modules() { return value.getModules(); }
        com.themuffinman.app.activity.dto.WorkspaceNavigationModuleDTO module(String id) { return modules().stream().filter(item -> item.getId().equals(id)).findFirst().orElseThrow(); }
    }
}
