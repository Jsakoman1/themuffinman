package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.workmarket.model.QuestStatus;
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
