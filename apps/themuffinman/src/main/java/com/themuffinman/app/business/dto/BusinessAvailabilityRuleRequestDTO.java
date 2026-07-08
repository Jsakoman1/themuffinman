package com.themuffinman.app.business.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAvailabilityRuleRequestDTO {

    private Long businessOfferingId;

    @NotNull(message = "Day of week is required")
    @Min(value = 1, message = "Day of week must be between 1 and 7")
    @Max(value = 7, message = "Day of week must be between 1 and 7")
    private Integer dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTimeLocal;

    @NotNull(message = "End time is required")
    private LocalTime endTimeLocal;

    @NotNull(message = "Slot granularity is required")
    @Min(value = 5, message = "Slot granularity must be at least 5 minutes")
    private Integer slotGranularityMinutes;

    @Min(value = 1, message = "Capacity override must be at least 1")
    private Integer capacityOverride;

    private LocalDate validFrom;

    private LocalDate validUntil;

    private Boolean active;
}
