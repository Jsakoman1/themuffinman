package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestStatusFilterOptionDTO {
    private String value;
    private String label;
}
