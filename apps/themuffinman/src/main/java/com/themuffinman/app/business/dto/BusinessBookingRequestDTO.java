package com.themuffinman.app.business.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class BusinessBookingRequestDTO {

    @NotNull(message = "Offering id is required")
    private Long businessOfferingId;

    @NotNull(message = "Booking start is required")
    private Instant startsAt;

    @NotNull(message = "Booking end is required")
    private Instant endsAt;

    @Size(max = 1000, message = "Customer note must be 1000 characters or fewer")
    private String customerNote;

    @Size(max = 120, message = "Idempotency key must be 120 characters or fewer")
    private String idempotencyKey;
}
