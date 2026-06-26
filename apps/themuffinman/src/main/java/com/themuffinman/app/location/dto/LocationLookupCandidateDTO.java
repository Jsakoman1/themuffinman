package com.themuffinman.app.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationLookupCandidateDTO {
    private String provider;
    @Nullable
    private String providerPlaceId;
    private String label;
    @Nullable
    private String countryCode;
    @Nullable
    private String country;
    @Nullable
    private String locality;
    @Nullable
    private String postalCode;
    @Nullable
    private String street;
    @Nullable
    private String houseNumber;
    private BigDecimal latitude;
    private BigDecimal longitude;
    @Nullable
    private Instant resolvedAt;
}
