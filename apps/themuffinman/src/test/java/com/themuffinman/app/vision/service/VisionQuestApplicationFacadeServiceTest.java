package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationReadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisionQuestApplicationFacadeServiceTest {

    @Mock
    private WorkmarketQuestApplicationReadService questApplicationReadService;

    @Mock
    private com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationService questApplicationService;

    @InjectMocks
    private VisionQuestApplicationFacadeService facadeService;

    @Test
    void getApplicationDetailDelegatesDirectlyToWorkmarketQuestApplicationService() {
        AppUser currentUser = new AppUser();
        currentUser.setId(11L);

        QuestApplicationDetailResponseDTO expected = QuestApplicationDetailResponseDTO.builder().build();
        when(questApplicationReadService.getApplicationDetailResponseById(42L, currentUser)).thenReturn(expected);

        QuestApplicationDetailResponseDTO result = facadeService.getApplicationDetail(42L, currentUser);

        assertEquals(expected, result);
        verify(questApplicationReadService).getApplicationDetailResponseById(42L, currentUser);
    }

    @Test
    void getMyApplicationsDelegatesToWorkmarketQuestApplicationService() {
        AppUser currentUser = new AppUser();
        currentUser.setId(11L);

        QuestApplicationResponseDTO responseDTO = QuestApplicationResponseDTO.builder().id(7L).build();
        when(questApplicationReadService.getApplicationsForApplicant(currentUser)).thenReturn(List.of(responseDTO));

        List<QuestApplicationResponseDTO> result = facadeService.getMyApplications(currentUser);

        assertEquals(List.of(responseDTO), result);
        verify(questApplicationReadService).getApplicationsForApplicant(currentUser);
    }

    @Test
    void getApplicationsViewForQuestDelegatesToWorkmarketQuestApplicationService() {
        AppUser currentUser = new AppUser();
        currentUser.setId(11L);

        QuestApplicationsViewDTO expected = QuestApplicationsViewDTO.builder().build();
        when(questApplicationReadService.getApplicationsViewForQuest(8L, currentUser, true)).thenReturn(expected);

        QuestApplicationsViewDTO result = facadeService.getApplicationsViewForQuest(8L, true, currentUser);

        assertEquals(expected, result);
        verify(questApplicationReadService).getApplicationsViewForQuest(8L, currentUser, true);
    }
}
