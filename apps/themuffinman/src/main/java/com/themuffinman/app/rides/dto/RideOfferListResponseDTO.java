package com.themuffinman.app.rides.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RideOfferListResponseDTO {
    private List<RideOfferResponseDTO> items;
}
