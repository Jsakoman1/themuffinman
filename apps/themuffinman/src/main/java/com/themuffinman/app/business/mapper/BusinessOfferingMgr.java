package com.themuffinman.app.business.mapper;

import com.themuffinman.app.business.dto.BusinessOfferingResponseDTO;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import org.springframework.stereotype.Component;

@Component
public class BusinessOfferingMgr {

    public BusinessOfferingResponseDTO toDto(BusinessOffering offering) {
        if (offering == null) {
            return null;
        }

        return BusinessOfferingResponseDTO.builder()
                .id(offering.getId())
                .businessProfileId(offering.getBusinessProfile().getId())
                .businessSlug(offering.getBusinessProfile().getSlug())
                .title(offering.getTitle())
                .slug(offering.getSlug())
                .summary(offering.getSummary())
                .description(RichTextInputValidator.sanitize(offering.getDescription()))
                .pricingType(offering.getPricingType())
                .basePriceAmount(offering.getBasePriceAmount())
                .basePriceCurrency(offering.getBasePriceCurrency())
                .durationMode(offering.getDurationMode())
                .defaultDurationMinutes(offering.getDefaultDurationMinutes())
                .minDurationMinutes(offering.getMinDurationMinutes())
                .maxDurationMinutes(offering.getMaxDurationMinutes())
                .capacityMode(offering.getCapacityMode())
                .slotCapacity(offering.getSlotCapacity())
                .bookingMode(offering.getBookingMode())
                .requiresOwnerConfirmation(offering.isRequiresOwnerConfirmation())
                .bufferBeforeMinutes(offering.getBufferBeforeMinutes())
                .bufferAfterMinutes(offering.getBufferAfterMinutes())
                .active(offering.isActive())
                .sortOrder(offering.getSortOrder())
                .createdAt(offering.getCreatedAt())
                .updatedAt(offering.getUpdatedAt())
                .build();
    }
}
