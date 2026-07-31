package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessOfferingSetupStepDTO {
    private String id;
    private String title;
    private String description;
}
