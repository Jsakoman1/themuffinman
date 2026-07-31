package com.themuffinman.app.calendar.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalendarSourceDTO {
    private String key;
    private String label;
    private String color;
}
