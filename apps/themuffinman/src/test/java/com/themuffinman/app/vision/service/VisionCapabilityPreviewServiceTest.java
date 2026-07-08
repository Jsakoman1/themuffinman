package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserReadService;
import com.themuffinman.app.identity.service.AppUserService;
import com.themuffinman.app.identity.service.UserProfileViewService;
import com.themuffinman.app.location.dto.UserLocationSettingsDTO;
import com.themuffinman.app.location.model.ExactLocationVisibilityScope;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.social.service.CircleService;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.workmarket.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationPresentationDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestNewsMgr;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationReadService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisionCapabilityPreviewServiceTest {

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
    private WorkmarketQuestReadService questReadService;

    @Mock
    private WorkmarketQuestApplicationReadService questApplicationReadService;

    @Mock
    private WorkmarketQuestApplicationService questApplicationService;

    @Mock
    private WorkmarketQuestNewsService questNewsService;

    @Mock
    private WorkmarketQuestNewsMgr questNewsMgr;

    @Mock
    private DashboardNotificationAssembler dashboardNotificationAssembler;

    @Mock
    private ThingSharingService thingSharingService;

    @Mock
    private SemanticAliasRegistry semanticAliasRegistry;

    private VisionCapabilityPreviewService service;

    @BeforeEach
    void setUp() {
        VisionIdentityPreviewRenderer identityPreviewRenderer = new VisionIdentityPreviewRenderer(
                appUserService,
                appUserReadService,
                appUserMgr,
                appUserRepository,
                userProfileViewService,
                chatService
        );
        VisionFeedPreviewRenderer feedPreviewRenderer = new VisionFeedPreviewRenderer(
                questNewsService,
                questNewsMgr,
                dashboardNotificationAssembler,
                thingSharingService
        );
        VisionSocialPreviewRenderer socialPreviewRenderer = new VisionSocialPreviewRenderer(circleReadService);
        VisionSocialMutationAdapter socialMutationAdapter = new VisionSocialMutationAdapter(circleService);
        VisionProfilePreviewRenderer profilePreviewRenderer = new VisionProfilePreviewRenderer(
                appUserReadService,
                appUserMgr
        );
        VisionProfileMutationAdapter profileMutationAdapter = new VisionProfileMutationAdapter(
                appUserService,
                appUserReadService,
                appUserMgr
        );
        VisionCapabilityEntityResolutionSupport entityResolutionSupport = new VisionCapabilityEntityResolutionSupport(
                appUserRepository,
                appUserReadService,
                circleReadService,
                questApplicationReadService,
                questReadService,
                semanticAliasRegistry
        );
        VisionWorkmarketPreviewRenderer workmarketPreviewRenderer = new VisionWorkmarketPreviewRenderer(
                questReadService,
                questApplicationReadService
        );
        VisionWorkmarketApplicationMutationAdapter workmarketApplicationMutationAdapter =
                new VisionWorkmarketApplicationMutationAdapter(questApplicationService);
        service = new VisionCapabilityPreviewService(
                appUserService,
                appUserReadService,
                appUserMgr,
                appUserRepository,
                userProfileViewService,
                chatService,
                circleReadService,
                socialPreviewRenderer,
                socialMutationAdapter,
                profilePreviewRenderer,
                profileMutationAdapter,
                identityPreviewRenderer,
                feedPreviewRenderer,
                entityResolutionSupport,
                workmarketPreviewRenderer,
                workmarketApplicationMutationAdapter
        );
    }

    @Test
    void previewQuestDraftSurfacesRecognizedQuestFields() {
        VisionCapabilityPreviewDTO preview = service.previewQuestDraft(Map.of(
                "quest_title", "Move my sofa",
                "quest_description", "Need help moving the sofa in the living room",
                "reward_amount", "25",
                "visibility", "PUBLIC",
                "schedule_mode", "fixed",
                "scheduled_date", "2026-07-14",
                "scheduled_time", "19:00",
                "location_mode", "custom",
                "location_label", "Living room"
        ));

        assertNotNull(preview);
        assertEquals("create_quest", preview.getCapabilityId());
        assertEquals("Quest draft", preview.getTitle());
        assertEquals("Review the quest draft so far before confirmation.", preview.getSummary());
        assertNotNull(preview.getItems());
        assertEquals(9, preview.getItems().size());
        assertField(preview.getItems().get(0), "quest_title", "Title", "Move my sofa");
        assertField(preview.getItems().get(1), "quest_description", "Description", "Need help moving the sofa in the living room");
        assertField(preview.getItems().get(2), "reward_amount", "Reward", "25");
        assertField(preview.getItems().get(3), "visibility", "Visibility", "PUBLIC");
        assertField(preview.getItems().get(4), "schedule_mode", "Schedule", "Fixed time");
        assertField(preview.getItems().get(5), "scheduled_date", "Date", "2026-07-14");
        assertField(preview.getItems().get(6), "scheduled_time", "Time", "19:00");
        assertField(preview.getItems().get(7), "location_mode", "Location", "Custom place");
        assertField(preview.getItems().get(8), "location_label", "Custom place", "Living room");
    }

    @Test
    void previewQuestDraftUsesPartialSummaryWhenOnlyAFewFieldsAreFilled() {
        VisionCapabilityPreviewDTO preview = service.previewQuestDraft(Map.of(
                "quest_title", "Move my sofa",
                "location_mode", "custom"
        ));

        assertNotNull(preview);
        assertEquals("Review the quest draft so far. Continue adding the remaining fields.", preview.getSummary());
    }

    @Test
    void previewCircleDraftShowsAFormLikeSummary() {
        VisionCapabilityPreviewDTO preview = service.previewCircleDraft("Neighbours");

        assertNotNull(preview);
        assertEquals("create_circle", preview.getCapabilityId());
        assertEquals("Circle draft", preview.getTitle());
        assertEquals("Review the new circle before confirmation.", preview.getSummary());
        assertNotNull(preview.getItems());
        assertEquals(1, preview.getItems().size());
        assertField(preview.getItems().get(0), "circle_name", "Circle name", "Neighbours");
    }

    @Test
    void previewCreateCircleRequestDraftShowsAFormLikeSummary() {
        VisionCapabilityPreviewDTO preview = service.previewCreateCircleRequestDraft("Josip");

        assertNotNull(preview);
        assertEquals("create_circle_request", preview.getCapabilityId());
        assertEquals("Circle request draft", preview.getTitle());
        assertEquals("Review the connection invite before confirmation.", preview.getSummary());
        assertNotNull(preview.getItems());
        assertEquals(1, preview.getItems().size());
        assertField(preview.getItems().get(0), "target_user", "Person", "Josip");
    }

    @Test
    void previewApplicationDraftSurfacesRecognizedApplicationFields() {
        VisionCapabilityPreviewDTO preview = service.previewApplicationDraft(
                "Move my sofa",
                "Josip",
                "25",
                true,
                "I can help next Tuesday",
                "15"
        );

        assertNotNull(preview);
        assertEquals("create_application", preview.getCapabilityId());
        assertEquals("Application draft", preview.getTitle());
        assertEquals("Review the quest target, message, and proposed price before confirmation.", preview.getSummary());
        assertNotNull(preview.getItems());
        assertEquals(5, preview.getItems().size());
        assertField(preview.getItems().get(0), "target_quest_query", "Quest", "Move my sofa");
        assertField(preview.getItems().get(1), "application_quest_creator", "Posted by", "Josip");
        assertField(preview.getItems().get(2), "application_quest_reward", "Reward", "25");
        assertField(preview.getItems().get(3), "application_message", "Message", "I can help next Tuesday");
        assertField(preview.getItems().get(4), "application_proposed_price", "Proposed price", "15");
    }

    @Test
    void previewApplicationsUsesOneCompactRowPerApplication() {
        AppUser currentUser = new AppUser();
        QuestApplicationResponseDTO application = QuestApplicationResponseDTO.builder()
                .id(21L)
                .questId(42L)
                .questTitle("Move my sofa")
                .status(QuestApplicationStatus.PENDING)
                .allowedActions(List.of(ApplicationAllowedActionDTO.EDIT))
                .presentation(QuestApplicationPresentationDTO.builder()
                        .statusLabel("Pending")
                        .build())
                .build();
        when(questApplicationReadService.getApplicationsForApplicant(currentUser)).thenReturn(List.of(application));

        VisionCapabilityPreviewDTO preview = service.previewApplications(currentUser);

        assertNotNull(preview);
        assertEquals("view_applications", preview.getCapabilityId());
        assertEquals("Applications", preview.getTitle());
        assertEquals("1 application.", preview.getSummary());
        assertNotNull(preview.getItems());
        assertEquals("Applications", preview.getItems().get(0).getLabel());
        assertEquals("1", preview.getItems().get(0).getValue());
        assertEquals("Move my sofa", preview.getItems().get(3).getLabel());
        assertEquals("Pending · edit", preview.getItems().get(3).getValue());
    }

    @Test
    void previewProfileDraftShowsCurrentValuesAndChanges() {
        AppUser currentUser = new AppUser();
        currentUser.setId(7L);
        currentUser.setUsername("jsakoman");
        currentUser.setEmail("jsakoman@example.com");
        currentUser.setProfileDescription("Reliable mover");

        when(appUserReadService.getAppUser(7L)).thenReturn(currentUser);

        VisionCapabilityPreviewDTO preview = service.previewProfileDraft(currentUser, "jsakoman", "Reliable mover and organizer");

        assertNotNull(preview);
        assertEquals("update_profile", preview.getCapabilityId());
        assertEquals("Profile draft", preview.getTitle());
        assertEquals("Review 1 profile change before confirmation.", preview.getSummary());
        assertNotNull(preview.getItems());
        assertEquals(3, preview.getItems().size());
        assertField(preview.getItems().get(0), "profile_username", "Username", "jsakoman");
        assertField(preview.getItems().get(1), "profile_description", "Profile description", "Reliable mover and organizer");
        assertField(preview.getItems().get(2), "profile_email", "Email", "jsakoman@example.com");
    }

    @Test
    void previewProfileLocationDraftShowsCurrentLocationSettings() {
        AppUser currentUser = new AppUser();
        currentUser.setId(7L);

        AppUser existingUser = new AppUser();
        existingUser.setId(7L);
        existingUser.setUsername("jsakoman");
        existingUser.setEmail("jsakoman@example.com");
        existingUser.setLocationMode(UserLocationMode.APPROXIMATE);
        existingUser.setLocationLabel("Zagreb");
        existingUser.setLocationCountry("Croatia");
        existingUser.setLocationCountryCode("HR");
        existingUser.setLocationLocality("Zagreb");
        existingUser.setLocationLatitude(null);
        existingUser.setLocationLongitude(null);
        existingUser.setLocationRadiusKm(15);
        existingUser.setExactLocationVisibilityScope(ExactLocationVisibilityScope.NOBODY);

        when(appUserReadService.getAppUser(7L)).thenReturn(existingUser);
        when(appUserMgr.toDto(existingUser)).thenReturn(AppUserResponseDTO.builder()
                .id(7L)
                .username("jsakoman")
                .email("jsakoman@example.com")
                .locationSettings(UserLocationSettingsDTO.builder()
                        .mode(UserLocationMode.APPROXIMATE)
                        .label("Zagreb")
                        .build())
                .build());

        VisionCapabilityPreviewDTO preview = service.previewProfileLocationDraft(currentUser, "exact", "Home");

        assertNotNull(preview);
        assertEquals("update_profile_location", preview.getCapabilityId());
        assertEquals("Profile location draft", preview.getTitle());
        assertEquals("Review the profile location changes before confirmation.", preview.getSummary());
        assertNotNull(preview.getItems());
        assertEquals(2, preview.getItems().size());
        assertField(preview.getItems().get(0), "profile_location_mode", "Location mode", "exact");
        assertField(preview.getItems().get(1), "profile_location_label", "Location", "Home");
    }

    @Test
    void previewUpdateApplicationDraftShowsCurrentAndDraftValues() {
        VisionCapabilityPreviewDTO preview = service.previewUpdateApplicationDraft(
                "Move my sofa",
                "Josip",
                "25",
                true,
                "I can help next Tuesday",
                "15",
                "I can help on Wednesday",
                "20"
        );

        assertNotNull(preview);
        assertEquals("update_application", preview.getCapabilityId());
        assertEquals("Application update draft", preview.getTitle());
        assertEquals("Review the application changes before confirmation. Unchanged values will be kept.", preview.getSummary());
        assertNotNull(preview.getItems());
        assertEquals(7, preview.getItems().size());
        assertField(preview.getItems().get(0), "target_quest_query", "Quest", "Move my sofa");
        assertField(preview.getItems().get(1), "application_quest_creator", "Posted by", "Josip");
        assertField(preview.getItems().get(2), "application_quest_reward", "Reward", "25");
        assertField(preview.getItems().get(3), "application_existing_message", "Current message", "I can help next Tuesday");
        assertField(preview.getItems().get(4), "application_message", "New message", "I can help on Wednesday");
        assertField(preview.getItems().get(5), "application_existing_proposed_price", "Current price", "15");
        assertField(preview.getItems().get(6), "application_proposed_price", "New price", "20");
    }

    @Test
    void previewWithdrawApplicationDraftShowsExistingApplicationDetails() {
        VisionCapabilityPreviewDTO preview = service.previewWithdrawApplicationDraft(
                "Move my sofa",
                "Josip",
                "25",
                "I can help next Tuesday",
                "15"
        );

        assertNotNull(preview);
        assertEquals("withdraw_application", preview.getCapabilityId());
        assertEquals("Application withdrawal review", preview.getTitle());
        assertEquals("Review the pending application you are about to withdraw.", preview.getSummary());
        assertNotNull(preview.getItems());
        assertEquals(5, preview.getItems().size());
        assertField(preview.getItems().get(0), "target_quest_query", "Quest", "Move my sofa");
        assertField(preview.getItems().get(1), "application_quest_creator", "Posted by", "Josip");
        assertField(preview.getItems().get(2), "application_quest_reward", "Reward", "25");
        assertField(preview.getItems().get(3), "application_existing_message", "Current message", "I can help next Tuesday");
        assertField(preview.getItems().get(4), "application_existing_proposed_price", "Current price", "15");
    }

    @Test
    void previewManagedApplicationDecisionDraftShowsDecisionContext() {
        VisionCapabilityPreviewDTO preview = service.previewManagedApplicationDecisionDraft(
                "approve_application",
                "Application approval review",
                "Review the pending application you are about to approve.",
                "Move my sofa",
                "Maja",
                "I can help next Tuesday",
                "15"
        );

        assertNotNull(preview);
        assertEquals("approve_application", preview.getCapabilityId());
        assertEquals("Application approval review", preview.getTitle());
        assertEquals("Review the pending application you are about to approve.", preview.getSummary());
        assertNotNull(preview.getItems());
        assertEquals(4, preview.getItems().size());
        assertField(preview.getItems().get(0), "target_quest_query", "Quest", "Move my sofa");
        assertField(preview.getItems().get(1), "target_user", "Applicant", "Maja");
        assertField(preview.getItems().get(2), "application_existing_message", "Message", "I can help next Tuesday");
        assertField(preview.getItems().get(3), "application_existing_proposed_price", "Proposed price", "15");
    }

    private static void assertField(VisionSlotSummaryDTO field, String slotId, String label, String value) {
        assertNotNull(field);
        assertEquals(slotId, field.getSlotId());
        assertEquals(label, field.getLabel());
        assertEquals(value, field.getValue());
    }
}
