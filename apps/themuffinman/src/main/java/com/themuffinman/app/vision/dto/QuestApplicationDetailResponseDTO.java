package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestApplicationDetailResponseDTO {
    private QuestApplicationResponseDTO summary;
    private QuestApplicationDetailSectionsDTO sections;
    private QuestApplicationResponseDTO application;
    private QuestResponseDTO quest;
}
