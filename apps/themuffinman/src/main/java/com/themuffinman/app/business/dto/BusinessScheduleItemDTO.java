package com.themuffinman.app.business.dto;

import com.themuffinman.app.business.model.BusinessBookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class BusinessScheduleItemDTO {
    private Long bookingId;
    private String role;
    private Long businessProfileId;
    private String businessName;
    private String businessSlug;
    private String businessOfferingTitle;
    private Instant startsAt;
    private Instant endsAt;
    private String timezone;
    private BusinessBookingStatus status;
    private String statusLabel;
    private List<BusinessBookingAllowedActionDTO> allowedActions;
}
