package com.themuffinman.app.business.dto;

import com.themuffinman.app.business.model.BusinessAvailabilityExceptionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAvailabilityExceptionRequestDTO {

    private Long businessOfferingId;

    @NotNull(message = "Exception type is required")
    private BusinessAvailabilityExceptionType exceptionType;

    @NotNull(message = "Start time is required")
    private Instant startAt;

    @NotNull(message = "End time is required")
    private Instant endAt;

    @Min(value = 1, message = "Replacement capacity must be at least 1")
    private Integer replacementCapacity;

    private LocalTime replacementStartTimeLocal;

    private LocalTime replacementEndTimeLocal;

    @Size(max = 240, message = "Reason must be 240 characters or less")
    private String reason;
}
