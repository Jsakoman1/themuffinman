package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessOfferingOptionDTO {
    private Long id;
    private String optionKey;
    private String label;
    private String description;
    private String valueType;
    private boolean required;
    private int sortOrder;
}
