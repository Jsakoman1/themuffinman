package com.themuffinman.app.social.dto;

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
    private List<AdminCircleRelationRowDTO> acceptedConnections;
    private List<AdminCircleRelationRowDTO> pendingRequests;
    private List<AdminCircleRelationRowDTO> blockedRelations;
}
