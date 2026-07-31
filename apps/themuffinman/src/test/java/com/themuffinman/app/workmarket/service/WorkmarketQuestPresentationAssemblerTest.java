package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.location.service.LocationQuestPresentationService;
import com.themuffinman.app.workmarket.dto.QuestAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestViewerRelationDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkmarketQuestPresentationAssemblerTest {

    @Mock private WorkmarketPresentationHelper presentationHelper;
    @Mock private LocationQuestPresentationService locationQuestPresentationService;
    @InjectMocks private WorkmarketQuestPresentationAssembler assembler;

    @Test
    void preparesSideJobRequestGuidanceWithoutChangingAllowedActions() {
        Quest quest = sideJob();
        QuestResponseDTO response = QuestResponseDTO.builder()
                .status(QuestStatus.OPEN)
                .viewerRelation(QuestViewerRelationDTO.VIEWER)
                .allowedActions(List.of(QuestAllowedActionDTO.APPLY))
                .build();
        prepareBasePresentation(quest);

        var presentation = assembler.buildPresentation(quest, response, null);

        assertEquals("SideJob", presentation.getSideJobLabel());
        assertEquals("Paid", presentation.getRewardLabel());
        assertEquals("Offer to help", presentation.getNextActionLabel());
        assertEquals("The owner receives your request to help. Nothing is confirmed yet.", presentation.getNextActionOutcome());
    }

    @Test
    void exposesPendingRequestAttentionOnlyForAnAuthorizedOwner() {
        Quest quest = sideJob();
        QuestResponseDTO response = QuestResponseDTO.builder()
                .status(QuestStatus.OPEN)
                .viewerRelation(QuestViewerRelationDTO.OWNER)
                .allowedActions(List.of(QuestAllowedActionDTO.VIEW_APPLICATIONS))
                .build();
        prepareBasePresentation(quest);

        var presentation = assembler.buildPresentation(quest, response, null, 2);

        assertEquals("Review requests", presentation.getNextActionLabel());
        assertEquals("2 requests need your review", presentation.getAttentionLabel());
    }

    private Quest sideJob() {
        Quest quest = new Quest();
        quest.setStatus(QuestStatus.OPEN);
        quest.setAwardAmount(BigDecimal.valueOf(40));
        quest.setAssigneeTarget(1);
        quest.setAudience(QuestAudience.EVERYONE);
        return quest;
    }

    private void prepareBasePresentation(Quest quest) {
        when(presentationHelper.formatQuestStatus(QuestStatus.OPEN)).thenReturn("Open");
        when(presentationHelper.badgeClassForQuestStatus(QuestStatus.OPEN)).thenReturn("open");
        when(presentationHelper.surfaceClassForQuestStatus(QuestStatus.OPEN)).thenReturn("open");
        when(presentationHelper.formatTimeType(false)).thenReturn("Flexible timing");
        when(presentationHelper.formatAudience(QuestAudience.EVERYONE)).thenReturn("Everyone");
        when(presentationHelper.formatAssigneeTarget(1)).thenReturn("1 person");
        when(presentationHelper.showAssigneeTarget(1)).thenReturn(true);
        when(locationQuestPresentationService.resolveQuestLocationLabel(quest, null)).thenReturn("Anywhere");
        when(locationQuestPresentationService.resolveQuestLocationSourceSummary(quest)).thenReturn("Location provided by owner");
        when(locationQuestPresentationService.resolveQuestLocationVisibilitySummary(quest, null)).thenReturn("Location visibility is respected");
    }
}
