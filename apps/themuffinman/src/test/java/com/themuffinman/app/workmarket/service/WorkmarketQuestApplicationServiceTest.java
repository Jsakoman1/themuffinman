package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.model.QuestApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkmarketQuestApplicationServiceTest {

    @Mock private WorkmarketQuestApplicationReadService readService;
    @Mock private WorkmarketApplyForQuestUseCase applyForQuestUseCase;
    @Mock private WorkmarketUpdateMyApplicationUseCase updateMyApplicationUseCase;
    @Mock private WorkmarketWithdrawMyApplicationUseCase withdrawMyApplicationUseCase;
    @Mock private WorkmarketApproveApplicationUseCase approveApplicationUseCase;
    @Mock private WorkmarketDeclineApplicationUseCase declineApplicationUseCase;
    @Mock private WorkmarketQuestApplicationAdminQueryService adminQueryService;
    @Mock private WorkmarketQuestApplicationAdminService adminService;
    @Mock private WorkmarketWorkerManagementService workerManagementService;

    @Test
    void appliesThroughTheAuthoritativeUseCaseAndReturnsApplicantSafeResponse() {
        WorkmarketQuestApplicationService service = new WorkmarketQuestApplicationService(
                readService, applyForQuestUseCase, updateMyApplicationUseCase, withdrawMyApplicationUseCase,
                approveApplicationUseCase, declineApplicationUseCase, adminQueryService, adminService, workerManagementService
        );
        AppUser applicant = new AppUser();
        applicant.setId(7L);
        QuestApplicationRequestDTO request = QuestApplicationRequestDTO.builder()
                .message("I can help on Saturday morning.")
                .proposedPrice(new BigDecimal("45.00"))
                .build();
        QuestApplication application = new QuestApplication();
        QuestApplicationResponseDTO response = QuestApplicationResponseDTO.builder().id(12L).build();

        when(applyForQuestUseCase.execute(3L, request, applicant)).thenReturn(application);
        when(readService.toApplicantResponse(application)).thenReturn(response);

        QuestApplicationResponseDTO result = service.applyForQuest(3L, request, applicant);

        assertSame(response, result);
        verify(applyForQuestUseCase).execute(3L, request, applicant);
        verify(readService).toApplicantResponse(application);
    }
}
