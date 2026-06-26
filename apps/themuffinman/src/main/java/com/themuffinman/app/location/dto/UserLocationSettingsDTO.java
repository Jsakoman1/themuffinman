package com.themuffinman.app.location.dto;

import com.themuffinman.app.location.model.ExactLocationVisibilityScope;
import com.themuffinman.app.location.model.UserLocationMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationSettingsDTO {
    private UserLocationMode mode;
    private Integer defaultRadiusKm;
    private boolean hasCoordinates;
    @Nullable
    private String sharingSummary;
    @Nullable
    private String visibilitySummary;
    private ExactLocationVisibilityScope exactVisibilityScope;
    @Nullable
    private List<Long> exactVisibleCircleIds;
    @Nullable
    private List<Long> exactVisibleUserIds;
    @Nullable
    private String provider;
    @Nullable
    private String providerPlaceId;
    @Nullable
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
    @Nullable
    private BigDecimal latitude;
    @Nullable
    private BigDecimal longitude;
    @Nullable
    private Instant resolvedAt;
    @Nullable
    private Instant updatedAt;
}
