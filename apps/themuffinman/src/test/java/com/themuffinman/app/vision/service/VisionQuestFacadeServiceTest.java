package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestListPresetDTO;
import com.themuffinman.app.vision.dto.QuestListResponseDTO;
import com.themuffinman.app.vision.dto.QuestRequestDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationReadService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisionQuestFacadeServiceTest {

    @Mock
    private WorkmarketQuestService questService;

    @Mock
    private WorkmarketQuestReadService questReadService;

    @Mock
    private WorkmarketQuestApplicationReadService questApplicationReadService;

    @InjectMocks
    private VisionQuestFacadeService facadeService;

    @Test
    void getApplicationDetailDelegatesToWorkmarketQuestApplicationService() {
        AppUser currentUser = new AppUser();
        currentUser.setId(21L);

        QuestApplicationDetailResponseDTO expected = QuestApplicationDetailResponseDTO.builder().build();
        when(questApplicationReadService.getApplicationDetailResponseById(88L, currentUser)).thenReturn(expected);

        QuestApplicationDetailResponseDTO result = facadeService.getApplicationDetailResponseById(88L, currentUser);

        assertEquals(expected, result);
        verify(questApplicationReadService).getApplicationDetailResponseById(88L, currentUser);
    }

    @Test
    void getQuestResponseByIdDelegatesToWorkmarketQuestService() {
        AppUser currentUser = new AppUser();
        currentUser.setId(21L);

        QuestResponseDTO expected = QuestResponseDTO.builder().id(10L).build();
        when(questReadService.getQuestResponseById(10L, currentUser)).thenReturn(expected);

        QuestResponseDTO result = facadeService.getQuestResponseById(10L, currentUser);

        assertEquals(expected, result);
        verify(questReadService).getQuestResponseById(10L, currentUser);
    }

    @Test
    void getQuestDetailResponseByIdDelegatesToWorkmarketQuestReadService() {
        AppUser currentUser = new AppUser();
        currentUser.setId(21L);

        QuestDetailResponseDTO expected = QuestDetailResponseDTO.builder().build();
        when(questReadService.getQuestDetailResponseById(10L, currentUser)).thenReturn(expected);

        QuestDetailResponseDTO result = facadeService.getQuestDetailResponseById(10L, currentUser);

        assertEquals(expected, result);
        verify(questReadService).getQuestDetailResponseById(10L, currentUser);
    }

    @Test
    void getQuestListPresetDelegatesToWorkmarketQuestReadService() {
        AppUser currentUser = new AppUser();
        currentUser.setId(21L);

        QuestListResponseDTO expected = QuestListResponseDTO.builder().build();
        when(questReadService.getQuestListPreset(
                QuestListPresetDTO.AVAILABLE,
                currentUser,
                "garden",
                null,
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14),
                "Europe/Zurich",
                120,
                true,
                false,
                5,
                "newest",
                1,
                10
        )).thenReturn(expected);

        QuestListResponseDTO result = facadeService.getQuestListPreset(
                QuestListPresetDTO.AVAILABLE,
                currentUser,
                "garden",
                null,
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14),
                "Europe/Zurich",
                120,
                true,
                false,
                5,
                "newest",
                1,
                10
        );

        assertEquals(expected, result);
        verify(questReadService).getQuestListPreset(
                QuestListPresetDTO.AVAILABLE,
                currentUser,
                "garden",
                null,
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14),
                "Europe/Zurich",
                120,
                true,
                false,
                5,
                "newest",
                1,
                10
        );
    }

    @Test
    void confirmQuestTermChangeDelegatesToWorkmarketQuestServiceResponseAdapter() {
        AppUser currentUser = new AppUser();
        currentUser.setId(21L);

        com.themuffinman.app.vision.model.Quest quest = new com.themuffinman.app.vision.model.Quest();
        QuestResponseDTO expected = QuestResponseDTO.builder().id(11L).build();
        when(questService.confirmQuestTermChange(12L, currentUser)).thenReturn(quest);
        when(questService.toResponse(quest, currentUser)).thenReturn(expected);

        QuestResponseDTO result = facadeService.confirmQuestTermChange(12L, currentUser);

        assertEquals(expected, result);
        verify(questService).confirmQuestTermChange(12L, currentUser);
        verify(questService).toResponse(quest, currentUser);
    }

    @Test
    void rejectQuestTermChangeDelegatesToWorkmarketQuestServiceResponseAdapter() {
        AppUser currentUser = new AppUser();
        currentUser.setId(21L);

        com.themuffinman.app.vision.model.Quest quest = new com.themuffinman.app.vision.model.Quest();
        QuestResponseDTO expected = QuestResponseDTO.builder().id(12L).build();
        when(questService.rejectQuestTermChange(13L, currentUser)).thenReturn(quest);
        when(questService.toResponse(quest, currentUser)).thenReturn(expected);

        QuestResponseDTO result = facadeService.rejectQuestTermChange(13L, currentUser);

        assertEquals(expected, result);
        verify(questService).rejectQuestTermChange(13L, currentUser);
        verify(questService).toResponse(quest, currentUser);
    }
}
