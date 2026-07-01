package com.themuffinman.app.vision.controller;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.vision.dto.AdminApplicationsQueryDTO;
import com.themuffinman.app.vision.dto.AdminQuestApplicationUpdateRequestDTO;
import com.themuffinman.app.vision.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.vision.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationListResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.service.QuestService;
import com.themuffinman.app.vision.service.QuestApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuestApplicationController {

    private final QuestApplicationService questApplicationService;
    private final QuestService questService;

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
        return questApplicationService.getApplicationsForQuest(questId, currentUser);
    }

    @GetMapping("/quests/{questId}/applications/view")
    public QuestApplicationsViewDTO getApplicationsViewForQuest(
            @PathVariable Long questId,
            @RequestParam(defaultValue = "false") boolean showAll,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return questApplicationService.getApplicationsViewForQuest(questId, currentUser, showAll);
    }

    @GetMapping("/quests/applications/me")
    public List<QuestApplicationResponseDTO> getMyApplications(@AuthenticationPrincipal AppUser currentUser) {
        return questApplicationService.getApplicationsForApplicant(currentUser);
    }

    @GetMapping("/applications/{applicationId}/detail")
    public QuestApplicationDetailResponseDTO getApplicationDetail(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return questService.getApplicationDetailResponseById(applicationId, currentUser);
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
