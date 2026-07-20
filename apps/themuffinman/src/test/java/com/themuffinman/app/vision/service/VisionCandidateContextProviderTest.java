package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.workmarket.dto.QuestAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisionCandidateContextProviderTest {

    @Test
    void ownedWorkProviderIncludesOnlyViewerOwnedQuests() {
        WorkmarketQuestReadService readService = mock(WorkmarketQuestReadService.class);
        AppUser user = new AppUser();
        user.setId(7L);

        QuestResponseDTO owned = QuestResponseDTO.builder().id(11L).creatorId(7L).title("Grill help").description("Chicken").build();
        QuestResponseDTO other = QuestResponseDTO.builder().id(12L).creatorId(8L).title("Other work").description("Boxes").build();
        when(readService.getAllQuestResponses(user)).thenReturn(List.of(owned, other));

        VisionCandidateContext context = new VisionWorkCandidateContextProvider(readService)
                .build(user, VisionIntent.VIEW_MY_WORK, "req-1", "open my grill work");

        assertEquals("current_user_owned_quests", context.getScope());
        assertEquals(1, context.getTotalCandidates());
        assertEquals("quest:11", context.getItems().getFirst().getStableCandidateId());
    }

    @Test
    void applicationProviderUsesViewerApplicationReadModel() {
        var readService = mock(com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationReadService.class);
        AppUser user = new AppUser();
        user.setId(7L);
        var application = new com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO();
        application.setId(21L);
        application.setQuestTitle("Garden work");
        when(readService.getApplicationsForApplicant(user)).thenReturn(List.of(application));

        VisionCandidateContext context = new VisionApplicationCandidateContextProvider(readService)
                .build(user, VisionIntent.VIEW_APPLICATIONS, "req-2", "show my applications");

        assertEquals("current_user_applications", context.getScope());
        assertEquals("application:21", context.getItems().getFirst().getStableCandidateId());
    }

    @Test
    void registryReturnsNoCandidateContextForPureNavigation() {
        VisionCandidateContextService service = new VisionCandidateContextService(List.of());
        AppUser user = new AppUser();
        user.setId(7L);

        assertNull(service.build(user, VisionIntent.VIEW_PROFILE, "req-3", "open my profile"));
    }

    @Test
    void largeWorkSetIsExplicitlyPartialInsteadOfSilentlyTruncated() {
        WorkmarketQuestReadService readService = mock(WorkmarketQuestReadService.class);
        AppUser user = new AppUser();
        user.setId(7L);
        List<QuestResponseDTO> quests = java.util.stream.IntStream.rangeClosed(1, 51)
                .mapToObj(id -> QuestResponseDTO.builder().id((long) id).creatorId(7L).title("Work " + id).build())
                .toList();
        when(readService.getAllQuestResponses(user)).thenReturn(quests);

        VisionCandidateContext context = new VisionWorkCandidateContextProvider(readService)
                .build(user, VisionIntent.VIEW_MY_WORK, "req-4", "show my work");

        assertEquals(false, context.isComplete());
        assertEquals(51, context.getTotalCandidates());
        assertEquals(50, context.getReturnedCandidates());
    }
}
