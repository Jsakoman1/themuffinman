package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.model.QuestStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestPreviewReadServiceTest {

    @Test
    void delegatesVisibilityScopedReadAndReturnsOnlyPreviewFields() {
        WorkmarketQuestReadService readService = mock(WorkmarketQuestReadService.class);
        AppUser viewer = new AppUser();
        QuestResponseDTO quest = QuestResponseDTO.builder()
                .id(12L).title("Move a sofa").description("<p>Two <strong>floors</strong></p>")
                .creatorUsername("owner").status(QuestStatus.OPEN).allowedActions(List.of()).build();
        when(readService.getQuestResponseById(12L, viewer)).thenReturn(quest);

        var preview = new QuestPreviewReadService(readService).getPreview(12L, viewer);

        assertEquals("Move a sofa", preview.title());
        assertEquals("Two floors", preview.summary());
        assertEquals(QuestStatus.OPEN.name(), preview.status());
    }
}
