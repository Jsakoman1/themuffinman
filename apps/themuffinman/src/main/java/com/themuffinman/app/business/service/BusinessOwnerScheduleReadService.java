package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessOwnerScheduleItemDTO;
import com.themuffinman.app.business.dto.BusinessOwnerScheduleSummaryDTO;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.time.TimeSupport;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessOwnerScheduleReadService {

    private static final EnumSet<BusinessBookingStatus> UPCOMING_STATUSES =
            EnumSet.of(BusinessBookingStatus.PENDING_CONFIRMATION, BusinessBookingStatus.CONFIRMED);

    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessBookingRepository businessBookingRepository;
    private final BusinessBookingPresentationService businessBookingPresentationService;

    /** Calendar data carries canonical booking targets for object-focused navigation. */
    public BusinessOwnerScheduleSummaryDTO getMyScheduleSummary(AppUser currentUser) {
        BusinessProfile profile = businessProfileRepository.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> ServiceErrors.badRequest("Create your business profile before viewing schedule"));
        ZoneId zoneId = TimeSupport.requireZoneId(profile.getTimezone(), "Create your business profile before viewing schedule");
        LocalDate today = TimeSupport.today(zoneId);
        Instant dayStart = TimeSupport.startOfDay(today, zoneId);
        Instant dayEnd = TimeSupport.endOfDay(today, zoneId);

        List<BusinessBooking> todayBookings = businessBookingRepository.findDetailedByOwnerIdAndStartsAtBetween(
                currentUser.getId(),
                dayStart,
                dayEnd
        );
        List<BusinessBooking> upcomingBookings = businessBookingRepository.findUpcomingDetailedByOwnerId(
                currentUser.getId(),
                UPCOMING_STATUSES,
                TimeSupport.now()
        );

        int pendingConfirmationCount = (int) upcomingBookings.stream()
                .filter(booking -> booking.getStatus() == BusinessBookingStatus.PENDING_CONFIRMATION)
                .count();

        return BusinessOwnerScheduleSummaryDTO.builder()
                .timezone(profile.getTimezone())
                .todayCount(todayBookings.size())
                .pendingConfirmationCount(pendingConfirmationCount)
                .upcomingCount(upcomingBookings.size())
                .nextItems(upcomingBookings.stream()
                        .limit(10)
                        .map(booking -> BusinessOwnerScheduleItemDTO.builder()
                                .bookingId(booking.getId())
                                .businessOfferingTitle(booking.getOfferingTitleSnapshot())
                                .customerUsername(booking.getCustomerUser().getUsername())
                                .startsAt(booking.getStartsAt())
                                .endsAt(booking.getEndsAt())
                                .timezone(booking.getTimezone())
                                .statusLabel(businessBookingPresentationService.formatStatusLabel(booking.getStatus()))
                                .build())
                        .toList())
                .build();
    }
}
