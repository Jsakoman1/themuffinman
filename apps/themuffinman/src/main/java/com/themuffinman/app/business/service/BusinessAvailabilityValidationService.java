package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessAvailabilityExceptionRequestDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityRuleRequestDTO;
import com.themuffinman.app.business.model.BusinessAvailabilityExceptionType;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.common.errors.ServiceErrors;
import org.springframework.stereotype.Service;

@Service
public class BusinessAvailabilityValidationService {

    public void validateBusinessTimezoneConfigured(BusinessProfile profile) {
        if (profile.getTimezone() == null || profile.getTimezone().isBlank()) {
            throw ServiceErrors.badRequest("Set a business timezone before managing availability");
        }
    }

    public void validateRuleRequest(BusinessAvailabilityRuleRequestDTO dto) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Business availability rule request is required");
        }
        if (dto.getStartTimeLocal() == null || dto.getEndTimeLocal() == null
                || !dto.getEndTimeLocal().isAfter(dto.getStartTimeLocal())) {
            throw ServiceErrors.badRequest("Availability rule end time must be after start time");
        }
        if (dto.getSlotGranularityMinutes() == null || dto.getSlotGranularityMinutes() < 5) {
            throw ServiceErrors.badRequest("Availability rule slot granularity must be at least 5 minutes");
        }
        if (dto.getCapacityOverride() != null && dto.getCapacityOverride() < 1) {
            throw ServiceErrors.badRequest("Availability rule capacity override must be at least 1");
        }
        if (dto.getValidFrom() != null && dto.getValidUntil() != null && dto.getValidUntil().isBefore(dto.getValidFrom())) {
            throw ServiceErrors.badRequest("Availability rule valid-until cannot be before valid-from");
        }
    }

    public void validateExceptionRequest(BusinessAvailabilityExceptionRequestDTO dto) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Business availability exception request is required");
        }
        if (dto.getStartAt() == null || dto.getEndAt() == null || !dto.getEndAt().isAfter(dto.getStartAt())) {
            throw ServiceErrors.badRequest("Availability exception end time must be after start time");
        }
        if (dto.getExceptionType() == BusinessAvailabilityExceptionType.REPLACE_WINDOW) {
            if (dto.getReplacementStartTimeLocal() == null || dto.getReplacementEndTimeLocal() == null) {
                throw ServiceErrors.badRequest("Replacement exceptions require replacement start and end times");
            }
            if (!dto.getReplacementEndTimeLocal().isAfter(dto.getReplacementStartTimeLocal())) {
                throw ServiceErrors.badRequest("Replacement exception end time must be after start time");
            }
        }
        if (dto.getReplacementCapacity() != null && dto.getReplacementCapacity() < 1) {
            throw ServiceErrors.badRequest("Replacement capacity must be at least 1");
        }
    }
}
