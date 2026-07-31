package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkmarketQuestServiceTest {

    @Mock private WorkmarketCreateQuestUseCase createQuestUseCase;
    @Mock private WorkmarketUpdateQuestUseCase updateQuestUseCase;
    @Mock private WorkmarketDeleteQuestUseCase deleteQuestUseCase;
    @Mock private WorkmarketStartQuestUseCase startQuestUseCase;
    @Mock private WorkmarketCompleteQuestUseCase completeQuestUseCase;
    @Mock private WorkmarketConfirmQuestTermChangeUseCase confirmTermChangeUseCase;
    @Mock private WorkmarketRejectQuestTermChangeUseCase rejectTermChangeUseCase;
    @Mock private WorkmarketQuestReadService readService;
    @Mock private WorkmarketQuestUpdateService updateService;

    @Test
    void createsSideJobOnlyThroughTheAuthoritativeCreationUseCase() {
        WorkmarketQuestService service = new WorkmarketQuestService(createQuestUseCase, updateQuestUseCase,
                deleteQuestUseCase, startQuestUseCase, completeQuestUseCase, confirmTermChangeUseCase,
                rejectTermChangeUseCase, readService, updateService);
        QuestRequestDTO request = new QuestRequestDTO();
        AppUser owner = new AppUser();
        Quest created = new Quest();
        when(createQuestUseCase.execute(request, owner)).thenReturn(created);

        Quest result = service.createQuest(request, owner);

        assertSame(created, result);
        verify(createQuestUseCase).execute(request, owner);
    }
}
