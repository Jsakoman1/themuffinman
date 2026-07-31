package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BusinessReviewListResponseDTO {
    private List<BusinessReviewResponseDTO> items;
    private int page;
    private int size;
}
