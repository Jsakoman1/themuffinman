package com.themuffinman.app.things.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ThingListingResponseDTO {
    private Long id;
    private Long ownerId;
    private String ownerUsername;
    private String title;
    private String description;
    private String conditionNote;
    private boolean available;
    private Long myPendingRequestId;
    private Instant createdAt;
    private Instant updatedAt;
}
