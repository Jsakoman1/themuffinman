package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionQuestReviewDTO {
    private String title;
    private String description;
    private String rewardLabel;
    private String visibility;
    private String schedule;
    private String location;
}
