package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserReadService;
import com.themuffinman.app.identity.service.AppUserService;
import com.themuffinman.app.identity.service.UserProfileViewService;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.social.service.CircleService;
import com.themuffinman.app.things.dto.ThingListingListResponseDTO;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.vision.dto.DashboardNotificationItemDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.vision.model.QuestNewsItem;
import com.themuffinman.app.vision.mapper.QuestNewsMgr;
import com.themuffinman.app.vision.service.QuestApplicationService;
import com.themuffinman.app.vision.service.QuestService;
import com.themuffinman.app.vision.service.QuestNewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisionCapabilityPreviewServiceAliasResolutionTest {

    @Mock
    private AppUserService appUserService;
    @Mock
    private AppUserReadService appUserReadService;
    @Mock
    private AppUserMgr appUserMgr;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private UserProfileViewService userProfileViewService;
    @Mock
    private ChatService chatService;
    @Mock
    private CircleReadService circleReadService;
    @Mock
    private CircleService circleService;
    @Mock
    private QuestService questService;
    @Mock
    private QuestApplicationService questApplicationService;
    @Mock
    private QuestNewsService questNewsService;
    @Mock
    private QuestNewsMgr questNewsMgr;
    @Mock
    private DashboardNotificationAssembler dashboardNotificationAssembler;
    @Mock
    private ThingSharingService thingSharingService;

    private VisionCapabilityPreviewService service;

    @BeforeEach
    void setUp() {
        service = new VisionCapabilityPreviewService(
                appUserService,
                appUserReadService,
                appUserMgr,
                appUserRepository,
                userProfileViewService,
                chatService,
                circleReadService,
                circleService,
                questService,
                questApplicationService,
                questNewsService,
                questNewsMgr,
                dashboardNotificationAssembler,
                thingSharingService,
                new SemanticAliasRegistry()
        );
    }

    @Test
    void resolveVisibleQuestUsesQuestAliases() {
        AppUser currentUser = new AppUser();
        QuestResponseDTO quest = QuestResponseDTO.builder()
                .id(42L)
                .title("Wash my luggage")
                .creatorUsername("Sasha")
                .build();
        when(questService.getAllQuestResponses(currentUser)).thenReturn(List.of(quest));

        VisionResolvedQuestTarget result = service.resolveVisibleQuest(currentUser, "wash my suitcases");

        assertTrue(result.resolved());
        assertEquals(42L, result.questId());
        assertEquals("Wash my luggage", result.questTitle());
    }

    @Test
    void resolveOwnedCircleUsesCircleAliases() {
        AppUser currentUser = new AppUser();
        CircleGroupResponseDTO circle = CircleGroupResponseDTO.builder()
                .id(7L)
                .name("Neighbour circle")
                .memberCount(3)
                .build();
        when(circleReadService.getCircles(currentUser)).thenReturn(List.of(circle));

        VisionResolvedCircleTarget result = service.resolveOwnedCircle(currentUser, "Neighbour group");

        assertTrue(result.resolved());
        assertEquals(7L, result.circleId());
        assertEquals("Neighbour circle", result.circleName());
    }

    @Test
    void resolveMyPendingApplicationUsesApplicationAliases() {
        AppUser currentUser = new AppUser();
        QuestApplicationResponseDTO application = QuestApplicationResponseDTO.builder()
                .id(11L)
                .questId(42L)
                .questTitle("Car repair application")
                .questCreatorUsername("Marta")
                .allowedActions(List.of(ApplicationAllowedActionDTO.EDIT))
                .build();
        when(questApplicationService.getApplicationsForApplicant(currentUser)).thenReturn(List.of(application));

        VisionResolvedApplicationTarget result = service.resolveMyPendingApplication(
                currentUser,
                "car repair request",
                ApplicationAllowedActionDTO.EDIT
        );

        assertTrue(result.resolved());
        assertEquals(11L, result.applicationId());
        assertEquals("Car repair application", result.questTitle());
    }

    @Test
    void previewThingsUsesAvailableListings() {
        AppUser currentUser = new AppUser();
        ThingListingResponseDTO listing = ThingListingResponseDTO.builder()
                .id(21L)
                .title("Sofa trolley")
                .description("Moves a sofa")
                .ownerUsername("alex")
                .available(true)
                .build();
        when(thingSharingService.getAvailableListings(currentUser)).thenReturn(
                ThingListingListResponseDTO.builder()
                        .items(List.of(listing))
                        .build()
        );

        var preview = service.previewThings(currentUser);

        assertEquals("view_things", preview.getCapabilityId());
        assertEquals("Things", preview.getTitle());
        assertTrue(preview.getSummary().contains("1 of 1"));
        assertEquals("Moves a sofa", preview.getItems().get(2).getValue());
    }

    @Test
    void previewNotificationsUsesInboxReadModel() {
        AppUser currentUser = new AppUser();
        QuestNewsItem unreadItem = new QuestNewsItem();
        unreadItem.setId(31L);
        unreadItem.setRecipientUserId(currentUser.getId());
        unreadItem.setTitle("New application");
        unreadItem.setMessage("Marta applied for your quest");
        unreadItem.setReadAt(null);
        QuestNewsItemResponseDTO unread = QuestNewsItemResponseDTO.builder()
                .id(31L)
                .title("New application")
                .message("Marta applied for your quest")
                .readAt(null)
                .build();
        QuestNewsItem readItem = new QuestNewsItem();
        readItem.setId(32L);
        readItem.setRecipientUserId(currentUser.getId());
        readItem.setTitle("Circle request");
        readItem.setMessage("Alex sent you a circle request");
        readItem.setReadAt(java.time.Instant.now());
        QuestNewsItemResponseDTO read = QuestNewsItemResponseDTO.builder()
                .id(32L)
                .title("Circle request")
                .message("Alex sent you a circle request")
                .readAt(java.time.Instant.now())
                .build();
        when(questNewsService.getMyNews(currentUser)).thenReturn(List.of(unreadItem, readItem));
        when(questNewsMgr.toDto(unreadItem)).thenReturn(unread);
        when(questNewsMgr.toDto(readItem)).thenReturn(read);
        when(dashboardNotificationAssembler.toRecentItems(List.of(unread, read))).thenReturn(List.of(
                DashboardNotificationItemDTO.builder().id(31L).title("New application").message("Marta applied for your quest").unread(true).typeLabel("New application").build(),
                DashboardNotificationItemDTO.builder().id(32L).title("Circle request").message("Alex sent you a circle request").unread(false).typeLabel("Circle request").build()
        ));

        var preview = service.previewNotifications(currentUser);

        assertEquals("view_notifications", preview.getCapabilityId());
        assertEquals("Notifications", preview.getTitle());
        assertTrue(preview.getSummary().contains("2 notifications"));
        assertTrue(preview.getSummary().contains("1 unread"));
        assertEquals("2", preview.getItems().get(0).getValue());
        assertEquals("1", preview.getItems().get(1).getValue());
        assertEquals("Marta applied for your quest", preview.getItems().get(2).getValue());
    }
}
