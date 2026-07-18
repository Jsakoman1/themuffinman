package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.things.dto.ThingListingListResponseDTO;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.dto.VisionSearchDiscoveryDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestListPresetDTO;
import com.themuffinman.app.workmarket.dto.QuestListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationReadService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VisionSearchDiscoveryServiceTest {

    @Mock
    private WorkmarketQuestReadService questReadService;

    @Mock
    private CircleReadService circleReadService;

    @Mock
    private ThingSharingService thingSharingService;

    @Mock
    private WorkmarketQuestApplicationReadService questApplicationService;

    @Mock
    private AppUserRepository appUserRepository;

    private VisionSearchDiscoveryService service;
    private AppUser currentUser;
    private SemanticAliasRegistry semanticAliasRegistry;

    @BeforeEach
    void setUp() {
        semanticAliasRegistry = new SemanticAliasRegistry();
        service = new VisionSearchDiscoveryService(
                questReadService,
                circleReadService,
                thingSharingService,
                questApplicationService,
                appUserRepository,
                semanticAliasRegistry
        );
        currentUser = new AppUser();
        currentUser.setId(7L);
        currentUser.setUsername("vision-user");

        when(circleReadService.getCircles(currentUser)).thenReturn(List.of(
                CircleGroupResponseDTO.builder()
                        .id(40L)
                        .name("Sofa Helpers")
                        .resolutionLabel("Sofa Helpers")
                        .exactResolutionEligible(true)
                        .memberCount(4)
                        .memberPreviewLabel("Move sofa together")
                        .build()
        ));
        when(questApplicationService.getApplicationsForApplicant(currentUser)).thenReturn(List.of(
                QuestApplicationResponseDTO.builder()
                        .id(70L)
                        .questTitle("Move sofa support")
                        .message("Need help moving a sofa")
                        .build()
        ));
        when(thingSharingService.getAvailableListings(currentUser)).thenReturn(
                ThingListingListResponseDTO.builder()
                        .items(List.of(
                                ThingListingResponseDTO.builder()
                                        .id(90L)
                                        .ownerUsername("alex")
                                        .title("Sofa trolley")
                                        .description("Moves a sofa easily")
                                        .available(true)
                                        .build()
                        ))
                        .build()
        );
        when(appUserRepository.searchByUsername("move sofa")).thenReturn(List.of(
                user(50L, "movesofa", "move@sofa.test")
        ));
        when(appUserRepository.searchByUsername("helper")).thenReturn(List.of(
                user(51L, "helper", "helper@test.local")
        ));
    }

    @Test
    void ranksExactMatchesAheadOfBroaderMatchesAndNormalizesTheQuery() {
        when(questReadService.getQuestListPreset(
                any(QuestListPresetDTO.class),
                any(),
                anyString(),
                any(),
                any(),
                any(),
                any(),
                any(),
                anyBoolean(),
                any(),
                any(),
                anyString(),
                anyInt(),
                anyInt()
        )).thenReturn(QuestListResponseDTO.builder()
                .items(List.of(
                        quest(11L, "Move sofa", "Move sofa"),
                        quest(12L, "Weekend help", "Weekend help")
                ))
                .page(0)
                .size(5)
                .totalItems(2)
                .totalPages(1)
                .build());

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("find people who can help move sofa")
                .semanticPlan(VisionSemanticPlan.empty())
                .build();

        VisionSearchDiscoveryDTO result = service.discover(null, understanding, currentUser);

        assertNotNull(result);
        assertEquals("move sofa", result.getQuery());
        assertEquals("search", result.getCapabilityId());
        assertFalse(result.getItems().isEmpty());
        assertEquals("Move sofa", result.getItems().get(0).getTitle());
        assertEquals("quest", result.getItems().get(0).getEntityFamily());
        assertTrue(result.getItems().stream().anyMatch(item -> "circle".equals(item.getEntityFamily())));
        assertTrue(result.getItems().stream().anyMatch(item -> "user".equals(item.getEntityFamily())));
        assertTrue(result.getItems().stream().anyMatch(item -> "application".equals(item.getEntityFamily())));
        assertTrue(result.getItems().stream().anyMatch(item -> "thing".equals(item.getEntityFamily())));
        assertTrue(result.getSummary().contains("move sofa"));
        assertTrue(result.getSummary().contains("narrow it"));
    }

    @Test
    void comparisonPreservesAuthorizedFamiliesAndUsesBoundedComparableFields() {
        when(questReadService.getQuestListPreset(
                any(QuestListPresetDTO.class), any(), anyString(), any(), any(), any(), any(), any(),
                anyBoolean(), any(), any(), anyString(), anyInt(), anyInt()
        )).thenReturn(QuestListResponseDTO.builder()
                .items(List.of(quest(11L, "Move sofa", "Move sofa")))
                .page(0).size(5).totalItems(1).totalPages(1).build());

        var result = service.compareWeb(currentUser, "move sofa", List.of("quest:11", "thing:90"));

        assertNotNull(result);
        assertEquals("cross_module.search.compare", result.getCapabilityId());
        assertEquals(3, result.getSelectionLimit());
        assertEquals(List.of("title", "summary", "owner_display_name", "member_context", "application_context", "location_label", "availability"), result.getComparableFields());
        assertEquals(2, result.getItems().size());
        assertTrue(result.getItems().stream().anyMatch(item -> "/work/quests/11".equals(item.getSourceRoute())));
        assertTrue(result.getItems().stream().anyMatch(item -> "thing".equals(item.getEntityFamily())));
    }

    @Test
    void visionComparisonUsesTheVisionCapabilityIdAndSameAuthorizationBoundary() {
        when(questReadService.getQuestListPreset(
                any(QuestListPresetDTO.class), any(), anyString(), any(), any(), any(), any(), any(),
                anyBoolean(), any(), any(), anyString(), anyInt(), anyInt()
        )).thenReturn(QuestListResponseDTO.builder()
                .items(List.of(quest(11L, "Move sofa", "Move sofa")))
                .page(0).size(5).totalItems(1).totalPages(1).build());

        var result = service.compareVision(currentUser, "move sofa", List.of("quest:11", "thing:90"));

        assertNotNull(result);
        assertEquals("vision.discovery.compare", result.getCapabilityId());
        assertEquals(2, result.getItems().size());
        assertEquals(0, result.getOmittedSelectionCount());
    }

    @Test
    void skipsUsernameSearchWhenTheNormalizedQueryIsBlank() {
        when(questReadService.getQuestListPreset(
                any(QuestListPresetDTO.class),
                any(),
                anyString(),
                any(),
                any(),
                any(),
                any(),
                any(),
                anyBoolean(),
                any(),
                any(),
                anyString(),
                anyInt(),
                anyInt()
        )).thenReturn(QuestListResponseDTO.builder()
                .items(List.of())
                .page(0)
                .size(5)
                .totalItems(0)
                .totalPages(0)
                .build());
        when(circleReadService.getCircles(currentUser)).thenReturn(List.of());
        when(questApplicationService.getApplicationsForApplicant(currentUser)).thenReturn(List.of());
        when(thingSharingService.getAvailableListings(currentUser)).thenReturn(ThingListingListResponseDTO.builder().items(List.of()).build());

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("search")
                .semanticPlan(VisionSemanticPlan.empty())
                .build();

        VisionSearchDiscoveryDTO result = service.discover(null, understanding, currentUser);

        assertNotNull(result);
        assertEquals("", result.getQuery());
        assertEquals("No matches.", result.getSummary());
        verify(appUserRepository, never()).searchByUsername(anyString());
    }

    @Test
    void stripsEntityFamilyWordsBeforeUsernameSearch() {
        when(questReadService.getQuestListPreset(
                any(QuestListPresetDTO.class),
                any(),
                anyString(),
                any(),
                any(),
                any(),
                any(),
                any(),
                anyBoolean(),
                any(),
                any(),
                anyString(),
                anyInt(),
                anyInt()
        )).thenReturn(QuestListResponseDTO.builder()
                .items(List.of())
                .page(0)
                .size(5)
                .totalItems(0)
                .totalPages(0)
                .build());
        when(circleReadService.getCircles(currentUser)).thenReturn(List.of());
        when(questApplicationService.getApplicationsForApplicant(currentUser)).thenReturn(List.of());
        when(thingSharingService.getAvailableListings(currentUser)).thenReturn(ThingListingListResponseDTO.builder().items(List.of()).build());

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("show user helper")
                .semanticPlan(VisionSemanticPlan.empty())
                .build();

        VisionSearchDiscoveryDTO result = service.discover(null, understanding, currentUser);

        assertNotNull(result);
        assertEquals("user helper", result.getQuery());
        assertTrue(result.getItems().stream().anyMatch(item -> "user".equals(item.getEntityFamily())));
        verify(appUserRepository).searchByUsername("helper");
    }

    private QuestResponseDTO quest(Long id, String title, String description) {
        return QuestResponseDTO.builder()
                .id(id)
                .title(title)
                .description(description)
                .build();
    }

    private AppUser user(Long id, String username, String email) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }
}
