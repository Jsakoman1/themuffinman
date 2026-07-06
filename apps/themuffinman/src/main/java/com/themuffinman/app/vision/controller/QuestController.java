package com.themuffinman.app.vision.controller;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.vision.dto.QuestDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestListPresetDTO;
import com.themuffinman.app.vision.dto.QuestListResponseDTO;
import com.themuffinman.app.vision.dto.QuestRequestDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.vision.dto.QuestSearchRequestDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.service.VisionQuestFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quests")
@RequiredArgsConstructor
public class QuestController {

    private final VisionQuestFacadeService questService;

    @PostMapping
    public ActionResultDTO createQuest(@Valid @RequestBody QuestRequestDTO dto, @AuthenticationPrincipal AppUser currentUser) {
        return questService.createQuest(dto, currentUser);
    }

    @GetMapping
    public List<QuestResponseDTO> getAllQuests(@AuthenticationPrincipal AppUser currentUser) {
        return questService.getAllQuests(currentUser);
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
        return questService.deleteQuest(id, currentUser);
    }

    @PutMapping("/{id}")
    public ActionResultDTO updateQuest(@PathVariable long id, @Valid @RequestBody QuestRequestDTO dto, @AuthenticationPrincipal AppUser currentUser) {
        return questService.updateQuest(id, dto, currentUser);
    }

    @PatchMapping("/{id}/start")
    public ActionResultDTO startQuest(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        return questService.startQuest(id, currentUser);
    }

    @PatchMapping("/{id}/complete")
    public ActionResultDTO completeQuest(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        return questService.completeQuest(id, currentUser);
    }

    @PatchMapping("/{id}/term/confirm")
    public QuestResponseDTO confirmQuestTermChange(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        return questService.confirmQuestTermChange(id, currentUser);
    }

    @PatchMapping("/{id}/term/reject")
    public QuestResponseDTO rejectQuestTermChange(@PathVariable long id, @AuthenticationPrincipal AppUser currentUser) {
        return questService.rejectQuestTermChange(id, currentUser);
    }

}
