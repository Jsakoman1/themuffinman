package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class BusinessQuoteResponseDTO {
    private Long businessOfferingId;
    private String offeringTitle;
    private String pricingState;
    private BigDecimal totalAmount;
    private String currency;
    private BigDecimal quantity;
    private Integer durationMinutes;
    private Instant startsAt;
    private Instant endsAt;
    private String timezone;
    private boolean ownerReviewRequired;
    private int schemaVersion;
    private List<String> explanations;
}
