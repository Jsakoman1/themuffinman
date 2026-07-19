package com.themuffinman.app.activity.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class WorkspaceNavigationResponseDTO {
    /** Versioned read-model identifier for client capability negotiation. */
    private String contractVersion;
    private Instant generatedAt;
    private int refreshAfterSeconds;
    private long unreadCount;
    private List<WorkspaceNavigationModuleDTO> modules;
}
