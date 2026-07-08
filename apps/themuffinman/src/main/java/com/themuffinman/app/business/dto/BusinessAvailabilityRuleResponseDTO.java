package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class BusinessAvailabilityRuleResponseDTO {
    private Long id;
    private Long businessProfileId;
    private Long businessOfferingId;
    private String businessOfferingTitle;
    private int dayOfWeek;
    private LocalTime startTimeLocal;
    private LocalTime endTimeLocal;
    private int slotGranularityMinutes;
    private Integer capacityOverride;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private boolean active;
    private String timezone;
    private Instant createdAt;
    private Instant updatedAt;
}
