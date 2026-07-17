package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestListPresetDTO;
import com.themuffinman.app.workmarket.dto.QuestListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("workmarketQuestService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkmarketQuestService {

    private final WorkmarketCreateQuestUseCase workmarketCreateQuestUseCase;
    private final WorkmarketUpdateQuestUseCase workmarketUpdateQuestUseCase;
    private final WorkmarketDeleteQuestUseCase workmarketDeleteQuestUseCase;
    private final WorkmarketStartQuestUseCase workmarketStartQuestUseCase;
    private final WorkmarketCompleteQuestUseCase workmarketCompleteQuestUseCase;
    private final WorkmarketConfirmQuestTermChangeUseCase workmarketConfirmQuestTermChangeUseCase;
    private final WorkmarketRejectQuestTermChangeUseCase workmarketRejectQuestTermChangeUseCase;
    private final WorkmarketQuestReadService workmarketQuestReadService;
    private final WorkmarketQuestUpdateService workmarketQuestUpdateService;

    @Transactional
    public Quest createQuest(QuestRequestDTO dto, AppUser currentUser) {
        return workmarketCreateQuestUseCase.execute(dto, currentUser);
    }

    public java.util.List<Quest> getAllQuests(AppUser currentUser) {
        return workmarketQuestReadService.getAllQuests(currentUser);
    }

    public QuestListResponseDTO searchQuests(
            AppUser currentUser,
            String query,
            QuestStatus status,
            QuestAudience audience,
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
        return workmarketQuestReadService.searchQuests(
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
            QuestAudience audience,
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
        return workmarketQuestReadService.getQuestListPreset(
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
        return workmarketQuestReadService.getQuestById(id, currentUser);
    }

    public QuestResponseDTO toResponse(Quest quest, AppUser currentUser) {
        return workmarketQuestReadService.toResponse(quest, currentUser);
    }

    @Transactional
    public void deleteQuest(Long id, AppUser currentUser) {
        workmarketDeleteQuestUseCase.execute(id, currentUser);
    }

    @Transactional
    public Quest updateQuest(Long id, QuestRequestDTO dto, AppUser currentUser) {
        return workmarketUpdateQuestUseCase.execute(id, dto, currentUser);
    }

    @Transactional
    public Quest startQuest(Long id, AppUser currentUser) {
        return workmarketStartQuestUseCase.execute(id, currentUser);
    }

    @Transactional
    public Quest completeQuest(Long id, AppUser currentUser) {
        return workmarketCompleteQuestUseCase.execute(id, currentUser);
    }

    @Transactional
    public Quest cancelQuest(Long id, AppUser currentUser) {
        return workmarketQuestUpdateService.cancelQuestForVision(id, currentUser);
    }

    @Transactional
    public Quest pauseQuest(Long id, AppUser currentUser) {
        return workmarketQuestUpdateService.pauseQuest(id, currentUser);
    }

    @Transactional
    public Quest resumeQuest(Long id, AppUser currentUser) {
        return workmarketQuestUpdateService.resumeQuest(id, currentUser);
    }

    @Transactional
    public Quest confirmQuestTermChange(Long id, AppUser currentUser) {
        return workmarketConfirmQuestTermChangeUseCase.execute(id, currentUser);
    }

    @Transactional
    public Quest rejectQuestTermChange(Long id, AppUser currentUser) {
        return workmarketRejectQuestTermChangeUseCase.execute(id, currentUser);
    }
}
