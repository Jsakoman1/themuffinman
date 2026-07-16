package com.themuffinman.app.things.dto;

import com.themuffinman.app.things.model.ThingBorrowRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ThingBorrowRequestResponseDTO {
    private Long id;
    private Long listingId;
    private Long borrowerId;
    private String borrowerUsername;
    private String message;
    private ThingBorrowRequestStatus status;
    private Instant approvedAt;
    private Instant createdAt;
}
