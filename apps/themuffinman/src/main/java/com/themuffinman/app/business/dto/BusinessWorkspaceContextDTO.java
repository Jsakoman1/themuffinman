package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class BusinessWorkspaceContextDTO {
    private List<BusinessProfileResponseDTO> businesses;
    private Long activeBusinessProfileId;
    private Instant from;
    private Instant to;
    private String timezone;
    private List<BusinessScheduleItemDTO> schedule;
}
