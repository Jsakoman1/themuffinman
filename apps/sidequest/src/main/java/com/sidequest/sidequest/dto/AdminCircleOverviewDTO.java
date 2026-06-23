package com.sidequest.sidequest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCircleOverviewDTO {
    private List<AdminCircleGroupResponseDTO> circles;
    private List<CircleRequestResponseDTO> acceptedConnections;
    private List<CircleRequestResponseDTO> pendingRequests;
    private List<CircleRequestResponseDTO> blockedRelations;
}
