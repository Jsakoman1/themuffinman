package com.themuffinman.app.things.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record ThingWishlistItemResponseDTO(
        Long id,
        Long listingId,
        String title,
        String ownerUsername,
        List<Long> sharedCircleIds,
        Instant savedAt
) {
}
