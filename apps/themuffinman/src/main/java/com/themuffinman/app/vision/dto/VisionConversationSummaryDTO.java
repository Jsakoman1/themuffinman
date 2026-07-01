package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionConversationSummaryDTO {
    private Long conversationId;
    private String intent;
    private String status;
    private String title;
    private String subtitle;
    private String stageLabel;
    private String progressLabel;
    private String groupKey;
    private String requestedSlot;
    private List<VisionSlotSummaryDTO> appliedSlotSummaries;
    private boolean resumable;
    private boolean completed;
    private boolean stale;
    private Instant updatedAt;
}
