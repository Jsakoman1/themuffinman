package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessOfferingSetupFieldDTO {
    private String name;
    private String label;
    private String helpText;
    private boolean required;
    private Integer minimum;
    private Integer maximum;
}
