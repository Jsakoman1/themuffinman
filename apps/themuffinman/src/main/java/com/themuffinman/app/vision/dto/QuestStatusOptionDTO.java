package com.themuffinman.app.vision.dto;

import com.themuffinman.app.vision.model.QuestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestStatusOptionDTO {
    private QuestStatus value;
    private String label;
}
