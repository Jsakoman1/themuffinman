package com.themuffinman.app.vision.dto;

import com.themuffinman.app.vision.model.QuestAudience;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestAudienceOptionDTO {
    private QuestAudience value;
    private String label;
    private String description;
}
