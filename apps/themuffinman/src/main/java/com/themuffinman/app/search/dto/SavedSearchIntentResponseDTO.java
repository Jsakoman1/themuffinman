package com.themuffinman.app.search.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter @Builder
public class SavedSearchIntentResponseDTO {
    private Long id;
    private String query;
    private String entityFamily;
    private boolean paused;
    private boolean notifyEnabled;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
}
