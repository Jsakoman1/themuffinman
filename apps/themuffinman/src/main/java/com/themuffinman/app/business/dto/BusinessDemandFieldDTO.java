package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessDemandFieldDTO {
    private Long id;
    private String fieldKey;
    private String label;
    private String description;
    private String valueType;
    private boolean required;
    private boolean customerVisible;
    private Integer retentionDays;
    private Object validationSchema;
    private int sortOrder;
}
