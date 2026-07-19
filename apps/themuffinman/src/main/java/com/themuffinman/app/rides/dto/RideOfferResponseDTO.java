package com.themuffinman.app.rides.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import com.themuffinman.app.rides.model.RideStatus;

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
    private RideStatus status;
    private int joinedSeats;
    private boolean viewerJoined;
    private boolean viewerIsDriver;
    private boolean canJoin;
    private boolean canLeave;
    private boolean canManage;
    private List<RideAllowedActionDTO> allowedActions;
    private Instant updatedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private List<String> visibleCircleNames;
    private Instant createdAt;
}
