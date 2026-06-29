package com.themuffinman.app.rides.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class RideOfferResponseDTO {
    private Long id;
    private Long driverId;
    private String driverUsername;
    private String origin;
    private String destination;
    private Instant departureAt;
    private Integer seats;
    private String note;
    private boolean active;
    private List<String> visibleCircleNames;
    private Instant createdAt;
}
