package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BusinessOwnerScheduleItemDTO {
    private Long bookingId;
    private String businessOfferingTitle;
    private String customerUsername;
    private Instant startsAt;
    private Instant endsAt;
    private String timezone;
    private String statusLabel;
}
