package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.common.validation.RichTextInputValidator;
import lombok.Builder;

import java.util.List;

@Builder
public record QuestPreviewResponseDTO(
        long id,
        String title,
        String summary,
        String creatorUsername,
        String status,
        QuestViewerRelationDTO viewerRelation,
        List<QuestAllowedActionDTO> allowedActions,
        boolean canOpenDetail
) {
    public static QuestPreviewResponseDTO from(QuestResponseDTO source) {
        return QuestPreviewResponseDTO.builder()
                .id(source.getId())
                .title(source.getTitle())
                .summary(RichTextInputValidator.extractPlainText(source.getDescription()))
                .creatorUsername(source.getCreatorUsername())
                .status(source.getStatus().name())
                .viewerRelation(source.getViewerRelation())
                .allowedActions(source.getAllowedActions())
                .canOpenDetail(true)
                .build();
    }
}
