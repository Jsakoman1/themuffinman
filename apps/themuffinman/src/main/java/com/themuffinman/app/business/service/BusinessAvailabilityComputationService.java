package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessAvailabilityWindowDTO;
import com.themuffinman.app.business.model.BusinessAvailabilityException;
import com.themuffinman.app.business.model.BusinessAvailabilityExceptionType;
import com.themuffinman.app.business.model.BusinessAvailabilityRule;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.time.TimeSupport;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class BusinessAvailabilityComputationService {

    public List<BusinessAvailabilityWindowDTO> deriveWindows(
            BusinessProfile profile,
            BusinessOffering offering,
            List<BusinessAvailabilityRule> rules,
            List<BusinessAvailabilityException> exceptions,
            Instant from,
            Instant to
    ) {
        validateRange(profile, from, to);

        ZoneId zoneId = TimeSupport.requireZoneId(profile.getTimezone(), "Business timezone is required before deriving availability");
        LocalDate startDate = from.atZone(zoneId).toLocalDate();
        LocalDate endDate = to.atZone(zoneId).toLocalDate();
        List<BusinessAvailabilityWindowDTO> windows = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate currentDate = date;
            int dayOfWeek = date.getDayOfWeek().getValue();
            List<BusinessAvailabilityRule> matchingRules = rules.stream()
                    .filter(BusinessAvailabilityRule::isActive)
                    .filter(rule -> rule.getDayOfWeek() == dayOfWeek)
                    .filter(rule -> rule.getValidFrom() == null || !currentDate.isBefore(rule.getValidFrom()))
                    .filter(rule -> rule.getValidUntil() == null || !currentDate.isAfter(rule.getValidUntil()))
                    .filter(rule -> appliesToOffering(rule.getBusinessOffering(), offering))
                    .toList();

            for (BusinessAvailabilityRule rule : matchingRules) {
                int effectiveCapacity = rule.getCapacityOverride() == null
                        ? offering.getSlotCapacity()
                        : rule.getCapacityOverride();
                ZonedDateTime cursor = ZonedDateTime.of(date, rule.getStartTimeLocal(), zoneId);
                ZonedDateTime boundary = ZonedDateTime.of(date, rule.getEndTimeLocal(), zoneId);

                int durationMinutes = effectiveDurationMinutes(offering, rule);
                while (cursor.isBefore(boundary)) {
                    ZonedDateTime nextCursor = cursor.plusMinutes(rule.getSlotGranularityMinutes());
                    ZonedDateTime slotEnd = cursor.plusMinutes(durationMinutes);
                    if (slotEnd.isAfter(boundary)) {
                        break;
                    }

                    Instant slotStart = cursor.toInstant();
                    Instant slotEndInstant = slotEnd.toInstant();
                    if (!slotEndInstant.isAfter(from) || slotStart.isAfter(to)) {
                        cursor = nextCursor;
                        continue;
                    }
                    if (isBlocked(exceptions, offering, slotStart, slotEndInstant)) {
                        cursor = nextCursor;
                        continue;
                    }

                    BusinessAvailabilityException replacement = replacementException(exceptions, offering, slotStart, slotEndInstant);
                    int capacity = replacement != null && replacement.getReplacementCapacity() != null
                            ? replacement.getReplacementCapacity()
                            : effectiveCapacity;

                    windows.add(BusinessAvailabilityWindowDTO.builder()
                            .businessProfileId(profile.getId())
                            .businessOfferingId(offering.getId())
                            .businessOfferingTitle(offering.getTitle())
                            .startsAt(slotStart)
                            .endsAt(slotEndInstant)
                            .timezone(profile.getTimezone())
                            .effectiveCapacity(capacity)
                            .build());

                    cursor = nextCursor;
                }
            }
        }

        return windows.stream()
                .sorted(Comparator.comparing(BusinessAvailabilityWindowDTO::getStartsAt)
                        .thenComparing(BusinessAvailabilityWindowDTO::getEndsAt))
                .toList();
    }

    private int effectiveDurationMinutes(BusinessOffering offering, BusinessAvailabilityRule rule) {
        if (offering.getDurationMode() == com.themuffinman.app.business.model.BusinessOfferingDurationMode.FIXED
                && offering.getDefaultDurationMinutes() != null) {
            return offering.getDefaultDurationMinutes();
        }
        return rule.getSlotGranularityMinutes();
    }

    private void validateRange(BusinessProfile profile, Instant from, Instant to) {
        if (profile.getTimezone() == null || profile.getTimezone().isBlank()) {
            throw ServiceErrors.badRequest("Business timezone is required before deriving availability");
        }
        if (from == null || to == null || !to.isAfter(from)) {
            throw ServiceErrors.badRequest("Availability range is invalid");
        }
    }

    private boolean appliesToOffering(BusinessOffering scopedOffering, BusinessOffering requestedOffering) {
        return scopedOffering == null || Objects.equals(scopedOffering.getId(), requestedOffering.getId());
    }

    private boolean isBlocked(List<BusinessAvailabilityException> exceptions, BusinessOffering offering, Instant start, Instant end) {
        return exceptions.stream()
                .filter(exception -> exception.getExceptionType() == BusinessAvailabilityExceptionType.BLOCK)
                .filter(exception -> appliesToOffering(exception.getBusinessOffering(), offering))
                .anyMatch(exception -> overlaps(exception.getStartAt(), exception.getEndAt(), start, end));
    }

    private BusinessAvailabilityException replacementException(
            List<BusinessAvailabilityException> exceptions,
            BusinessOffering offering,
            Instant start,
            Instant end
    ) {
        return exceptions.stream()
                .filter(exception -> exception.getExceptionType() == BusinessAvailabilityExceptionType.REPLACE_WINDOW)
                .filter(exception -> appliesToOffering(exception.getBusinessOffering(), offering))
                .filter(exception -> overlaps(exception.getStartAt(), exception.getEndAt(), start, end))
                .findFirst()
                .orElse(null);
    }

    private boolean overlaps(Instant aStart, Instant aEnd, Instant bStart, Instant bEnd) {
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }
}
