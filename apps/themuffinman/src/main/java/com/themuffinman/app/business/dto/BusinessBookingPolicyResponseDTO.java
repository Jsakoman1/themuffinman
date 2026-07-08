package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BusinessBookingPolicyResponseDTO {
    private Long id;
    private Long businessProfileId;
    private int leadTimeMinutes;
    private int maxAdvanceDays;
    private int customerCancellationWindowMinutes;
    private int ownerRescheduleWindowMinutes;
    private boolean requiresOwnerConfirmationDefault;
    private boolean allowCustomerCancellation;
    private boolean allowOwnerManualApproval;
    private boolean allowOwnerManualRejection;
    private boolean allowWaitlist;
    private Instant createdAt;
    private Instant updatedAt;
}
