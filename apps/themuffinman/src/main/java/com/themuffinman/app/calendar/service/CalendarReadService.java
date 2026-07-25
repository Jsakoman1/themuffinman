package com.themuffinman.app.calendar.service;

import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.calendar.dto.CalendarEventDTO;
import com.themuffinman.app.calendar.dto.CalendarProjectionDTO;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.rides.model.RideOffer;
import com.themuffinman.app.rides.repository.RideOfferRepository;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import com.themuffinman.app.workmarket.service.WorkmarketQuestVisibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarReadService {
    private static final Set<String> SUPPORTED_SOURCES = Set.of("business", "quest", "ride");
    private static final Instant OPEN_ENDED_EVENT_END = Instant.parse("9999-12-31T00:00:00Z");
    private static final int MAX_EVENTS_PER_PROJECTION = 500;

    private final BusinessBookingRepository businessBookingRepository;
    private final WorkmarketQuestRepository questRepository;
    private final WorkmarketQuestVisibilityService questVisibilityService;
    private final RideOfferRepository rideOfferRepository;

    public CalendarProjectionDTO getCalendar(AppUser viewer, Instant from, Instant to, List<String> requestedSources, Long businessId, String requestedView) {
        if (viewer == null) {
            throw ServiceErrors.forbidden("Authentication is required");
        }
        Instant resolvedFrom = from == null ? Instant.now().minus(Duration.ofDays(30)) : from;
        Instant resolvedTo = to == null ? resolvedFrom.plus(Duration.ofDays(31)) : to;
        if (!resolvedTo.isAfter(resolvedFrom) || Duration.between(resolvedFrom, resolvedTo).toDays() > 366) {
            throw ServiceErrors.badRequest("Calendar range must be between one moment and 366 days");
        }

        Set<String> sources = normalizeSources(requestedSources);
        String view = normalizeView(requestedView);
        List<CalendarEventDTO> events = new ArrayList<>();
        if (sources.contains("business")) events.addAll(businessEvents(viewer, resolvedFrom, resolvedTo, businessId));
        if (sources.contains("quest")) events.addAll(questEvents(viewer, resolvedFrom, resolvedTo));
        if (sources.contains("ride")) events.addAll(rideEvents(viewer, resolvedFrom, resolvedTo));
        events.sort((left, right) -> left.getStartsAt().compareTo(right.getStartsAt()));
        if (events.size() > MAX_EVENTS_PER_PROJECTION) {
            events = new ArrayList<>(events.subList(0, MAX_EVENTS_PER_PROJECTION));
        }

        return CalendarProjectionDTO.builder()
                .from(resolvedFrom)
                .to(resolvedTo)
                .view(view)
                .rangeKind(view.equals("AGENDA") ? "OPEN" : view)
                .timezone("UTC")
                .availableSources(List.of("business", "quest", "ride"))
                .events(events)
                .build();
    }

    private Set<String> normalizeSources(List<String> requestedSources) {
        if (requestedSources == null || requestedSources.isEmpty()) return new LinkedHashSet<>(SUPPORTED_SOURCES);
        Set<String> normalized = new LinkedHashSet<>();
        for (String raw : requestedSources) {
            if (raw == null) continue;
            for (String value : raw.split(",")) {
                String source = value.trim().toLowerCase(Locale.ROOT);
                if (!source.isBlank()) normalized.add(source);
            }
        }
        if (!SUPPORTED_SOURCES.containsAll(normalized)) throw ServiceErrors.badRequest("Calendar source is not supported");
        return normalized;
    }

    private List<CalendarEventDTO> businessEvents(AppUser viewer, Instant from, Instant to, Long businessId) {
        List<BusinessBooking> bookings = new ArrayList<>(businessBookingRepository.findDetailedByCustomerIdAndOverlap(viewer.getId(), from, to));
        bookings.addAll(businessBookingRepository.findDetailedByOwnerIdAndOverlap(viewer.getId(), from, to));
        return bookings.stream()
                .filter(booking -> businessId == null || businessId.equals(booking.getBusinessProfile().getId()))
                .map(this::businessEvent)
                .distinct()
                .toList();
    }

    private CalendarEventDTO businessEvent(BusinessBooking booking) {
        return CalendarEventDTO.builder()
                .eventKey("business:" + booking.getId())
                .source("business")
                .title(booking.getOfferingTitleSnapshot())
                .startsAt(booking.getStartsAt())
                .endsAt(booking.getEndsAt())
                .timezone(booking.getTimezone())
                .status(booking.getStatus().name())
                .businessId(booking.getBusinessProfile().getId())
                .businessName(booking.getBusinessProfile().getBusinessName())
                .navigationPath("/business/bookings/" + booking.getId())
                .allDay(false)
                .build();
    }

    private List<CalendarEventDTO> questEvents(AppUser viewer, Instant from, Instant to) {
        return questRepository.findForQuestList().stream()
                .filter(quest -> quest.getScheduledAt() != null && quest.getScheduledAt().isBefore(to))
                .filter(quest -> quest.getEndsAt() == null || quest.getEndsAt().isAfter(from))
                .filter(quest -> questVisibilityService.canViewQuest(viewer, quest))
                .map(this::questEvent)
                .toList();
    }

    private CalendarEventDTO questEvent(Quest quest) {
        Instant endsAt = quest.getEndsAt() == null ? quest.getScheduledAt().plus(Duration.ofHours(1)) : quest.getEndsAt();
        return CalendarEventDTO.builder()
                .eventKey("quest:" + quest.getId())
                .source("quest")
                .title(quest.getTitle())
                .startsAt(quest.getScheduledAt())
                .endsAt(endsAt)
                .timezone("UTC")
                .status(quest.getStatus().name())
                .navigationPath("/work/quests/" + quest.getId())
                .allDay(false)
                .build();
    }

    private List<CalendarEventDTO> rideEvents(AppUser viewer, Instant from, Instant to) {
        return rideOfferRepository.findVisibleActiveOffers(viewer.getId()).stream()
                .filter(ride -> !ride.getDepartureAt().isBefore(from) && ride.getDepartureAt().isBefore(to))
                .map(this::rideEvent)
                .toList();
    }

    private CalendarEventDTO rideEvent(RideOffer ride) {
        return CalendarEventDTO.builder()
                .eventKey("ride:" + ride.getId())
                .source("ride")
                .title(ride.getOrigin() + " → " + ride.getDestination())
                .startsAt(ride.getDepartureAt())
                .endsAt(ride.getDepartureAt().plus(Duration.ofHours(1)))
                .timezone("UTC")
                .status(ride.getStatus().name())
                .navigationPath("/rides/" + ride.getId())
                .allDay(false)
                .build();
    }

    private String normalizeView(String requestedView) {
        String view = requestedView == null || requestedView.isBlank() ? "AGENDA" : requestedView.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("AGENDA", "DAY", "WEEK", "MONTH").contains(view)) throw ServiceErrors.badRequest("Calendar view is not supported");
        return view;
    }
}
