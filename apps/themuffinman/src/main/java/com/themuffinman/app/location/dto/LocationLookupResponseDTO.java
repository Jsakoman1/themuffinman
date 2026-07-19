package com.themuffinman.app.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.themuffinman.app.location.model.LocationLookupResolutionStatus;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationLookupResponseDTO {
    private boolean configured;
    private String provider;
    private LocationLookupResolutionStatus resolutionStatus;
    private List<LocationLookupCandidateDTO> items;
}
