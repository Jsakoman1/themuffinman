package com.themuffinman.app.business.dto;

import com.themuffinman.app.business.model.BusinessAvailabilityExceptionType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalTime;

@Data
@Builder
public class BusinessAvailabilityExceptionResponseDTO {
    private Long id;
    private Long businessProfileId;
    private Long businessOfferingId;
    private String businessOfferingTitle;
    private BusinessAvailabilityExceptionType exceptionType;
    private Instant startAt;
    private Instant endAt;
    private Integer replacementCapacity;
    private LocalTime replacementStartTimeLocal;
    private LocalTime replacementEndTimeLocal;
    private String reason;
    private String timezone;
    private Instant createdAt;
    private Instant updatedAt;
}
