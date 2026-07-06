package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.vision.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestListPresetDTO;
import com.themuffinman.app.vision.dto.QuestListResponseDTO;
import com.themuffinman.app.vision.dto.QuestRequestDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisionQuestFacadeService {

    private final WorkmarketQuestService questService;

    public ActionResultDTO createQuest(QuestRequestDTO dto, AppUser currentUser) {
        questService.createQuest(dto, currentUser);
        return ActionResults.of("CREATE_QUEST", "Quest created.");
    }

    public List<QuestResponseDTO> getAllQuests(AppUser currentUser) {
        return questService.getAllQuestResponses(currentUser);
    }

    public QuestListResponseDTO searchQuests(
            AppUser currentUser,
            String query,
            com.themuffinman.app.vision.model.QuestStatus status,
            com.themuffinman.app.vision.model.QuestAudience audience,
            java.time.LocalDate dateFrom,
            java.time.LocalDate dateTo,
            String viewerTimeZone,
            Integer viewerTimezoneOffsetMinutes,
            Boolean excludeMine,
            Boolean withImages,
            Boolean scheduledOnly,
            Integer radiusKm,
            String sort,
            Integer page,
            Integer size
    ) {
        return questService.searchQuests(
                currentUser,
                query,
                status,
                audience,
                dateFrom,
                dateTo,
                viewerTimeZone,
                viewerTimezoneOffsetMinutes,
                excludeMine,
                withImages,
                scheduledOnly,
                radiusKm,
                sort,
                page,
                size
        );
    }

    public QuestListResponseDTO getQuestListPreset(
            QuestListPresetDTO preset,
            AppUser currentUser,
            String query,
            com.themuffinman.app.vision.model.QuestAudience audience,
            java.time.LocalDate dateFrom,
            java.time.LocalDate dateTo,
            String viewerTimeZone,
            Integer viewerTimezoneOffsetMinutes,
            Boolean withImages,
            Boolean scheduledOnly,
            Integer radiusKm,
            String sort,
            Integer page,
            Integer size
    ) {
        return questService.getQuestListPreset(
                preset,
                currentUser,
                query,
                audience,
                dateFrom,
                dateTo,
                viewerTimeZone,
                viewerTimezoneOffsetMinutes,
                withImages,
                scheduledOnly,
                radiusKm,
                sort,
                page,
                size
        );
    }

    public QuestResponseDTO getQuestResponseById(Long id, AppUser currentUser) {
        return questService.getQuestResponseById(id, currentUser);
    }

    public QuestDetailResponseDTO getQuestDetailResponseById(Long id, AppUser currentUser) {
        return questService.getQuestDetailResponseById(id, currentUser);
    }

    public QuestApplicationDetailResponseDTO getApplicationDetailResponseById(Long applicationId, AppUser currentUser) {
        return questService.getApplicationDetailResponseById(applicationId, currentUser);
    }

    public ActionResultDTO deleteQuest(Long id, AppUser currentUser) {
        questService.deleteQuest(id, currentUser);
        return ActionResults.of("DELETE_QUEST", "Quest deleted.");
    }

    public ActionResultDTO updateQuest(Long id, QuestRequestDTO dto, AppUser currentUser) {
        questService.updateQuest(id, dto, currentUser);
        return ActionResults.of("UPDATE_QUEST", "Quest updated.");
    }

    public ActionResultDTO startQuest(Long id, AppUser currentUser) {
        questService.startQuest(id, currentUser);
        return ActionResults.of("START_QUEST", "Quest started.");
    }

    public ActionResultDTO completeQuest(Long id, AppUser currentUser) {
        questService.completeQuest(id, currentUser);
        return ActionResults.of("COMPLETE_QUEST", "Quest completed.");
    }

    public QuestResponseDTO confirmQuestTermChange(Long id, AppUser currentUser) {
        return questService.toResponse(questService.confirmQuestTermChange(id, currentUser), currentUser);
    }

    public QuestResponseDTO rejectQuestTermChange(Long id, AppUser currentUser) {
        return questService.toResponse(questService.rejectQuestTermChange(id, currentUser), currentUser);
    }
}
