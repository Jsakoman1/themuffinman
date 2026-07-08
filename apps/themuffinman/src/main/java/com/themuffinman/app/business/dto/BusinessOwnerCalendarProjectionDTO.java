package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class BusinessOwnerCalendarProjectionDTO {
    private String timezone;
    private Instant from;
    private Instant to;
    private int totalBookings;
    private List<BusinessOwnerCalendarDayDTO> days;
}
