package com.sidequest.sidequest.service;

import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.model.AppUserRole;
import com.sidequest.sidequest.model.Quest;
import com.sidequest.sidequest.model.QuestAudience;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class QuestVisibilityServiceTest {

    @Mock
    private CircleService circleService;

    @Test
    void allowsEveryoneAudienceForAnyUser() {
        QuestVisibilityService visibilityService = new QuestVisibilityService(circleService);

        AppUser viewer = createUser(1L, "viewer");
        AppUser creator = createUser(2L, "creator");
        Quest quest = new Quest();
        quest.setCreator(creator);
        quest.setAudience(QuestAudience.EVERYONE);

        assertEquals(true, visibilityService.canViewQuest(viewer, quest));
    }

    @Test
    void allowsCircleAudienceWhenUsersAreConnected() {
        QuestVisibilityService visibilityService = new QuestVisibilityService(circleService);

        AppUser viewer = createUser(1L, "viewer");
        AppUser creator = createUser(2L, "creator");
        Quest quest = new Quest();
        quest.setCreator(creator);
        quest.setAudience(QuestAudience.CIRCLES);

        org.mockito.Mockito.when(circleService.isCircleBetween(viewer, creator)).thenReturn(true);

        assertEquals(true, visibilityService.canViewQuest(viewer, quest));
    }

    @Test
    void allowsAdminRegardlessOfAudience() {
        QuestVisibilityService visibilityService = new QuestVisibilityService(circleService);

        AppUser admin = createUser(1L, "admin");
        admin.setRole(AppUserRole.ADMIN);
        AppUser creator = createUser(2L, "creator");
        Quest quest = new Quest();
        quest.setCreator(creator);
        quest.setAudience(QuestAudience.CIRCLES);

        assertEquals(true, visibilityService.canViewQuest(admin, quest));
    }

    private AppUser createUser(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setRole(AppUserRole.USER);
        return user;
    }
}
