package com.themuffinman.app.business.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class BusinessBookingRequestDTO {

    @NotNull(message = "Offering id is required")
    private Long businessOfferingId;

    @NotNull(message = "Booking start is required")
    private Instant startsAt;

    private Instant endsAt;

    @Size(max = 1000, message = "Customer note must be 1000 characters or fewer")
    private String customerNote;

    @Size(max = 120, message = "Idempotency key must be 120 characters or fewer")
    private String idempotencyKey;

    @jakarta.validation.constraints.DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    private Map<String, String> answers;
    private Map<String, String> selectedOptions;
}
