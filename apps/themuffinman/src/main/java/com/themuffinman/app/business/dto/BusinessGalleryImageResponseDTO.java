package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BusinessGalleryImageResponseDTO {
    private Long id;
    private Long businessProfileId;
    private String imageUrl;
    private String altText;
    private int sortOrder;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
