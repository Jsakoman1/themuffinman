package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.service.LocationQuestPresentationService;
import com.themuffinman.app.vision.dto.QuestAllowedActionDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.vision.dto.QuestViewerRelationDTO;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestAudience;
import com.themuffinman.app.vision.model.QuestStatus;
import com.themuffinman.app.vision.service.VisionPresentationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestPresentationAssemblerTest {

    @Mock
    private LocationQuestPresentationService locationQuestPresentationService;

    private QuestPresentationAssembler questPresentationAssembler;

    @BeforeEach
    void setUp() {
        questPresentationAssembler = new QuestPresentationAssembler(new VisionPresentationHelper(), locationQuestPresentationService);
        when(locationQuestPresentationService.resolveQuestLocationLabel(any(Quest.class), any())).thenReturn(null);
        when(locationQuestPresentationService.resolveQuestLocationSourceSummary(any(Quest.class))).thenReturn("Uses creator profile location");
        when(locationQuestPresentationService.resolveQuestLocationVisibilitySummary(any(Quest.class), any())).thenReturn("Approximate area shown");
    }

    @Test
    void buildDefaultPresentationUsesTheCanonicalQuestBaseShape() {
        Quest quest = new Quest();
        quest.setAwardAmount(BigDecimal.valueOf(50));
        quest.setStatus(QuestStatus.OPEN);
        quest.setAudience(QuestAudience.EVERYONE);
        quest.setTermFixed(true);
        quest.setAssigneeTarget(2);

        var dto = questPresentationAssembler.buildDefaultPresentation(quest);

        assertEquals("OPEN", dto.getStatusLabel());
        assertEquals("Everyone", dto.getAudienceLabel());
        assertEquals(BigDecimal.valueOf(50), dto.getSuggestedApplicationPrice());
        assertTrue(dto.getApplicationDraftRules().isProposedPriceRequired());
        assertFalse(dto.isCanEdit());
        assertFalse(dto.isOfferSectionVisible());
    }

    @Test
    void buildPresentationSetsViewerAwareExecutionState() {
        AppUser creator = new AppUser();
        creator.setId(1L);
        creator.setUsername("creator");

        Quest quest = new Quest();
        quest.setId(9L);
        quest.setCreator(creator);
        quest.setAwardAmount(BigDecimal.valueOf(20));
        quest.setStatus(QuestStatus.OPEN);
        quest.setAudience(QuestAudience.EVERYONE);
        quest.setTermFixed(true);
        quest.setAssigneeTarget(1);

        QuestResponseDTO questResponse = QuestResponseDTO.builder()
                .awardAmount(BigDecimal.valueOf(20))
                .status(QuestStatus.OPEN)
                .audience(QuestAudience.EVERYONE)
                .termFixed(true)
                .assigneeTarget(1)
                .approvedApplicationCount(1)
                .remainingAssigneeSlots(0)
                .showApprovedApplicants(true)
                .viewerRelation(QuestViewerRelationDTO.OWNER)
                .allowedActions(List.of(QuestAllowedActionDTO.START, QuestAllowedActionDTO.VIEW_APPLICATIONS, QuestAllowedActionDTO.EDIT))
                .hasApplied(false)
                .myApplicationId(null)
                .canViewApplications(true)
                .build();

        var dto = questPresentationAssembler.buildPresentation(quest, questResponse, creator);

        assertEquals("Start work", dto.getPrimaryExecutionActionLabel());
        assertTrue(dto.isCanViewApplications());
        assertTrue(dto.isCanEdit());
        assertTrue(dto.isApplicationsSectionVisible());
        assertNull(dto.getExecutionHelperText());
    }
}
