package com.themuffinman.app.calendar.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CalendarProjectionDTO {
    private Instant from;
    private Instant to;
    private String view;
    private String rangeKind;
    private String timezone;
    private List<String> availableSources;
    private List<CalendarEventDTO> events;
}
