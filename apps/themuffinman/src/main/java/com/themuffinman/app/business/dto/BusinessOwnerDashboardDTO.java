package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessOwnerDashboardDTO {
    private Long businessProfileId;
    private String businessName;
    private String slug;
    private boolean bookingEnabled;
    private int activeOfferingCount;
    private int pendingConfirmationCount;
    private int todayCount;
    private int upcomingCount;
    private int staleThresholdMinutes;
    private BusinessOwnerScheduleSummaryDTO scheduleSummary;
}
