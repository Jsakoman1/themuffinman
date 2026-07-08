package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessOwnerDashboardDTO;
import com.themuffinman.app.business.dto.BusinessOwnerScheduleSummaryDTO;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.BusinessProperties;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessOwnerDashboardReadService {

    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessOfferingRepository businessOfferingRepository;
    private final BusinessOwnerScheduleReadService businessOwnerScheduleReadService;
    private final BusinessProperties businessProperties;

    public BusinessOwnerDashboardDTO getMyDashboard(AppUser currentUser) {
        BusinessProfile profile = businessProfileRepository.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> ServiceErrors.badRequest("Create your business profile before viewing the dashboard"));
        BusinessOwnerScheduleSummaryDTO summary = businessOwnerScheduleReadService.getMyScheduleSummary(currentUser);

        return BusinessOwnerDashboardDTO.builder()
                .businessProfileId(profile.getId())
                .businessName(profile.getBusinessName())
                .slug(profile.getSlug())
                .bookingEnabled(profile.isBookingEnabled())
                .activeOfferingCount((int) businessOfferingRepository.findByOwnerId(currentUser.getId()).stream()
                        .filter(com.themuffinman.app.business.model.BusinessOffering::isActive)
                        .count())
                .pendingConfirmationCount(summary.getPendingConfirmationCount())
                .todayCount(summary.getTodayCount())
                .upcomingCount(summary.getUpcomingCount())
                .staleThresholdMinutes(businessProperties.getDashboard().getStaleThresholdMinutes())
                .scheduleSummary(summary)
                .build();
    }
}
