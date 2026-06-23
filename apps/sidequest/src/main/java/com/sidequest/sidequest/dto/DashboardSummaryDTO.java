package com.sidequest.sidequest.dto;

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
public class DashboardSummaryDTO {
    private long questCount;
    private long visibleMyQuestsCount;
    private long pendingWorkApplicationsCount;
    private long activeWorkApplicationsCount;
    private long activeMyQuestsCount;
    private long activeWorkCount;
    private long completedMyQuestsCount;
    private long openQuestCount;
    private long assignedQuestCount;
    private long waitingConfirmationQuestCount;
    private long unreadNewsCount;
    private long totalUserCount;
    private long adminUserCount;
}
