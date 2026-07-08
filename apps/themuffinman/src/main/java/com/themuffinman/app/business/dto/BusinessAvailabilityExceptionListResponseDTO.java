package com.themuffinman.app.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAvailabilityExceptionListResponseDTO {
    private List<BusinessAvailabilityExceptionResponseDTO> items;
}
