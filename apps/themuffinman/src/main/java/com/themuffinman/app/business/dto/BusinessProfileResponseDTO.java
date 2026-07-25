package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import org.springframework.lang.Nullable;

@Data
@Builder
public class BusinessProfileResponseDTO {
    private Long id;
    private Long ownerId;
    private String ownerUsername;
    private String businessName;
    private String slug;
    private String headline;
    private String description;
    private String contactEmail;
    private String contactPhone;
    private String websiteUrl;
    private String timezone;
    private boolean bookingEnabled;
    private String publicAddressLabel;
    @Nullable
    private Double latitude;
    @Nullable
    private Double longitude;
    private String contactWhatsapp;
    private String heroImageUrl;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
