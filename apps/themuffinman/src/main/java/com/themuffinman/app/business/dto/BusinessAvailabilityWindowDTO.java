package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BusinessAvailabilityWindowDTO {
    private Long businessProfileId;
    private Long businessOfferingId;
    private String businessOfferingTitle;
    private Instant startsAt;
    private Instant endsAt;
    private String timezone;
    private int effectiveCapacity;
}
