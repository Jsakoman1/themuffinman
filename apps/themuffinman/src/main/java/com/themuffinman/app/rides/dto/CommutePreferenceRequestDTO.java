package com.themuffinman.app.rides.dto;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.LocalTime;
import java.util.List;

@Data
public class CommutePreferenceRequestDTO {
    private boolean enabled;
    private boolean consentGranted;
    @Nullable private String homeArea;
    @Nullable private String workArea;
    @Nullable private List<Integer> weekdays;
    @Nullable private LocalTime departureTime;
    @Nullable private LocalTime returnTime;
}
