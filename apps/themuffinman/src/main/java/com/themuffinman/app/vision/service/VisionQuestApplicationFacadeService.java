package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.AdminApplicationsQueryDTO;
import com.themuffinman.app.vision.dto.AdminQuestApplicationUpdateRequestDTO;
import com.themuffinman.app.vision.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationListResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationReadService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisionQuestApplicationFacadeService {

    private final WorkmarketQuestApplicationReadService questApplicationReadService;
    private final WorkmarketQuestApplicationService questApplicationService;

    public ActionResultDTO applyForQuest(Long questId, QuestApplicationRequestDTO dto, AppUser currentUser) {
        questApplicationService.applyForQuest(questId, dto, currentUser);
        return ActionResults.of("APPLY_FOR_QUEST", "Application sent.");
    }

    public List<QuestApplicationResponseDTO> getApplicationsForQuest(Long questId, AppUser currentUser) {
        return questApplicationReadService.getApplicationsForQuest(questId, currentUser);
    }

    public QuestApplicationsViewDTO getApplicationsViewForQuest(Long questId, boolean showAll, AppUser currentUser) {
        return questApplicationReadService.getApplicationsViewForQuest(questId, currentUser, showAll);
    }

    public List<QuestApplicationResponseDTO> getMyApplications(AppUser currentUser) {
        return questApplicationReadService.getApplicationsForApplicant(currentUser);
    }

    public QuestApplicationDetailResponseDTO getApplicationDetail(Long applicationId, AppUser currentUser) {
        return questApplicationReadService.getApplicationDetailResponseById(applicationId, currentUser);
    }

    public QuestApplicationListResponseDTO getAllApplicationsForAdmin(AppUser currentUser, AdminApplicationsQueryDTO query) {
        return questApplicationService.searchApplicationsForAdmin(
                currentUser,
                query.getQ(),
                query.getStatus(),
                query.getPage(),
                query.getSize()
        );
    }

    public ActionResultDTO updateApplicationForAdmin(Long applicationId, AdminQuestApplicationUpdateRequestDTO dto, AppUser currentUser) {
        questApplicationService.updateApplicationForAdmin(applicationId, dto, currentUser);
        return ActionResults.of("UPDATE_APPLICATION_AS_ADMIN", "Application updated.");
    }

    public ActionResultDTO deleteApplicationForAdmin(Long applicationId, AppUser currentUser) {
        questApplicationService.deleteApplicationForAdmin(applicationId, currentUser);
        return ActionResults.of("DELETE_APPLICATION_AS_ADMIN", "Application deleted.");
    }

    public ActionResultDTO updateMyApplication(Long questId, QuestApplicationRequestDTO dto, AppUser currentUser) {
        questApplicationService.updateMyApplication(questId, dto, currentUser);
        return ActionResults.of("UPDATE_APPLICATION", "Application updated.");
    }

    public ActionResultDTO withdrawMyApplication(Long questId, AppUser currentUser) {
        questApplicationService.withdrawMyApplication(questId, currentUser);
        return ActionResults.of("WITHDRAW_APPLICATION", "Application withdrawn.");
    }

    public ActionResultDTO approveApplication(Long questId, Long applicationId, AppUser currentUser) {
        questApplicationService.approveApplication(questId, applicationId, currentUser);
        return ActionResults.of("APPROVE_APPLICATION", "Application approved.");
    }

    public ActionResultDTO declineApplication(Long questId, Long applicationId, AppUser currentUser) {
        questApplicationService.declineApplication(questId, applicationId, currentUser);
        return ActionResults.of("DECLINE_APPLICATION", "Application declined.");
    }
}
