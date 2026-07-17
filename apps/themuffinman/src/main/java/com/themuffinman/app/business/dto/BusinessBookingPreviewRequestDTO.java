package com.themuffinman.app.business.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class BusinessBookingPreviewRequestDTO {
    @NotNull
    private Long businessOfferingId;
    @NotNull
    private Instant startsAt;
}
