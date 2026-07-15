package com.themuffinman.app.workmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestListPresentationDTO {
    private String archetype;
    private String density;
    private String primaryActionLabel;
    private List<String> visibleFields;
}
