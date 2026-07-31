package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class BusinessPublicAvailabilityCalendarDTO {
    private Long businessOfferingId;
    private String view;
    private String timezone;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<BusinessPublicAvailabilityDayDTO> days;
}
