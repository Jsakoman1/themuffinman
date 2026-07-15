package com.themuffinman.app.workmarket.controller;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.AdminApplicationsQueryDTO;
import com.themuffinman.app.workmarket.dto.AdminQuestApplicationUpdateRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationReadService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationService;
import com.themuffinman.app.workmarket.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationsViewDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuestApplicationController {

    private final WorkmarketQuestApplicationService questApplicationService;
    private final WorkmarketQuestApplicationReadService questApplicationReadService;

    @PostMapping("/quests/{questId}/applications")
    public ActionResultDTO applyForQuest(
            @PathVariable Long questId,
            @Valid @RequestBody QuestApplicationRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        questApplicationService.applyForQuest(questId, dto, currentUser);
        return ActionResults.of("APPLY_FOR_QUEST", "Application sent.");
    }

    @GetMapping("/quests/{questId}/applications")
    public List<QuestApplicationResponseDTO> getApplicationsForQuest(@PathVariable Long questId, @AuthenticationPrincipal AppUser currentUser) {
        return questApplicationReadService.getApplicationsForQuest(questId, currentUser);
    }

    @GetMapping("/quests/{questId}/applications/view")
    public QuestApplicationsViewDTO getApplicationsViewForQuest(
            @PathVariable Long questId,
            @RequestParam(defaultValue = "false") boolean showAll,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return questApplicationReadService.getApplicationsViewForQuest(questId, currentUser, showAll);
    }

    @GetMapping("/quests/applications/me")
    public QuestApplicationListResponseDTO getMyApplications(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return questApplicationReadService.getApplicationsForApplicantPage(currentUser, page, size);
    }

    @GetMapping("/applications/{applicationId}/detail")
    public QuestApplicationDetailResponseDTO getApplicationDetail(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return questApplicationReadService.getApplicationDetailResponseById(applicationId, currentUser);
    }

    @GetMapping("/admin/applications")
    public QuestApplicationListResponseDTO getAllApplicationsForAdmin(
            @AuthenticationPrincipal AppUser currentUser,
            @ModelAttribute AdminApplicationsQueryDTO query
    ) {
        return questApplicationService.searchApplicationsForAdmin(
                currentUser,
                query.getQ(),
                query.getStatus(),
                query.getPage(),
                query.getSize()
        );
    }

    @PutMapping("/admin/applications/{applicationId}")
    public ActionResultDTO updateApplicationForAdmin(
            @PathVariable Long applicationId,
            @Valid @RequestBody AdminQuestApplicationUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        questApplicationService.updateApplicationForAdmin(applicationId, dto, currentUser);
        return ActionResults.of("UPDATE_APPLICATION_AS_ADMIN", "Application updated.");
    }

    @DeleteMapping("/admin/applications/{applicationId}")
    public ActionResultDTO deleteApplicationForAdmin(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        questApplicationService.deleteApplicationForAdmin(applicationId, currentUser);
        return ActionResults.of("DELETE_APPLICATION_AS_ADMIN", "Application deleted.");
    }

    @PutMapping("/quests/{questId}/applications/me")
    public ActionResultDTO updateMyApplication(
            @PathVariable Long questId,
            @Valid @RequestBody QuestApplicationRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        questApplicationService.updateMyApplication(questId, dto, currentUser);
        return ActionResults.of("UPDATE_APPLICATION", "Application updated.");
    }

    @PatchMapping("/quests/{questId}/applications/me/withdraw")
    public ActionResultDTO withdrawMyApplication(@PathVariable Long questId, @AuthenticationPrincipal AppUser currentUser) {
        questApplicationService.withdrawMyApplication(questId, currentUser);
        return ActionResults.of("WITHDRAW_APPLICATION", "Application withdrawn.");
    }

    @PatchMapping("/quests/{questId}/applications/{applicationId}/approve")
    public ActionResultDTO approveApplication(
            @PathVariable Long questId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        questApplicationService.approveApplication(questId, applicationId, currentUser);
        return ActionResults.of("APPROVE_APPLICATION", "Application approved.");
    }

    @PatchMapping("/quests/{questId}/applications/{applicationId}/decline")
    public ActionResultDTO declineApplication(
            @PathVariable Long questId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        questApplicationService.declineApplication(questId, applicationId, currentUser);
        return ActionResults.of("DECLINE_APPLICATION", "Application declined.");
    }
}
