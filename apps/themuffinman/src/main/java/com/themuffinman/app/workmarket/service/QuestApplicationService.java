package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.AdminQuestApplicationUpdateRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.repository.QuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.QuestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestApplicationService {

    private final QuestApplicationRepository questApplicationRepository;
    private final QuestRepository questRepository;
    private final QuestApplicationWorkflowSupport workflowSupport;
    private final QuestApplicationReadService questApplicationReadService;
    private final QuestApplicationAdminQueryService questApplicationAdminQueryService;
    private final QuestApplicationAdminService questApplicationAdminService;
    private final ApplyForQuestUseCase applyForQuestUseCase;
    private final UpdateMyApplicationUseCase updateMyApplicationUseCase;
    private final WithdrawMyApplicationUseCase withdrawMyApplicationUseCase;
    private final ApproveApplicationUseCase approveApplicationUseCase;
    private final DeclineApplicationUseCase declineApplicationUseCase;

    @Transactional
    public QuestApplicationResponseDTO applyForQuest(Long questId, QuestApplicationRequestDTO dto, AppUser currentUser) {
        return toApplicantResponse(applyForQuestUseCase.execute(questId, dto, currentUser));
    }

    public List<QuestApplicationResponseDTO> getApplicationsForQuest(Long questId, AppUser currentUser) {
        return questApplicationReadService.getApplicationsForQuest(questId, currentUser);
    }

    public QuestApplicationsViewDTO getApplicationsViewForQuest(Long questId, AppUser currentUser, boolean showAll) {
        return questApplicationReadService.getApplicationsViewForQuest(questId, currentUser, showAll);
    }

    public QuestApplicationsViewDTO getPublicApprovedApplicationsViewForQuest(Long questId) {
        return questApplicationReadService.getPublicApprovedApplicationsViewForQuest(questId);
    }

    public List<QuestApplicationResponseDTO> getApplicationsForApplicant(AppUser currentUser) {
        return questApplicationReadService.getApplicationsForApplicant(currentUser);
    }

    public QuestApplicationResponseDTO toApplicantResponse(QuestApplication application) {
        return questApplicationReadService.toApplicantResponse(application);
    }

    public QuestApplicationResponseDTO toViewerResponse(QuestApplication application, AppUser currentUser) {
        return questApplicationReadService.toViewerResponse(application, currentUser);
    }

    public List<QuestApplicationResponseDTO> getAllApplicationsForAdmin(AppUser currentUser) {
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
        return toApplicantResponse(updateMyApplicationUseCase.execute(questId, dto, currentUser));
    }

    @Transactional
    public QuestApplicationResponseDTO withdrawMyApplication(Long questId, AppUser currentUser) {
        return toApplicantResponse(withdrawMyApplicationUseCase.execute(questId, currentUser));
    }

    @Transactional
    public QuestApplicationResponseDTO approveApplication(Long questId, Long applicationId, AppUser currentUser) {
        return toApplicantResponse(approveApplicationUseCase.execute(questId, applicationId, currentUser));
    }

    @Transactional
    public QuestApplicationResponseDTO declineApplication(Long questId, Long applicationId, AppUser currentUser) {
        return toApplicantResponse(declineApplicationUseCase.execute(questId, applicationId, currentUser));
    }

}
