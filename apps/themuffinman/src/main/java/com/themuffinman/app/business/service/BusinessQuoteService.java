package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessQuoteRequestDTO;
import com.themuffinman.app.business.dto.BusinessQuoteResponseDTO;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessQuoteService {

    private final BusinessOfferingRepository businessOfferingRepository;
    private final BusinessPricingCalculationService businessPricingCalculationService;

    public BusinessQuoteResponseDTO quote(String slug, BusinessQuoteRequestDTO request) {
        if (request == null) {
            throw ServiceErrors.badRequest("Quote request is required");
        }
        BusinessOffering offering = businessOfferingRepository.findById(request.getBusinessOfferingId())
                .filter(item -> item.isActive()
                        && item.getBusinessProfile().isActive()
                        && item.getBusinessProfile().isBookingEnabled()
                        && item.getBusinessProfile().getSlug().equals(slug))
                .orElseThrow(() -> ServiceErrors.notFound("Business offering not found"));
        if (request.getSchemaVersion() != null && request.getSchemaVersion() != offering.getSchemaVersion()) {
            throw ServiceErrors.conflict("Service configuration changed; refresh the offering before quoting");
        }
        if (request.getQuantity() != null && offering.getMinimumQuantity() != null
                && request.getQuantity().compareTo(java.math.BigDecimal.valueOf(offering.getMinimumQuantity())) < 0) {
            throw ServiceErrors.badRequest("Requested quantity is below the offering minimum");
        }
        if (request.getQuantity() != null && offering.getMaximumQuantity() != null
                && request.getQuantity().compareTo(java.math.BigDecimal.valueOf(offering.getMaximumQuantity())) > 0) {
            throw ServiceErrors.badRequest("Requested quantity exceeds the offering maximum");
        }
        Integer duration = request.getDurationMinutes() == null ? offering.getDefaultDurationMinutes() : request.getDurationMinutes();
        if (duration != null && duration < 1) throw ServiceErrors.badRequest("Duration must be at least one minute");
        if (offering.getDurationMode() == com.themuffinman.app.business.model.BusinessOfferingDurationMode.FIXED
                && offering.getDefaultDurationMinutes() != null && !offering.getDefaultDurationMinutes().equals(duration)) {
            throw ServiceErrors.conflict("Quote duration must match the offering duration");
        }
        if (offering.getDurationIncrementMinutes() != null && duration != null && duration % offering.getDurationIncrementMinutes() != 0) {
            throw ServiceErrors.conflict("Quote duration must match the offering duration increment");
        }
        if (offering.getMinDurationMinutes() != null && duration != null && duration < offering.getMinDurationMinutes()) {
            throw ServiceErrors.conflict("Quote duration is shorter than the offering minimum");
        }
        if (offering.getMaxDurationMinutes() != null && duration != null && duration > offering.getMaxDurationMinutes()) {
            throw ServiceErrors.conflict("Quote duration is longer than the offering maximum");
        }
        return businessPricingCalculationService.calculate(offering, request);
    }
}
