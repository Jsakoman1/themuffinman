package com.themuffinman.app.identity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ProfileGalleryImageResponseDTO {
    private Long id;
    private Long ownerId;
    private String imageUrl;
    private String altText;
    private int sortOrder;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
