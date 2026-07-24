package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessBookingPreviewRequestDTO;
import com.themuffinman.app.business.dto.BusinessBookingPreviewResponseDTO;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessBookingPreviewService {

    private final BusinessOfferingRepository businessOfferingRepository;

    public BusinessBookingPreviewResponseDTO preview(String slug, BusinessBookingPreviewRequestDTO request) {
        if (request == null || request.getStartsAt() == null) {
            throw ServiceErrors.badRequest("Booking preview start time is required");
        }
        BusinessOffering offering = businessOfferingRepository.findById(request.getBusinessOfferingId())
                .filter(item -> item.isActive()
                        && item.getBusinessProfile().isActive()
                        && item.getBusinessProfile().getSlug().equals(slug))
                .orElseThrow(() -> ServiceErrors.notFound("Business offering not found"));

        Integer durationMinutes = offering.getDefaultDurationMinutes();
        if (durationMinutes == null || durationMinutes < 1) {
            throw ServiceErrors.badRequest("This offering does not have a previewable duration");
        }
        if (offering.getDurationIncrementMinutes() != null
                && durationMinutes % offering.getDurationIncrementMinutes() != 0) {
            throw ServiceErrors.conflict("Offering duration is not aligned to its configured increment");
        }

        return BusinessBookingPreviewResponseDTO.builder()
                .businessOfferingId(offering.getId())
                .offeringTitle(offering.getTitle())
                .startsAt(request.getStartsAt())
                .endsAt(request.getStartsAt().plus(Duration.ofMinutes(durationMinutes)))
                .timezone(offering.getBusinessProfile().getTimezone())
                .durationMinutes(durationMinutes)
                .actions(java.util.List.of())
                .build();
    }
}
