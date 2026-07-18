package com.themuffinman.app.things.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ThingPreviewResponseDTO(
        long id,
        String title,
        String summary,
        String ownerUsername,
        String conditionNote,
        boolean available,
        Long myPendingRequestId,
        Instant updatedAt,
        boolean canOpenDetail
) {
    public static ThingPreviewResponseDTO from(ThingListingResponseDTO source) {
        return ThingPreviewResponseDTO.builder()
                .id(source.getId())
                .title(source.getTitle())
                .summary(source.getDescription())
                .ownerUsername(source.getOwnerUsername())
                .conditionNote(source.getConditionNote())
                .available(source.isAvailable())
                .myPendingRequestId(source.getMyPendingRequestId())
                .updatedAt(source.getUpdatedAt())
                .canOpenDetail(true)
                .build();
    }
}
