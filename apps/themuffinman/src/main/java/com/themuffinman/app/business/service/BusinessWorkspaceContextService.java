package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessBookingResponseDTO;
import com.themuffinman.app.business.dto.BusinessProfileResponseDTO;
import com.themuffinman.app.business.dto.BusinessScheduleItemDTO;
import com.themuffinman.app.business.dto.BusinessWorkspaceContextDTO;
import com.themuffinman.app.business.mapper.BusinessBookingMgr;
import com.themuffinman.app.business.mapper.BusinessProfileMgr;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.time.TimeSupport;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessWorkspaceContextService {
    // The context is a read projection; role-specific allowed actions still come from booking presentation services.
    private final BusinessProfileRepository profileRepository;
    private final BusinessProfileMgr profileMgr;
    private final BusinessBookingRepository bookingRepository;
    private final BusinessBookingMgr bookingMgr;
    private final BusinessBookingPresentationService presentationService;

    public BusinessWorkspaceContextDTO getContext(AppUser viewer, Long profileId, Instant from, Instant to) {
        List<BusinessProfile> ownedProfiles = profileRepository.findAllByOwnerId(viewer.getId());
        BusinessProfile activeProfile = chooseProfile(ownedProfiles, profileId);
        Instant resolvedFrom = from == null ? TimeSupport.now().minus(Duration.ofDays(30)) : from;
        Instant resolvedTo = to == null ? TimeSupport.now().plus(Duration.ofDays(60)) : to;
        if (!resolvedTo.isAfter(resolvedFrom)) throw ServiceErrors.badRequest("Schedule range must end after it starts");

        List<BusinessBooking> ownedBookings = activeProfile == null
                ? bookingRepository.findDetailedByOwnerIdAndOverlap(viewer.getId(), resolvedFrom, resolvedTo)
                : bookingRepository.findDetailedByOwnerIdAndProfileIdAndOverlap(viewer.getId(), activeProfile.getId(), resolvedFrom, resolvedTo);
        List<BusinessBooking> customerBookings = activeProfile == null
                ? bookingRepository.findDetailedByCustomerIdAndOverlap(viewer.getId(), resolvedFrom, resolvedTo)
                : bookingRepository.findDetailedByCustomerIdAndProfileIdAndOverlap(viewer.getId(), activeProfile.getId(), resolvedFrom, resolvedTo);

        Map<Long, BusinessScheduleItemDTO> schedule = new LinkedHashMap<>();
        ownedBookings.forEach(booking -> schedule.put(booking.getId(), toItem(booking, viewer, "OWNER")));
        customerBookings.forEach(booking -> schedule.putIfAbsent(booking.getId(), toItem(booking, viewer, "CUSTOMER")));
        List<BusinessProfileResponseDTO> businesses = ownedProfiles.stream().map(profileMgr::toDto).toList();
        return BusinessWorkspaceContextDTO.builder()
                .businesses(businesses)
                .activeBusinessProfileId(activeProfile == null ? null : activeProfile.getId())
                .from(resolvedFrom).to(resolvedTo)
                .timezone(activeProfile == null ? "UTC" : activeProfile.getTimezone())
                .schedule(new ArrayList<>(schedule.values()))
                .build();
    }

    private BusinessProfile chooseProfile(List<BusinessProfile> profiles, Long profileId) {
        if (profiles.isEmpty()) return null;
        // A missing profile id is the explicit aggregate “All businesses” context.
        if (profileId == null) return null;
        return profiles.stream().filter(profile -> profile.getId().equals(profileId)).findFirst()
                .orElseThrow(() -> ServiceErrors.notFound("Business profile not found"));
    }

    private BusinessScheduleItemDTO toItem(BusinessBooking booking, AppUser viewer, String role) {
        BusinessBookingResponseDTO dto = "OWNER".equals(role)
                ? presentationService.enrichForOwner(bookingMgr.toDto(booking), booking, viewer)
                : presentationService.enrichForCustomer(bookingMgr.toDto(booking), booking, viewer);
        return BusinessScheduleItemDTO.builder()
                .bookingId(dto.getId()).role(role)
                .businessProfileId(dto.getBusinessProfileId()).businessName(dto.getBusinessName()).businessSlug(dto.getBusinessSlug())
                .businessOfferingTitle(dto.getBusinessOfferingTitle()).startsAt(dto.getStartsAt()).endsAt(dto.getEndsAt())
                .timezone(dto.getTimezone()).status(dto.getStatus()).statusLabel(dto.getStatusLabel()).allowedActions(dto.getAllowedActions())
                .build();
    }
}
