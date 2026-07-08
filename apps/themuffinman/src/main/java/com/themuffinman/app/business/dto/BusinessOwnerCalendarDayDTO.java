package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BusinessOwnerCalendarDayDTO {
    private LocalDate date;
    private int bookingCount;
    private List<BusinessOwnerCalendarItemDTO> items;
}
