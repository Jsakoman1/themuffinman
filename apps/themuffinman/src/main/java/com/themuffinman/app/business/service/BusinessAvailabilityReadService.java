package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessAvailabilityWindowListResponseDTO;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessAvailabilityExceptionRepository;
import com.themuffinman.app.business.repository.BusinessAvailabilityRuleRepository;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessAvailabilityReadService {

    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessOfferingRepository businessOfferingRepository;
    private final BusinessAvailabilityRuleRepository businessAvailabilityRuleRepository;
    private final BusinessAvailabilityExceptionRepository businessAvailabilityExceptionRepository;
    private final BusinessAvailabilityComputationService businessAvailabilityComputationService;

    public BusinessAvailabilityWindowListResponseDTO getPublicAvailability(String slug, Long offeringId, Instant from, Instant to) {
        if (from == null || to == null || !to.isAfter(from)) {
            throw ServiceErrors.badRequest("Availability range is invalid");
        }
        BusinessProfile profile = businessProfileRepository.findBySlug(slug)
                .filter(BusinessProfile::isActive)
                .orElseThrow(() -> ServiceErrors.notFound("Business profile not found"));
        if (!profile.isBookingEnabled()) {
            throw ServiceErrors.notFound("Business availability not found");
        }

        BusinessOffering offering = businessOfferingRepository.findActiveByBusinessProfileId(profile.getId()).stream()
                .filter(candidate -> offeringId == null || candidate.getId().equals(offeringId))
                .findFirst()
                .orElseThrow(() -> ServiceErrors.notFound("Business offering not found"));
        if (offering.getFulfillmentMode() == com.themuffinman.app.business.model.BusinessOfferingFulfillmentMode.ALL_DAY_STAY
                && offering.getDurationMode() != com.themuffinman.app.business.model.BusinessOfferingDurationMode.ALL_DAY) {
            throw ServiceErrors.conflict("Stay offerings must use all-day duration configuration");
        }

        return BusinessAvailabilityWindowListResponseDTO.builder()
                .items(businessAvailabilityComputationService.deriveWindows(
                        profile,
                        offering,
                        businessAvailabilityRuleRepository.findActiveByBusinessProfileId(profile.getId()),
                        businessAvailabilityExceptionRepository.findByBusinessProfileId(profile.getId()),
                        from,
                        to
                ))
                .build();
    }
}
