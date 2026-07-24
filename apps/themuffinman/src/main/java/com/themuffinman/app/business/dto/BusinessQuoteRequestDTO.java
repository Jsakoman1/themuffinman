package com.themuffinman.app.business.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
public class BusinessQuoteRequestDTO {
    @NotNull
    private Long businessOfferingId;
    private Instant startsAt;
    private Integer durationMinutes;
    @DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;
    private Integer schemaVersion;
    private Map<String, String> answers;
    private Map<String, String> selectedOptions;
}
