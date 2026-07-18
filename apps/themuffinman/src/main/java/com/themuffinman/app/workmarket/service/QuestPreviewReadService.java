package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestPreviewResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestPreviewReadService {

    private final WorkmarketQuestReadService questReadService;

    public QuestPreviewResponseDTO getPreview(long questId, AppUser viewer) {
        return QuestPreviewResponseDTO.from(questReadService.getQuestResponseById(questId, viewer));
    }
}
