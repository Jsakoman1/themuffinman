package com.themuffinman.app.search.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class SavedSearchIntentRequestDTO {
    private String query;
    private String entityFamily;
    private boolean paused;
    private Boolean notifyEnabled;
    private Instant expiresAt;
}
