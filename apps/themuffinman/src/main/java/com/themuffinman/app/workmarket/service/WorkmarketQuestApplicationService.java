package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.AdminQuestApplicationUpdateRequestDTO;
import com.themuffinman.app.vision.dto.QuestApplicationListResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.vision.model.QuestApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("workmarketQuestApplicationService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkmarketQuestApplicationService {

    private final WorkmarketQuestApplicationReadService workmarketQuestApplicationReadService;
    private final WorkmarketApplyForQuestUseCase applyForQuestUseCase;
    private final WorkmarketUpdateMyApplicationUseCase updateMyApplicationUseCase;
    private final WorkmarketWithdrawMyApplicationUseCase withdrawMyApplicationUseCase;
    private final WorkmarketApproveApplicationUseCase approveApplicationUseCase;
    private final WorkmarketDeclineApplicationUseCase declineApplicationUseCase;
    private final WorkmarketQuestApplicationAdminQueryService questApplicationAdminQueryService;
    private final WorkmarketQuestApplicationAdminService questApplicationAdminService;

    @Transactional
    public QuestApplicationResponseDTO applyForQuest(Long questId, QuestApplicationRequestDTO dto, AppUser currentUser) {
        return workmarketQuestApplicationReadService.toApplicantResponse(applyForQuestUseCase.execute(questId, dto, currentUser));
    }

    public java.util.List<QuestApplicationResponseDTO> getApplicationsForQuest(Long questId, AppUser currentUser) {
        return workmarketQuestApplicationReadService.getApplicationsForQuest(questId, currentUser);
    }

    public QuestApplicationsViewDTO getApplicationsViewForQuest(Long questId, AppUser currentUser, boolean showAll) {
        return workmarketQuestApplicationReadService.getApplicationsViewForQuest(questId, currentUser, showAll);
    }

    public QuestApplicationsViewDTO getPublicApprovedApplicationsViewForQuest(Long questId) {
        return workmarketQuestApplicationReadService.getPublicApprovedApplicationsViewForQuest(questId);
    }

    public java.util.List<QuestApplicationResponseDTO> getApplicationsForApplicant(AppUser currentUser) {
        return workmarketQuestApplicationReadService.getApplicationsForApplicant(currentUser);
    }

    public QuestApplicationResponseDTO toApplicantResponse(QuestApplication application) {
        return workmarketQuestApplicationReadService.toApplicantResponse(application);
    }

    public QuestApplicationResponseDTO toViewerResponse(QuestApplication application, AppUser currentUser) {
        return workmarketQuestApplicationReadService.toViewerResponse(application, currentUser);
    }

    public java.util.List<QuestApplicationResponseDTO> getAllApplicationsForAdmin(AppUser currentUser) {
        return questApplicationAdminQueryService.getAllApplicationsForAdmin(currentUser);
    }

    public QuestApplicationListResponseDTO searchApplicationsForAdmin(
            AppUser currentUser,
            String query,
            QuestApplicationStatus status,
            Integer page,
            Integer size
    ) {
        return questApplicationAdminQueryService.searchApplicationsForAdmin(currentUser, query, status, page, size);
    }

    @Transactional
    public QuestApplicationResponseDTO updateApplicationForAdmin(
            Long applicationId,
            AdminQuestApplicationUpdateRequestDTO dto,
            AppUser currentUser
    ) {
        return questApplicationAdminService.updateApplicationForAdmin(applicationId, dto, currentUser);
    }

    @Transactional
    public void deleteApplicationForAdmin(Long applicationId, AppUser currentUser) {
        questApplicationAdminService.deleteApplicationForAdmin(applicationId, currentUser);
    }

    @Transactional
    public QuestApplicationResponseDTO updateMyApplication(Long questId, QuestApplicationRequestDTO dto, AppUser currentUser) {
        return workmarketQuestApplicationReadService.toApplicantResponse(updateMyApplicationUseCase.execute(questId, dto, currentUser));
    }

    @Transactional
    public QuestApplicationResponseDTO withdrawMyApplication(Long questId, AppUser currentUser) {
        return workmarketQuestApplicationReadService.toApplicantResponse(withdrawMyApplicationUseCase.execute(questId, currentUser));
    }

    @Transactional
    public QuestApplicationResponseDTO approveApplication(Long questId, Long applicationId, AppUser currentUser) {
        return workmarketQuestApplicationReadService.toApplicantResponse(approveApplicationUseCase.execute(questId, applicationId, currentUser));
    }

    @Transactional
    public QuestApplicationResponseDTO declineApplication(Long questId, Long applicationId, AppUser currentUser) {
        return workmarketQuestApplicationReadService.toApplicantResponse(declineApplicationUseCase.execute(questId, applicationId, currentUser));
    }

    public com.themuffinman.app.vision.dto.QuestApplicationDetailResponseDTO getApplicationDetailResponseById(Long applicationId, AppUser currentUser) {
        return workmarketQuestApplicationReadService.getApplicationDetailResponseById(applicationId, currentUser);
    }
}
