package com.themuffinman.app.calendar.controller;

import com.themuffinman.app.calendar.dto.CalendarProjectionDTO;
import com.themuffinman.app.calendar.service.CalendarReadService;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/calendar")
public class CalendarController {
    private final CalendarReadService calendarReadService;

    @GetMapping
    public CalendarProjectionDTO getCalendar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) Long businessId,
            @AuthenticationPrincipal AppUser viewer
    ) {
        return calendarReadService.getCalendar(viewer, from, to, source, businessId);
    }
}
