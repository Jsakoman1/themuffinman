package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessBookingPresentationDTO {
    private String statusLabel;
    private String blockingReason;
}
