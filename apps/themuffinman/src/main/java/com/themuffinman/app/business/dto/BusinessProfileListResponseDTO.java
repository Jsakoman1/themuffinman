package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BusinessProfileListResponseDTO {
    private List<BusinessProfileResponseDTO> items;
}
