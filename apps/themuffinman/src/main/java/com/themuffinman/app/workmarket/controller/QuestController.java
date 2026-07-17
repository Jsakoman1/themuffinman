package com.themuffinman.app.workmarket.controller;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestService;
import com.themuffinman.app.workmarket.dto.QuestDetailResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestListPresetDTO;
import com.themuffinman.app.workmarket.dto.QuestListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestSearchRequestDTO;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/quests")
@RequiredArgsConstructor
public class QuestController {

    private final WorkmarketQuestService questService;
    private final WorkmarketQuestReadService questReadService;

    @PostMapping
    public ActionResultDTO createQuest(@Valid @RequestBody QuestRequestDTO dto, @AuthenticationPrincipal AppUser currentUser) {
        questService.createQuest(dto, currentUser);
        return ActionResults.of("CREATE_QUEST", "Quest created.");
    }

    @GetMapping
    public List<QuestResponseDTO> getAllQuests(@AuthenticationPrincipal AppUser currentUser) {
        return questReadService.getAllQuestResponses(currentUser);
    }

    @GetMapping("/search")
    public QuestListResponseDTO searchQuests(
            @AuthenticationPrincipal AppUser currentUser,
            @ModelAttribute QuestSearchRequestDTO query
    ) {
        return questReadService.searchQuests(
                currentUser,
                query.getQ(),
                query.getStatus(),
                query.getAudience(),
                query.getDateFrom(),
                query.getDateTo(),
                query.getViewerTimeZone(),
                query.getViewerTimezoneOffsetMinutes(),
                query.getExcludeMine(),
                query.getWithImages(),
                query.getScheduledOnly(),
                query.getRadiusKm(),
                query.getSort(),
                query.getPage(),
                query.getSize()
        );
    }

    @GetMapping("/presets/{preset}")
    public QuestListResponseDTO getQuestPreset(
            @PathVariable QuestListPresetDTO preset,
            @AuthenticationPrincipal AppUser currentUser,
            @ModelAttribute QuestSearchRequestDTO query
    ) {
        return questReadService.getQuestListPreset(
                preset,
                currentUser,
                query.getQ(),
                query.getAudience(),
                query.getDateFrom(),
                query.getDateTo(),
                query.getViewerTimeZone(),
                query.getViewerTimezoneOffsetMinutes(),
                query.getWithImages(),
                query.getScheduledOnly(),
                query.getRadiusKm(),
                query.getSort(),
                query.getPage(),
                query.getSize()
        );
    }

    @GetMapping("/{id}")
    public QuestResponseDTO getQuestById(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        return questReadService.getQuestResponseById(id, currentUser);
    }

    @GetMapping("/{id}/detail")
    public QuestDetailResponseDTO getQuestDetailById(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        return questReadService.getQuestDetailResponseById(id, currentUser);
    }

    @DeleteMapping("/{id}")
    public ActionResultDTO deleteQuest(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        questService.deleteQuest(id, currentUser);
        return ActionResults.of("DELETE_QUEST", "Quest deleted.");
    }

    @PutMapping("/{id}")
    public ActionResultDTO updateQuest(@PathVariable long id, @Valid @RequestBody QuestRequestDTO dto, @AuthenticationPrincipal AppUser currentUser) {
        questService.updateQuest(id, dto, currentUser);
        return ActionResults.of("UPDATE_QUEST", "Quest updated.");
    }

    @PatchMapping("/{id}/start")
    public ActionResultDTO startQuest(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        questService.startQuest(id, currentUser);
        return ActionResults.of("START_QUEST", "Quest started.");
    }

    @PatchMapping("/{id}/complete")
    public ActionResultDTO completeQuest(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        questService.completeQuest(id, currentUser);
        return ActionResults.of("COMPLETE_QUEST", "Quest completed.");
    }

    @PatchMapping("/{id}/cancel")
    public ActionResultDTO cancelQuest(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        questService.cancelQuest(id, currentUser);
        return ActionResults.of("CANCEL_QUEST", "Quest cancelled.");
    }

    @PatchMapping("/{id}/pause")
    public ActionResultDTO pauseQuest(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        questService.pauseQuest(id, currentUser);
        return ActionResults.of("PAUSE_QUEST", "Quest paused.");
    }

    @PatchMapping("/{id}/resume")
    public ActionResultDTO resumeQuest(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        questService.resumeQuest(id, currentUser);
        return ActionResults.of("RESUME_QUEST", "Quest resumed.");
    }

    @PatchMapping("/{id}/term/confirm")
    public QuestResponseDTO confirmQuestTermChange(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        return questService.toResponse(questService.confirmQuestTermChange(id, currentUser), currentUser);
    }

    @PatchMapping("/{id}/term/reject")
    public QuestResponseDTO rejectQuestTermChange(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        return questService.toResponse(questService.rejectQuestTermChange(id, currentUser), currentUser);
    }
}
