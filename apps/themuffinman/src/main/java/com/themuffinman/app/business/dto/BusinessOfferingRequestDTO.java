package com.themuffinman.app.business.dto;

import com.themuffinman.app.business.model.BusinessOfferingBookingMode;
import com.themuffinman.app.business.model.BusinessOfferingCapacityMode;
import com.themuffinman.app.business.model.BusinessOfferingDurationMode;
import com.themuffinman.app.business.model.BusinessOfferingPricingType;
import com.themuffinman.app.business.model.BusinessOfferingFulfillmentMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessOfferingRequestDTO {

    @Size(max = 120, message = "Offering title must be 120 characters or less")
    private String title;

    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Offering slug must use lowercase letters, numbers, and hyphens")
    @Size(max = 140, message = "Offering slug must be 140 characters or less")
    private String slug;

    @Size(max = 240, message = "Offering summary must be 240 characters or less")
    private String summary;

    @Size(max = 4000, message = "Offering description must be 4000 characters or less")
    private String description;

    private BusinessOfferingPricingType pricingType;

    @DecimalMin(value = "0.00", message = "Base price must be zero or greater")
    private BigDecimal basePriceAmount;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Base price currency must be a three-letter ISO code")
    private String basePriceCurrency;

    private BusinessOfferingDurationMode durationMode;

    @Min(value = 1, message = "Default duration must be at least 1 minute")
    private Integer defaultDurationMinutes;

    @Min(value = 1, message = "Minimum duration must be at least 1 minute")
    private Integer minDurationMinutes;

    @Min(value = 1, message = "Maximum duration must be at least 1 minute")
    private Integer maxDurationMinutes;

    private BusinessOfferingCapacityMode capacityMode;

    @Min(value = 1, message = "Slot capacity must be at least 1")
    private Integer slotCapacity;

    private BusinessOfferingBookingMode bookingMode;

    private BusinessOfferingFulfillmentMode fulfillmentMode;

    @Min(value = 1, message = "Duration increment must be at least 1 minute")
    private Integer durationIncrementMinutes;

    @Min(value = 1, message = "Minimum quantity must be at least 1")
    private Integer minimumQuantity;

    @Min(value = 1, message = "Maximum quantity must be at least 1")
    private Integer maximumQuantity;

    private Boolean requiresOwnerConfirmation;

    @Min(value = 0, message = "Buffer before must be zero or greater")
    private Integer bufferBeforeMinutes;

    @Min(value = 0, message = "Buffer after must be zero or greater")
    private Integer bufferAfterMinutes;

    private Boolean active;

    @Min(value = 0, message = "Sort order must be zero or greater")
    private Integer sortOrder;
}
