package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessRatingSummaryDTO {
    private double averageStars;
    private long reviewCount;
}
