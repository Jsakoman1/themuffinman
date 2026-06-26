package com.themuffinman.app.location.dto;

import com.themuffinman.app.location.model.QuestLocationVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestLocationVisibilityOptionDTO {
    private QuestLocationVisibility value;
    private String label;
    private String description;
}
