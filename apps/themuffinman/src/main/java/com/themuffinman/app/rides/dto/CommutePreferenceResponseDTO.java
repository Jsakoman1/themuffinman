package com.themuffinman.app.rides.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class CommutePreferenceResponseDTO {
    private Long id;
    private boolean enabled;
    private boolean consentGranted;
    private String homeArea;
    private String workArea;
    private List<Integer> weekdays;
    private LocalTime departureTime;
    private LocalTime returnTime;
    private Instant updatedAt;
}
