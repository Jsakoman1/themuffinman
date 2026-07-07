package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestListPresetDTO;
import com.themuffinman.app.vision.dto.QuestListResponseDTO;
import com.themuffinman.app.vision.dto.QuestRequestDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestMgr;
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
    private final WorkmarketQuestMgr questMgr;

    public com.themuffinman.app.vision.model.Quest createQuest(QuestRequestDTO dto, AppUser currentUser) {
        return questMgr.toVisionEntity(workmarketCreateQuestUseCase.execute(dto, currentUser));
    }

    public java.util.List<com.themuffinman.app.vision.model.Quest> getAllQuests(AppUser currentUser) {
        return workmarketQuestReadService.getAllQuests(currentUser).stream()
                .map(quest -> questMgr.toVisionEntity(quest))
                .toList();
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

    public com.themuffinman.app.vision.model.Quest getQuestById(Long id, AppUser currentUser) {
        return questMgr.toVisionEntity(workmarketQuestReadService.getQuestById(id, currentUser));
    }

    public QuestResponseDTO toResponse(com.themuffinman.app.vision.model.Quest quest, AppUser currentUser) {
        return workmarketQuestReadService.toResponse(questMgr.toWorkmarketEntity(quest), currentUser);
    }

    @Transactional
    public void deleteQuest(Long id, AppUser currentUser) {
        workmarketDeleteQuestUseCase.execute(id, currentUser);
    }

    @Transactional
    public com.themuffinman.app.vision.model.Quest updateQuest(Long id, QuestRequestDTO dto, AppUser currentUser) {
        return questMgr.toVisionEntity(workmarketUpdateQuestUseCase.execute(id, dto, currentUser));
    }

    @Transactional
    public com.themuffinman.app.vision.model.Quest startQuest(Long id, AppUser currentUser) {
        return questMgr.toVisionEntity(workmarketStartQuestUseCase.execute(id, currentUser));
    }

    @Transactional
    public com.themuffinman.app.vision.model.Quest completeQuest(Long id, AppUser currentUser) {
        return questMgr.toVisionEntity(workmarketCompleteQuestUseCase.execute(id, currentUser));
    }

    @Transactional
    public com.themuffinman.app.vision.model.Quest confirmQuestTermChange(Long id, AppUser currentUser) {
        return questMgr.toVisionEntity(workmarketConfirmQuestTermChangeUseCase.execute(id, currentUser));
    }

    @Transactional
    public com.themuffinman.app.vision.model.Quest rejectQuestTermChange(Long id, AppUser currentUser) {
        return questMgr.toVisionEntity(workmarketRejectQuestTermChangeUseCase.execute(id, currentUser));
    }
}
