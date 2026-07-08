package com.themuffinman.app.business.mapper;

import com.themuffinman.app.business.dto.BusinessBookingPolicyResponseDTO;
import com.themuffinman.app.business.model.BusinessBookingPolicy;
import org.springframework.stereotype.Component;

@Component
public class BusinessBookingPolicyMgr {

    public BusinessBookingPolicyResponseDTO toDto(BusinessBookingPolicy policy) {
        if (policy == null) {
            return null;
        }

        return BusinessBookingPolicyResponseDTO.builder()
                .id(policy.getId())
                .businessProfileId(policy.getBusinessProfile().getId())
                .leadTimeMinutes(policy.getLeadTimeMinutes())
                .maxAdvanceDays(policy.getMaxAdvanceDays())
                .customerCancellationWindowMinutes(policy.getCustomerCancellationWindowMinutes())
                .ownerRescheduleWindowMinutes(policy.getOwnerRescheduleWindowMinutes())
                .requiresOwnerConfirmationDefault(policy.isRequiresOwnerConfirmationDefault())
                .allowCustomerCancellation(policy.isAllowCustomerCancellation())
                .allowOwnerManualApproval(policy.isAllowOwnerManualApproval())
                .allowOwnerManualRejection(policy.isAllowOwnerManualRejection())
                .allowWaitlist(policy.isAllowWaitlist())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
