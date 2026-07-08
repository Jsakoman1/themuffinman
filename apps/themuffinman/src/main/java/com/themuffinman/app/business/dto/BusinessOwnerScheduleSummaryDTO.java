package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BusinessOwnerScheduleSummaryDTO {
    private String timezone;
    private int todayCount;
    private int pendingConfirmationCount;
    private int upcomingCount;
    private List<BusinessOwnerScheduleItemDTO> nextItems;
}
