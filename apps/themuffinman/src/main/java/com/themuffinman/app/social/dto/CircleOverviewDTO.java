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
public class CircleOverviewDTO {
    private long connectionCount;
    private long unassignedConnectionCount;
    private long incomingRequestCount;
    private long outgoingRequestCount;
}
