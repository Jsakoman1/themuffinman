package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessOfferingListResponseDTO;
import com.themuffinman.app.business.dto.BusinessOfferingRequestDTO;
import com.themuffinman.app.business.dto.BusinessOfferingResponseDTO;
import com.themuffinman.app.business.mapper.BusinessOfferingMgr;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessOfferingBookingMode;
import com.themuffinman.app.business.model.BusinessOfferingCapacityMode;
import com.themuffinman.app.business.model.BusinessOfferingDurationMode;
import com.themuffinman.app.business.model.BusinessOfferingPricingType;
import com.themuffinman.app.business.model.BusinessOfferingFulfillmentMode;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.normalization.SlugNormalizer;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessOfferingService {

    private final BusinessOfferingRepository businessOfferingRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessOfferingMgr businessOfferingMgr;

    public BusinessOfferingListResponseDTO getMyOfferings(AppUser currentUser) {
        return getMyOfferings(currentUser, null);
    }

    public BusinessOfferingListResponseDTO getMyOfferings(AppUser currentUser, Long businessProfileId) {
        BusinessProfile profile = businessProfileId == null ? null : requireOwnerProfile(currentUser, businessProfileId);
        return BusinessOfferingListResponseDTO.builder()
                .items((profile == null ? businessOfferingRepository.findByOwnerId(currentUser.getId()) : businessOfferingRepository.findByBusinessProfileId(profile.getId(), currentUser.getId())).stream()
                        .map(businessOfferingMgr::toDto)
                        .toList())
                .build();
    }

    @Transactional
    public BusinessOfferingResponseDTO createMyOffering(BusinessOfferingRequestDTO dto, AppUser currentUser) {
        return createMyOffering(dto, currentUser, null);
    }

    @Transactional
    public BusinessOfferingResponseDTO createMyOffering(BusinessOfferingRequestDTO dto, AppUser currentUser, Long businessProfileId) {
        BusinessProfile profile = businessProfileId == null ? requireOwnerProfile(currentUser) : requireOwnerProfile(currentUser, businessProfileId);
        BusinessOffering offering = new BusinessOffering();
        offering.setBusinessProfile(profile);
        applyRequest(offering, dto, profile, true);
        return businessOfferingMgr.toDto(businessOfferingRepository.save(offering));
    }

    @Transactional
    public BusinessOfferingResponseDTO updateMyOffering(Long offeringId, BusinessOfferingRequestDTO dto, AppUser currentUser) {
        BusinessOffering offering = businessOfferingRepository.findOwnedById(offeringId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Business offering not found"));
        applyRequest(offering, dto, offering.getBusinessProfile(), false);
        return businessOfferingMgr.toDto(businessOfferingRepository.save(offering));
    }

    @Transactional
    public BusinessOfferingResponseDTO updateMyOfferingTitleForVision(Long offeringId, String title, AppUser currentUser) {
        BusinessOffering offering = businessOfferingRepository.findOwnedById(offeringId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Business offering not found"));
        offering.setTitle(TextValueNormalizer.requireTrimmed(title, "Offering title is required"));
        return businessOfferingMgr.toDto(businessOfferingRepository.save(offering));
    }

    @Transactional
    public void deleteMyOffering(Long offeringId, AppUser currentUser) {
        BusinessOffering offering = businessOfferingRepository.findOwnedById(offeringId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Business offering not found"));
        offering.setActive(false);
        businessOfferingRepository.save(offering);
    }

    private BusinessProfile requireOwnerProfile(AppUser currentUser) {
        return businessProfileRepository.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> ServiceErrors.badRequest("Create your business profile before managing offerings"));
    }

    private BusinessProfile requireOwnerProfile(AppUser currentUser, Long businessProfileId) {
        return businessProfileRepository.findById(businessProfileId)
                .filter(profile -> profile.getOwner().getId().equals(currentUser.getId()))
                .orElseThrow(() -> ServiceErrors.notFound("Business profile not found"));
    }

    private void applyRequest(BusinessOffering offering, BusinessOfferingRequestDTO dto, BusinessProfile profile, boolean creating) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Business offering request is required");
        }

        String title = TextValueNormalizer.requireTrimmed(dto.getTitle(), "Offering title is required");
        String slug = dto.getSlug() == null || dto.getSlug().isBlank()
                ? SlugNormalizer.slugify(title, "Offering slug could not be derived from the title")
                : SlugNormalizer.normalizeSlug(dto.getSlug(), "Offering slug is required", "Offering slug must use lowercase letters, numbers, and hyphens");

        validateSlugAvailable(profile.getId(), slug, offering.getId(), creating);

        BusinessOfferingPricingType pricingType = dto.getPricingType() == null
                ? BusinessOfferingPricingType.FIXED
                : dto.getPricingType();
        BusinessOfferingDurationMode durationMode = dto.getDurationMode() == null
                ? BusinessOfferingDurationMode.FIXED
                : dto.getDurationMode();
        BusinessOfferingCapacityMode capacityMode = dto.getCapacityMode() == null
                ? BusinessOfferingCapacityMode.SINGLE
                : dto.getCapacityMode();
        BusinessOfferingBookingMode bookingMode = dto.getBookingMode() == null
                ? BusinessOfferingBookingMode.REQUEST
                : dto.getBookingMode();

        Integer slotCapacity = dto.getSlotCapacity() == null ? 1 : dto.getSlotCapacity();
        if (slotCapacity < 1) {
            throw ServiceErrors.badRequest("Slot capacity must be at least 1");
        }

        Integer defaultDurationMinutes = dto.getDefaultDurationMinutes();
        if (durationMode == BusinessOfferingDurationMode.FIXED && defaultDurationMinutes == null) {
            throw ServiceErrors.badRequest("Fixed-duration offerings require a default duration");
        }
        if (durationMode != BusinessOfferingDurationMode.ALL_DAY && defaultDurationMinutes != null && defaultDurationMinutes < 1) {
            throw ServiceErrors.badRequest("Default duration must be at least 1 minute");
        }
        if (dto.getMinDurationMinutes() != null && dto.getMaxDurationMinutes() != null
                && dto.getMinDurationMinutes() > dto.getMaxDurationMinutes()) {
            throw ServiceErrors.badRequest("Minimum duration cannot exceed maximum duration");
        }
        if (dto.getDurationIncrementMinutes() != null && defaultDurationMinutes != null
                && defaultDurationMinutes % dto.getDurationIncrementMinutes() != 0) {
            throw ServiceErrors.badRequest("Default duration must match the duration increment");
        }
        if (dto.getMinimumQuantity() != null && dto.getMaximumQuantity() != null
                && dto.getMinimumQuantity() > dto.getMaximumQuantity()) {
            throw ServiceErrors.badRequest("Minimum quantity cannot exceed maximum quantity");
        }

        BigDecimal basePriceAmount = dto.getBasePriceAmount();
        String basePriceCurrency = normalizeCurrency(dto.getBasePriceCurrency());
        if (pricingType == BusinessOfferingPricingType.FIXED || pricingType == BusinessOfferingPricingType.FROM) {
            if (basePriceAmount == null) {
                throw ServiceErrors.badRequest("This pricing type requires a base price amount");
            }
            if (basePriceAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw ServiceErrors.badRequest("Base price amount must be zero or greater");
            }
            if (basePriceCurrency == null) {
                throw ServiceErrors.badRequest("This pricing type requires a base price currency");
            }
        }

        offering.setTitle(title);
        offering.setSlug(slug);
        offering.setSummary(TextValueNormalizer.trimToNull(dto.getSummary()));
        offering.setDescription(RichTextInputValidator.sanitize(dto.getDescription()));
        offering.setPricingType(pricingType);
        offering.setBasePriceAmount(basePriceAmount);
        offering.setBasePriceCurrency(basePriceCurrency);
        offering.setDurationMode(durationMode);
        offering.setDefaultDurationMinutes(defaultDurationMinutes);
        offering.setMinDurationMinutes(dto.getMinDurationMinutes());
        offering.setMaxDurationMinutes(dto.getMaxDurationMinutes());
        offering.setCapacityMode(capacityMode);
        offering.setSlotCapacity(slotCapacity);
        offering.setBookingMode(bookingMode);
        offering.setFulfillmentMode(dto.getFulfillmentMode() == null
                ? BusinessOfferingFulfillmentMode.EXACT_APPOINTMENT : dto.getFulfillmentMode());
        offering.setDurationIncrementMinutes(dto.getDurationIncrementMinutes());
        offering.setMinimumQuantity(dto.getMinimumQuantity());
        offering.setMaximumQuantity(dto.getMaximumQuantity());
        offering.setRequiresOwnerConfirmation(dto.getRequiresOwnerConfirmation() == null
                ? bookingMode != BusinessOfferingBookingMode.INSTANT
                : dto.getRequiresOwnerConfirmation());
        offering.setBufferBeforeMinutes(dto.getBufferBeforeMinutes() == null ? 0 : dto.getBufferBeforeMinutes());
        offering.setBufferAfterMinutes(dto.getBufferAfterMinutes() == null ? 0 : dto.getBufferAfterMinutes());
        offering.setActive(dto.getActive() == null || dto.getActive());
        offering.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        if (!creating) {
            offering.setSchemaVersion(Math.max(1, offering.getSchemaVersion()) + 1);
        }
    }

    private void validateSlugAvailable(Long businessProfileId, String slug, Long offeringId, boolean creating) {
        boolean taken = creating
                ? businessOfferingRepository.existsByBusinessProfileIdAndSlug(businessProfileId, slug)
                : businessOfferingRepository.existsByBusinessProfileIdAndSlugAndIdNot(businessProfileId, slug, offeringId);
        if (taken) {
            throw ServiceErrors.conflict("Business offering slug is already in use");
        }
    }

    private String normalizeCurrency(String value) {
        return TextValueNormalizer.upperTrimToNull(value);
    }
}
