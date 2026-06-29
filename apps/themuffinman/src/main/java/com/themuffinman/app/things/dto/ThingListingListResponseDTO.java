package com.themuffinman.app.things.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ThingListingListResponseDTO {
    private List<ThingListingResponseDTO> items;
}
