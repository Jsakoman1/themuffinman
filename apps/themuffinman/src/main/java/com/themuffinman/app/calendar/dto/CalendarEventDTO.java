package com.themuffinman.app.calendar.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CalendarEventDTO {
    private String eventKey;
    private String source;
    private String title;
    private Instant startsAt;
    private Instant endsAt;
    private String timezone;
    private String startsAtLocal;
    private String endsAtLocal;
    private String status;
    private Long businessId;
    private String businessName;
    private String navigationPath;
    private boolean allDay;
}
