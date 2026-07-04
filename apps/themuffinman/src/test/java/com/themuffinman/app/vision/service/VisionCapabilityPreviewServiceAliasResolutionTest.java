package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserService;
import com.themuffinman.app.identity.service.UserProfileViewService;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.service.CircleService;
import com.themuffinman.app.vision.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
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
    private AppUserMgr appUserMgr;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private UserProfileViewService userProfileViewService;
    @Mock
    private ChatService chatService;
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

    private VisionCapabilityPreviewService service;

    @BeforeEach
    void setUp() {
        service = new VisionCapabilityPreviewService(
                appUserService,
                appUserMgr,
                appUserRepository,
                userProfileViewService,
                chatService,
                circleService,
                questService,
                questApplicationService,
                questNewsService,
                questNewsMgr,
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
        when(circleService.getCircles(currentUser)).thenReturn(List.of(circle));

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
}
