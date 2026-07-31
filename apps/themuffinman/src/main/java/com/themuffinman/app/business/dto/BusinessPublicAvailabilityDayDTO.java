package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class BusinessPublicAvailabilityDayDTO {
    private LocalDate date;
    private String availabilityState;
    private int availableSlotCount;
    private List<BusinessPublicAvailabilitySlotDTO> slots;
}
