package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestApplicationDetailSectionsDTO {
    private QuestResponseDTO quest;
    private QuestApplicationDetailNavigationSectionDTO navigation;
    private QuestApplicationDetailContextSectionDTO context;
}
