package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BusinessFavoriteResponseDTO {
    private Long id;
    private Long businessProfileId;
    private String businessName;
    private String slug;
    private boolean bookingEnabled;
    private Instant createdAt;
}
