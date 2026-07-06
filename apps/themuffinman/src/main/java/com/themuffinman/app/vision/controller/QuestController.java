package com.themuffinman.app.vision.controller;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.vision.dto.QuestDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestListPresetDTO;
import com.themuffinman.app.vision.dto.QuestListResponseDTO;
import com.themuffinman.app.vision.dto.QuestRequestDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.vision.dto.QuestSearchRequestDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.service.WorkmarketQuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quests")
@RequiredArgsConstructor
public class QuestController {

    private final WorkmarketQuestService questService;

    @PostMapping
    public ActionResultDTO createQuest(@Valid @RequestBody QuestRequestDTO dto, @AuthenticationPrincipal AppUser currentUser) {
        questService.createQuest(dto, currentUser);
        return ActionResults.of("CREATE_QUEST", "Quest created.");
    }

    @GetMapping
    public List<QuestResponseDTO> getAllQuests(@AuthenticationPrincipal AppUser currentUser) {
        return questService.getAllQuestResponses(currentUser);
    }

    @GetMapping("/search")
    public QuestListResponseDTO searchQuests(
            @AuthenticationPrincipal AppUser currentUser,
            @ModelAttribute QuestSearchRequestDTO query
    ) {
        return questService.searchQuests(
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
        return questService.getQuestListPreset(
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
        return questService.getQuestResponseById(id, currentUser);
    }

    @GetMapping("/{id}/detail")
    public QuestDetailResponseDTO getQuestDetailById(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        return questService.getQuestDetailResponseById(id, currentUser);
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

    @PatchMapping("/{id}/term/confirm")
    public QuestResponseDTO confirmQuestTermChange(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        return questService.toResponse(questService.confirmQuestTermChange(id, currentUser), currentUser);
    }

    @PatchMapping("/{id}/term/reject")
    public QuestResponseDTO rejectQuestTermChange(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        return questService.toResponse(questService.rejectQuestTermChange(id, currentUser), currentUser);
    }

}
