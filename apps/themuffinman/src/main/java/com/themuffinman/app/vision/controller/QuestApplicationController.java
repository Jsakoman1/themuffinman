package com.themuffinman.app.vision.controller;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.vision.dto.AdminApplicationsQueryDTO;
import com.themuffinman.app.vision.dto.AdminQuestApplicationUpdateRequestDTO;
import com.themuffinman.app.vision.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.vision.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationListResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.service.VisionQuestApplicationFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuestApplicationController {

    private final VisionQuestApplicationFacadeService questApplicationService;

    @PostMapping("/quests/{questId}/applications")
    public ActionResultDTO applyForQuest(
            @PathVariable Long questId,
            @Valid @RequestBody QuestApplicationRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return questApplicationService.applyForQuest(questId, dto, currentUser);
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
        return questApplicationService.getApplicationsViewForQuest(questId, showAll, currentUser);
    }

    @GetMapping("/quests/applications/me")
    public List<QuestApplicationResponseDTO> getMyApplications(@AuthenticationPrincipal AppUser currentUser) {
        return questApplicationService.getMyApplications(currentUser);
    }

    @GetMapping("/applications/{applicationId}/detail")
    public QuestApplicationDetailResponseDTO getApplicationDetail(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return questApplicationService.getApplicationDetail(applicationId, currentUser);
    }

    @GetMapping("/admin/applications")
    public QuestApplicationListResponseDTO getAllApplicationsForAdmin(
            @AuthenticationPrincipal AppUser currentUser,
            @ModelAttribute AdminApplicationsQueryDTO query
    ) {
        return questApplicationService.getAllApplicationsForAdmin(currentUser, query);
    }

    @PutMapping("/admin/applications/{applicationId}")
    public ActionResultDTO updateApplicationForAdmin(
            @PathVariable Long applicationId,
            @Valid @RequestBody AdminQuestApplicationUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return questApplicationService.updateApplicationForAdmin(applicationId, dto, currentUser);
    }

    @DeleteMapping("/admin/applications/{applicationId}")
    public ActionResultDTO deleteApplicationForAdmin(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return questApplicationService.deleteApplicationForAdmin(applicationId, currentUser);
    }

    @PutMapping("/quests/{questId}/applications/me")
    public ActionResultDTO updateMyApplication(
            @PathVariable Long questId,
            @Valid @RequestBody QuestApplicationRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return questApplicationService.updateMyApplication(questId, dto, currentUser);
    }

    @PatchMapping("/quests/{questId}/applications/me/withdraw")
    public ActionResultDTO withdrawMyApplication(@PathVariable Long questId, @AuthenticationPrincipal AppUser currentUser) {
        return questApplicationService.withdrawMyApplication(questId, currentUser);
    }

    @PatchMapping("/quests/{questId}/applications/{applicationId}/approve")
    public ActionResultDTO approveApplication(
            @PathVariable Long questId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return questApplicationService.approveApplication(questId, applicationId, currentUser);
    }

    @PatchMapping("/quests/{questId}/applications/{applicationId}/decline")
    public ActionResultDTO declineApplication(
            @PathVariable Long questId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return questApplicationService.declineApplication(questId, applicationId, currentUser);
    }

}
