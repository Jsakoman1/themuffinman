package com.themuffinman.app.business.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessBookingPolicyRequestDTO {

    @Min(value = 0, message = "Lead time must be zero or greater")
    private Integer leadTimeMinutes;

    @Min(value = 1, message = "Max advance days must be at least 1")
    private Integer maxAdvanceDays;

    @Min(value = 0, message = "Customer cancellation window must be zero or greater")
    private Integer customerCancellationWindowMinutes;

    @Min(value = 0, message = "Owner reschedule window must be zero or greater")
    private Integer ownerRescheduleWindowMinutes;

    private Boolean requiresOwnerConfirmationDefault;
    private Boolean allowCustomerCancellation;
    private Boolean allowOwnerManualApproval;
    private Boolean allowOwnerManualRejection;
    private Boolean allowWaitlist;
}
