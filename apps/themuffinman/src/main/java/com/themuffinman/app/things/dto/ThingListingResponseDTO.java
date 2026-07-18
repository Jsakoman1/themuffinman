package com.themuffinman.app.things.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

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
    private boolean archived;
    private Long myPendingRequestId;
    private Instant createdAt;
    private Instant updatedAt;
    private String availabilityLabel;
    private List<ThingAllowedActionDTO> allowedActions;
}
