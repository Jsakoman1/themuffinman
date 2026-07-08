package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BusinessPublicPageDTO {
    private Long businessProfileId;
    private String businessName;
    private String slug;
    private String headline;
    private String description;
    private String publicAddressLabel;
    private Double latitude;
    private Double longitude;
    private String contactEmail;
    private String contactPhone;
    private String contactWhatsapp;
    private String websiteUrl;
    private String heroImageUrl;
    private String timezone;
    private boolean bookingEnabled;
    private List<BusinessOfferingResponseDTO> offerings;
    private List<BusinessGalleryImageResponseDTO> galleryImages;
}
