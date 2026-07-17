package com.themuffinman.app.identity.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProfileGalleryImageListResponseDTO {
    private List<ProfileGalleryImageResponseDTO> items;
}
