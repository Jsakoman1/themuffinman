package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BusinessOfferingSetupDTO {
    private String contractVersion;
    private BusinessOfferingRequestDTO defaults;
    private List<BusinessOfferingSetupOptionDTO> pricingTypes;
    private List<BusinessOfferingSetupOptionDTO> bookingModes;
    private List<BusinessOfferingSetupOptionDTO> fulfillmentModes;
    private List<BusinessOfferingSetupOptionDTO> durationOptions;
    private List<BusinessOfferingSetupStepDTO> steps;
    private List<BusinessOfferingSetupFieldDTO> fields;
    private boolean readyForBookings;
    private String readinessMessage;
}
