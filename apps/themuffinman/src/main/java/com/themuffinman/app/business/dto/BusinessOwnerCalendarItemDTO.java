package com.themuffinman.app.business.dto;

import com.themuffinman.app.business.model.BusinessBookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class BusinessOwnerCalendarItemDTO {
    private Long bookingId;
    private String businessOfferingTitle;
    private String customerUsername;
    private Instant startsAt;
    private Instant endsAt;
    private String timezone;
    private BusinessBookingStatus status;
    private String statusLabel;
    private String blockingReason;
    private List<BusinessBookingAllowedActionDTO> allowedActions;
}
