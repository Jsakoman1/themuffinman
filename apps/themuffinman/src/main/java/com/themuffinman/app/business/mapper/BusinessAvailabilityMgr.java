package com.themuffinman.app.business.mapper;

import com.themuffinman.app.business.dto.BusinessAvailabilityExceptionResponseDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityRuleResponseDTO;
import com.themuffinman.app.business.model.BusinessAvailabilityException;
import com.themuffinman.app.business.model.BusinessAvailabilityRule;
import org.springframework.stereotype.Component;

@Component
public class BusinessAvailabilityMgr {

    public BusinessAvailabilityRuleResponseDTO toDto(BusinessAvailabilityRule rule) {
        if (rule == null) {
            return null;
        }

        return BusinessAvailabilityRuleResponseDTO.builder()
                .id(rule.getId())
                .businessProfileId(rule.getBusinessProfile().getId())
                .businessOfferingId(rule.getBusinessOffering() == null ? null : rule.getBusinessOffering().getId())
                .businessOfferingTitle(rule.getBusinessOffering() == null ? null : rule.getBusinessOffering().getTitle())
                .dayOfWeek(rule.getDayOfWeek())
                .startTimeLocal(rule.getStartTimeLocal())
                .endTimeLocal(rule.getEndTimeLocal())
                .slotGranularityMinutes(rule.getSlotGranularityMinutes())
                .capacityOverride(rule.getCapacityOverride())
                .validFrom(rule.getValidFrom())
                .validUntil(rule.getValidUntil())
                .active(rule.isActive())
                .timezone(rule.getBusinessProfile().getTimezone())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }

    public BusinessAvailabilityExceptionResponseDTO toDto(BusinessAvailabilityException exception) {
        if (exception == null) {
            return null;
        }

        return BusinessAvailabilityExceptionResponseDTO.builder()
                .id(exception.getId())
                .businessProfileId(exception.getBusinessProfile().getId())
                .businessOfferingId(exception.getBusinessOffering() == null ? null : exception.getBusinessOffering().getId())
                .businessOfferingTitle(exception.getBusinessOffering() == null ? null : exception.getBusinessOffering().getTitle())
                .exceptionType(exception.getExceptionType())
                .startAt(exception.getStartAt())
                .endAt(exception.getEndAt())
                .replacementCapacity(exception.getReplacementCapacity())
                .replacementStartTimeLocal(exception.getReplacementStartTimeLocal())
                .replacementEndTimeLocal(exception.getReplacementEndTimeLocal())
                .reason(exception.getReason())
                .timezone(exception.getBusinessProfile().getTimezone())
                .createdAt(exception.getCreatedAt())
                .updatedAt(exception.getUpdatedAt())
                .build();
    }
}
