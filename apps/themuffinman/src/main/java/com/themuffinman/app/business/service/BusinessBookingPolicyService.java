package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessBookingPolicyRequestDTO;
import com.themuffinman.app.business.dto.BusinessBookingPolicyResponseDTO;
import com.themuffinman.app.business.mapper.BusinessBookingPolicyMgr;
import com.themuffinman.app.business.model.BusinessBookingPolicy;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessBookingPolicyRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.BusinessProperties;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessBookingPolicyService {

    private final BusinessBookingPolicyRepository businessBookingPolicyRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessBookingPolicyMgr businessBookingPolicyMgr;
    private final BusinessProperties businessProperties;

    public BusinessBookingPolicyResponseDTO getMyPolicy(AppUser currentUser) {
        return businessBookingPolicyMgr.toDto(loadOrCreatePolicyEntity(currentUser));
    }

    @Transactional(readOnly = true)
    public BusinessBookingPolicy resolveEffectivePolicy(AppUser currentUser) {
        return businessBookingPolicyRepository.findByOwnerId(currentUser.getId())
                .orElseGet(() -> {
                    BusinessProfile profile = businessProfileRepository.findByOwnerId(currentUser.getId())
                            .orElseThrow(() -> ServiceErrors.badRequest("Create your business profile before managing booking policy"));
                    return createDefaultPolicy(profile);
                });
    }

    @Transactional
    public BusinessBookingPolicyResponseDTO saveMyPolicy(BusinessBookingPolicyRequestDTO dto, AppUser currentUser) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Business booking policy request is required");
        }

        BusinessBookingPolicy policy = loadOrCreatePolicyEntity(currentUser);
        policy.setLeadTimeMinutes(requireNonNegative(dto.getLeadTimeMinutes(), "Lead time is required"));
        policy.setMaxAdvanceDays(requireAtLeastOne(dto.getMaxAdvanceDays(), "Max advance days is required"));
        policy.setCustomerCancellationWindowMinutes(
                requireNonNegative(dto.getCustomerCancellationWindowMinutes(), "Customer cancellation window is required"));
        policy.setOwnerRescheduleWindowMinutes(
                requireNonNegative(dto.getOwnerRescheduleWindowMinutes(), "Owner reschedule window is required"));
        policy.setRequiresOwnerConfirmationDefault(Boolean.TRUE.equals(dto.getRequiresOwnerConfirmationDefault()));
        policy.setAllowCustomerCancellation(dto.getAllowCustomerCancellation() == null || dto.getAllowCustomerCancellation());
        policy.setAllowOwnerManualApproval(dto.getAllowOwnerManualApproval() == null || dto.getAllowOwnerManualApproval());
        policy.setAllowOwnerManualRejection(dto.getAllowOwnerManualRejection() == null || dto.getAllowOwnerManualRejection());
        policy.setAllowWaitlist(dto.getAllowWaitlist() != null && dto.getAllowWaitlist());

        return businessBookingPolicyMgr.toDto(businessBookingPolicyRepository.save(policy));
    }

    @Transactional
    protected BusinessBookingPolicy loadOrCreatePolicyEntity(AppUser currentUser) {
        return businessBookingPolicyRepository.findByOwnerId(currentUser.getId())
                .orElseGet(() -> {
                    BusinessProfile profile = businessProfileRepository.findByOwnerId(currentUser.getId())
                            .orElseThrow(() -> ServiceErrors.badRequest("Create your business profile before managing booking policy"));
                    return businessBookingPolicyRepository.save(createDefaultPolicy(profile));
                });
    }

    private BusinessBookingPolicy createDefaultPolicy(BusinessProfile profile) {
        BusinessBookingPolicy created = new BusinessBookingPolicy();
        created.setBusinessProfile(profile);
        created.setLeadTimeMinutes(businessProperties.getBooking().getDefaultLeadTimeMinutes());
        created.setMaxAdvanceDays(businessProperties.getBooking().getDefaultMaxAdvanceDays());
        created.setCustomerCancellationWindowMinutes(
                businessProperties.getBooking().getDefaultCustomerCancellationWindowMinutes());
        created.setOwnerRescheduleWindowMinutes(
                businessProperties.getBooking().getDefaultCustomerCancellationWindowMinutes());
        created.setRequiresOwnerConfirmationDefault(true);
        created.setAllowCustomerCancellation(true);
        created.setAllowOwnerManualApproval(true);
        created.setAllowOwnerManualRejection(true);
        created.setAllowWaitlist(false);
        return created;
    }

    private int requireNonNegative(Integer value, String message) {
        if (value == null) {
            throw ServiceErrors.badRequest(message);
        }
        if (value < 0) {
            throw ServiceErrors.badRequest(message);
        }
        return value;
    }

    private int requireAtLeastOne(Integer value, String message) {
        if (value == null || value < 1) {
            throw ServiceErrors.badRequest(message);
        }
        return value;
    }
}
