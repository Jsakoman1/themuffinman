package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BusinessServiceSchemaDTO {
    private Long businessOfferingId;
    private int schemaVersion;
    private List<BusinessDemandFieldDTO> demandFields;
    private List<BusinessOfferingOptionDTO> options;
    private List<BusinessPricingRuleDTO> pricingRules;
}
