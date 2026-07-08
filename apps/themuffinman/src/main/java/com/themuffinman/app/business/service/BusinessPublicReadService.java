package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessOfferingListResponseDTO;
import com.themuffinman.app.business.dto.BusinessPublicPageDTO;
import com.themuffinman.app.business.mapper.BusinessGalleryImageMgr;
import com.themuffinman.app.business.mapper.BusinessOfferingMgr;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessGalleryImageRepository;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessPublicReadService {

    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessOfferingRepository businessOfferingRepository;
    private final BusinessGalleryImageRepository businessGalleryImageRepository;
    private final BusinessOfferingMgr businessOfferingMgr;
    private final BusinessGalleryImageMgr businessGalleryImageMgr;

    public BusinessPublicPageDTO getPublicBusinessPage(String slug) {
        BusinessProfile profile = businessProfileRepository.findBySlug(slug)
                .filter(BusinessProfile::isActive)
                .orElseThrow(() -> ServiceErrors.notFound("Business profile not found"));

        return BusinessPublicPageDTO.builder()
                .businessProfileId(profile.getId())
                .businessName(profile.getBusinessName())
                .slug(profile.getSlug())
                .headline(profile.getHeadline())
                .description(RichTextInputValidator.sanitize(profile.getDescription()))
                .publicAddressLabel(profile.getPublicAddressLabel())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .contactEmail(profile.getContactEmail())
                .contactPhone(profile.getContactPhone())
                .contactWhatsapp(profile.getContactWhatsapp())
                .websiteUrl(profile.getWebsiteUrl())
                .heroImageUrl(profile.getHeroImageUrl())
                .timezone(profile.getTimezone())
                .bookingEnabled(profile.isBookingEnabled())
                .offerings(businessOfferingRepository.findActiveByBusinessProfileId(profile.getId()).stream()
                        .map(businessOfferingMgr::toDto)
                        .toList())
                .galleryImages(businessGalleryImageRepository.findActiveByBusinessProfileId(profile.getId()).stream()
                        .map(businessGalleryImageMgr::toDto)
                        .toList())
                .build();
    }

    public BusinessOfferingListResponseDTO getPublicOfferings(String slug) {
        BusinessProfile profile = businessProfileRepository.findBySlug(slug)
                .filter(BusinessProfile::isActive)
                .orElseThrow(() -> ServiceErrors.notFound("Business profile not found"));

        return BusinessOfferingListResponseDTO.builder()
                .items(businessOfferingRepository.findActiveByBusinessProfileId(profile.getId()).stream()
                        .map(businessOfferingMgr::toDto)
                        .toList())
                .build();
    }
}
