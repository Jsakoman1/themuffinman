package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessOwnerCalendarDayDTO;
import com.themuffinman.app.business.dto.BusinessOwnerCalendarItemDTO;
import com.themuffinman.app.business.dto.BusinessOwnerCalendarProjectionDTO;
import com.themuffinman.app.business.dto.BusinessBookingResponseDTO;
import com.themuffinman.app.business.mapper.BusinessBookingMgr;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.BusinessProperties;
import com.themuffinman.app.common.time.TimeSupport;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessOwnerCalendarReadService {

    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessBookingRepository businessBookingRepository;
    private final BusinessBookingMgr businessBookingMgr;
    private final BusinessBookingPresentationService businessBookingPresentationService;
    private final BusinessProperties businessProperties;

    public BusinessOwnerCalendarProjectionDTO getMyCalendar(AppUser currentUser, Instant from, Instant to) {
        BusinessProfile profile = businessProfileRepository.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> ServiceErrors.badRequest("Create your business profile before viewing calendar"));
        ZoneId zoneId = TimeSupport.requireZoneId(profile.getTimezone(), "Create your business profile before viewing calendar");
        CalendarWindow window = resolveWindow(zoneId, from, to);
        List<BusinessBooking> bookings = businessBookingRepository.findDetailedByOwnerIdAndOverlap(
                currentUser.getId(),
                window.from(),
                window.to()
        );
        Map<LocalDate, List<BusinessBooking>> bookingsByDay = bookings.stream()
                .filter(booking -> booking.getEndsAt().isAfter(window.from()) && booking.getStartsAt().isBefore(window.to()))
                .collect(Collectors.groupingBy(booking -> booking.getStartsAt().atZone(zoneId).toLocalDate()));

        List<BusinessOwnerCalendarDayDTO> days = IntStream.rangeClosed(0, (int) ChronoUnit.DAYS.between(window.startDate(), window.endDateInclusive()))
                .mapToObj(offset -> window.startDate().plusDays(offset))
                .map(day -> BusinessOwnerCalendarDayDTO.builder()
                        .date(day)
                        .bookingCount(bookingsByDay.getOrDefault(day, List.of()).size())
                        .items(bookingsByDay.getOrDefault(day, List.of()).stream()
                                .sorted((left, right) -> left.getStartsAt().compareTo(right.getStartsAt()))
                                .map(booking -> toCalendarItem(booking, currentUser))
                                .toList())
                        .build())
                .toList();

        return BusinessOwnerCalendarProjectionDTO.builder()
                .timezone(profile.getTimezone())
                .from(window.from())
                .to(window.to())
                .totalBookings(bookings.size())
                .days(days)
                .build();
    }

    private BusinessOwnerCalendarItemDTO toCalendarItem(BusinessBooking booking, AppUser currentUser) {
        BusinessBookingResponseDTO presentation = businessBookingPresentationService.enrichForOwner(
                businessBookingMgr.toDto(booking),
                booking,
                currentUser
        );
        return BusinessOwnerCalendarItemDTO.builder()
                .bookingId(presentation.getId())
                .businessOfferingTitle(presentation.getBusinessOfferingTitle())
                .customerUsername(presentation.getCustomerUsername())
                .startsAt(presentation.getStartsAt())
                .endsAt(presentation.getEndsAt())
                .timezone(presentation.getTimezone())
                .status(presentation.getStatus())
                .statusLabel(presentation.getStatusLabel())
                .blockingReason(presentation.getBlockingReason())
                .allowedActions(presentation.getAllowedActions())
                .build();
    }

    private CalendarWindow resolveWindow(ZoneId zoneId, Instant from, Instant to) {
        Instant defaultFrom = TimeSupport.startOfDay(TimeSupport.today(zoneId), zoneId);
        Instant resolvedFrom = from != null ? from : defaultFrom;
        Instant resolvedTo = to != null ? to : resolvedFrom.plus(
                businessProperties.getCalendar().getDefaultProjectionDays(),
                ChronoUnit.DAYS
        );
        if (!resolvedTo.isAfter(resolvedFrom)) {
            throw ServiceErrors.badRequest("Calendar range must end after it starts");
        }

        long rangeDays = Duration.between(resolvedFrom, resolvedTo).toDays();
        if (rangeDays > businessProperties.getCalendar().getMaxProjectionDays()) {
            throw ServiceErrors.badRequest("Calendar range exceeds the configured maximum");
        }

        LocalDate startDate = resolvedFrom.atZone(zoneId).toLocalDate();
        LocalDate endDateInclusive = resolvedTo.minusNanos(1).atZone(zoneId).toLocalDate();
        return new CalendarWindow(resolvedFrom, resolvedTo, startDate, endDateInclusive);
    }

    private record CalendarWindow(Instant from, Instant to, LocalDate startDate, LocalDate endDateInclusive) {
    }
}
