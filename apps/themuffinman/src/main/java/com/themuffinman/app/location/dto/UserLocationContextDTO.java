package com.themuffinman.app.location.dto;

import com.themuffinman.app.location.model.LocationResolutionStatus;
import com.themuffinman.app.location.model.UserLocationMode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLocationContextDTO {
    private UserLocationMode mode;
    private LocationResolutionStatus resolutionStatus;
    private boolean hasCoordinates;
    private String countryCode;
    private String country;
    private String locality;
    private String locationLabel;
    private String approximateLocationLabel;
}
