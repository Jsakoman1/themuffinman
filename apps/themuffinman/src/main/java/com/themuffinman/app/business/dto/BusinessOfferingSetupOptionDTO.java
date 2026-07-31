package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessOfferingSetupOptionDTO {
    private String value;
    private String label;
    private String helpText;
}
