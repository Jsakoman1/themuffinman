package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BusinessGalleryImageListResponseDTO {
    private List<BusinessGalleryImageResponseDTO> items;
}
