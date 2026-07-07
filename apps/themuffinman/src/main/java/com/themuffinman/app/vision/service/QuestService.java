package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestRequestDTO;
import com.themuffinman.app.vision.model.Quest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestService {

    private final QuestReadService questReadService;
    private final CreateQuestUseCase createQuestUseCase;
    private final UpdateQuestUseCase updateQuestUseCase;
    private final DeleteQuestUseCase deleteQuestUseCase;
    private final StartQuestUseCase startQuestUseCase;
    private final CompleteQuestUseCase completeQuestUseCase;
    private final ConfirmQuestTermChangeUseCase confirmQuestTermChangeUseCase;
    private final RejectQuestTermChangeUseCase rejectQuestTermChangeUseCase;
    public Quest createQuest(QuestRequestDTO dto, AppUser currentUser) {
        return createQuestUseCase.execute(dto, currentUser);
    }

    public java.util.List<Quest> getAllQuests(AppUser currentUser) {
        return questReadService.getAllQuests(currentUser);
    }

    public com.themuffinman.app.vision.dto.QuestListResponseDTO searchQuests(
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
        return questReadService.searchQuests(
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

    public com.themuffinman.app.vision.dto.QuestListResponseDTO getQuestListPreset(
            com.themuffinman.app.vision.dto.QuestListPresetDTO preset,
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
        return questReadService.getQuestListPreset(
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

    public Quest getQuestById(Long id, AppUser currentUser) {
        return questReadService.getQuestById(id, currentUser);
    }

    public com.themuffinman.app.vision.dto.QuestDetailResponseDTO getQuestDetailResponseById(Long id, AppUser currentUser) {
        return questReadService.getQuestDetailResponseById(id, currentUser);
    }

    @Transactional
    public void deleteQuest(Long id, AppUser currentUser) {
        deleteQuestUseCase.execute(id, currentUser);
    }

    @Transactional
    public Quest updateQuest(Long id, QuestRequestDTO dto, AppUser currentUser) {
        return updateQuestUseCase.execute(id, dto, currentUser);
    }

    @Transactional
    public Quest startQuest(Long id, AppUser currentUser) {
        return startQuestUseCase.execute(id, currentUser);
    }

    @Transactional
    public Quest completeQuest(Long id, AppUser currentUser) {
        return completeQuestUseCase.execute(id, currentUser);
    }

    @Transactional
    public Quest confirmQuestTermChange(Long id, AppUser currentUser) {
        return confirmQuestTermChangeUseCase.execute(id, currentUser);
    }

    @Transactional
    public Quest rejectQuestTermChange(Long id, AppUser currentUser) {
        return rejectQuestTermChangeUseCase.execute(id, currentUser);
    }

}
