package com.themuffinman.app.workmarket.controller;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestListPreset;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.service.QuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;

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
            @RequestParam(required = false) String q,
            @RequestParam(required = false) QuestStatus status,
            @RequestParam(required = false) QuestAudience audience,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) Boolean excludeMine,
            @RequestParam(required = false) Boolean withImages,
            @RequestParam(required = false) Boolean scheduledOnly,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return questService.searchQuests(currentUser, q, status, audience, dateFrom, dateTo, excludeMine, withImages, scheduledOnly, sort, page, size);
    }

    @GetMapping("/presets/{preset}")
    public QuestListResponseDTO getQuestPreset(
            @PathVariable QuestListPreset preset,
            @AuthenticationPrincipal AppUser currentUser,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) QuestAudience audience,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) Boolean withImages,
            @RequestParam(required = false) Boolean scheduledOnly,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return questService.getQuestListPreset(preset, currentUser, q, audience, dateFrom, dateTo, withImages, scheduledOnly, sort, page, size);
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
