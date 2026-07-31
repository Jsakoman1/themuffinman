package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessAvailabilityWindowListResponseDTO;
import com.themuffinman.app.business.dto.BusinessPublicAvailabilityCalendarDTO;
import com.themuffinman.app.business.dto.BusinessPublicAvailabilityDayDTO;
import com.themuffinman.app.business.dto.BusinessPublicAvailabilitySlotDTO;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessAvailabilityExceptionRepository;
import com.themuffinman.app.business.repository.BusinessAvailabilityRuleRepository;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.time.TimeSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessAvailabilityReadService {

    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessOfferingRepository businessOfferingRepository;
    private final BusinessAvailabilityRuleRepository businessAvailabilityRuleRepository;
    private final BusinessAvailabilityExceptionRepository businessAvailabilityExceptionRepository;
    private final BusinessAvailabilityComputationService businessAvailabilityComputationService;
    private final BusinessResourceAssignmentService businessResourceAssignmentService;

    public BusinessAvailabilityWindowListResponseDTO getPublicAvailability(String slug, Long offeringId, Instant from, Instant to) {
        if (from == null || to == null || !to.isAfter(from)) {
            throw ServiceErrors.badRequest("Availability range is invalid");
        }
        BusinessProfile profile = findBookableProfile(slug);
        BusinessOffering offering = findOffering(profile, offeringId);
        if (offering.getFulfillmentMode() == com.themuffinman.app.business.model.BusinessOfferingFulfillmentMode.ALL_DAY_STAY
                && offering.getDurationMode() != com.themuffinman.app.business.model.BusinessOfferingDurationMode.ALL_DAY) {
            throw ServiceErrors.conflict("Stay offerings must use all-day duration configuration");
        }

        List<com.themuffinman.app.business.dto.BusinessAvailabilityWindowDTO> windows = businessAvailabilityComputationService.deriveWindows(
                        profile,
                        offering,
                        businessAvailabilityRuleRepository.findActiveByBusinessProfileId(profile.getId()),
                        businessAvailabilityExceptionRepository.findByBusinessProfileId(profile.getId()),
                        from,
                        to
                );
        return BusinessAvailabilityWindowListResponseDTO.builder()
                .items(windows.stream()
                        .filter(window -> businessResourceAssignmentService.hasAvailableResources(offering, window.getStartsAt(), window.getEndsAt()))
                        .toList())
                .build();
    }

    public BusinessAvailabilityWindowListResponseDTO getPublicAvailabilityForBusinessDate(String slug, Long offeringId, LocalDate date) {
        if (date == null) throw ServiceErrors.badRequest("Business date is required");
        BusinessProfile profile = findBookableProfile(slug);
        ZoneId zone = TimeSupport.requireZoneId(profile.getTimezone(), "Business timezone is required before deriving availability");
        Instant from = date.atStartOfDay(zone).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(zone).toInstant().minusNanos(1);
        return getPublicAvailability(slug, offeringId, from, to);
    }

    public BusinessPublicAvailabilityCalendarDTO getPublicAvailabilityCalendar(String slug, Long offeringId, LocalDate fromDate, int days) {
        return getPublicAvailabilityCalendar(slug, offeringId, fromDate, days, "MONTH");
    }

    public BusinessPublicAvailabilityCalendarDTO getPublicAvailabilityCalendar(String slug, Long offeringId, LocalDate fromDate, int days, String view) {
        if (fromDate == null) throw ServiceErrors.badRequest("Calendar start date is required");
        if (days < 1 || days > 31) throw ServiceErrors.badRequest("Calendar range must be between 1 and 31 days");
        String safeView = switch (view == null ? "MONTH" : view.toUpperCase()) {
            case "MONTH", "WEEK", "DAY" -> view.toUpperCase();
            default -> throw ServiceErrors.badRequest("Calendar view must be MONTH, WEEK, or DAY");
        };
        BusinessProfile profile = findBookableProfile(slug);
        ZoneId zone = TimeSupport.requireZoneId(profile.getTimezone(), "Business timezone is required before deriving availability");
        LocalDate toDate = fromDate.plusDays(days - 1L);
        List<com.themuffinman.app.business.dto.BusinessAvailabilityWindowDTO> windows = getPublicAvailability(
                slug, offeringId, fromDate.atStartOfDay(zone).toInstant(), toDate.plusDays(1).atStartOfDay(zone).toInstant()
        ).getItems();
        Map<LocalDate, List<com.themuffinman.app.business.dto.BusinessAvailabilityWindowDTO>> byDate = windows.stream()
                .collect(Collectors.groupingBy(window -> window.getStartsAt().atZone(zone).toLocalDate()));
        return BusinessPublicAvailabilityCalendarDTO.builder()
                .businessOfferingId(offeringId)
                .view(safeView)
                .timezone(profile.getTimezone())
                .fromDate(fromDate)
                .toDate(toDate)
                .days(fromDate.datesUntil(toDate.plusDays(1)).map(date -> {
                    List<com.themuffinman.app.business.dto.BusinessAvailabilityWindowDTO> slots = byDate.getOrDefault(date, List.of());
                    return BusinessPublicAvailabilityDayDTO.builder().date(date)
                            .availabilityState(slots.isEmpty() ? "UNAVAILABLE" : slots.size() == 1 ? "LIMITED" : "AVAILABLE")
                            .availableSlotCount(slots.size())
                            .slots(slots.stream().map(slot -> BusinessPublicAvailabilitySlotDTO.builder()
                                    .startsAt(slot.getStartsAt())
                                    .endsAt(slot.getEndsAt())
                                    .timezone(slot.getTimezone())
                                    .build()).toList())
                            .build();
                }).toList())
                .build();
    }

    private BusinessProfile findBookableProfile(String slug) {
        BusinessProfile profile = businessProfileRepository.findBySlug(slug)
                .filter(BusinessProfile::isActive)
                .orElseThrow(() -> ServiceErrors.notFound("Business profile not found"));
        if (!profile.isBookingEnabled()) throw ServiceErrors.notFound("Business availability not found");
        return profile;
    }

    private BusinessOffering findOffering(BusinessProfile profile, Long offeringId) {
        return businessOfferingRepository.findActiveByBusinessProfileId(profile.getId()).stream()
                .filter(candidate -> offeringId == null || candidate.getId().equals(offeringId))
                .findFirst()
                .orElseThrow(() -> ServiceErrors.notFound("Business offering not found"));
    }
}
