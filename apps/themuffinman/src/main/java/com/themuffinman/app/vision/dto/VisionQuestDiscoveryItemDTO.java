package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionQuestDiscoveryItemDTO {
    private Long questId;
    private int rank;
    private String title;
    private String description;
    private String creatorUsername;
    private String rewardLabel;
    private String statusLabel;
    private String locationLabel;
    private Instant scheduledAt;
    private String matchSummary;
}
