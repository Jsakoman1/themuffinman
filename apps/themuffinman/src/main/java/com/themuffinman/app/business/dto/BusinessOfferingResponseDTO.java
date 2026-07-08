package com.themuffinman.app.business.dto;

import com.themuffinman.app.business.model.BusinessOfferingBookingMode;
import com.themuffinman.app.business.model.BusinessOfferingCapacityMode;
import com.themuffinman.app.business.model.BusinessOfferingDurationMode;
import com.themuffinman.app.business.model.BusinessOfferingPricingType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class BusinessOfferingResponseDTO {
    private Long id;
    private Long businessProfileId;
    private String businessSlug;
    private String title;
    private String slug;
    private String summary;
    private String description;
    private BusinessOfferingPricingType pricingType;
    private BigDecimal basePriceAmount;
    private String basePriceCurrency;
    private BusinessOfferingDurationMode durationMode;
    private Integer defaultDurationMinutes;
    private Integer minDurationMinutes;
    private Integer maxDurationMinutes;
    private BusinessOfferingCapacityMode capacityMode;
    private Integer slotCapacity;
    private BusinessOfferingBookingMode bookingMode;
    private boolean requiresOwnerConfirmation;
    private int bufferBeforeMinutes;
    private int bufferAfterMinutes;
    private boolean active;
    private int sortOrder;
    private Instant createdAt;
    private Instant updatedAt;
}
