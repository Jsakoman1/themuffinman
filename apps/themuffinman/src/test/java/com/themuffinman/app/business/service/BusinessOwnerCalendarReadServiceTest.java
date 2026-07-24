package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessBookingResponseDTO;
import com.themuffinman.app.business.dto.BusinessBookingAllowedActionDTO;
import com.themuffinman.app.business.mapper.BusinessBookingMgr;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingSource;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.config.BusinessProperties;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessOwnerCalendarReadServiceTest {

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    @Mock
    private BusinessBookingRepository businessBookingRepository;

    @Spy
    private BusinessBookingMgr businessBookingMgr = new BusinessBookingMgr();

    @Mock
    private BusinessBookingPresentationService businessBookingPresentationService;

    @Spy
    private BusinessProperties businessProperties = new BusinessProperties();

    @InjectMocks
    private BusinessOwnerCalendarReadService businessOwnerCalendarReadService;

    @Test
    void getMyCalendarGroupsBookingsByBusinessLocalDay() {
        AppUser owner = user(1L, "owner");
        BusinessProfile profile = profile(owner);
        ZoneId zoneId = ZoneId.of(profile.getTimezone());
        LocalDate startDate = LocalDate.of(2026, 7, 8);
        LocalDate endDate = LocalDate.of(2026, 7, 10);
        Instant from = startDate.atStartOfDay(zoneId).toInstant();
        Instant to = endDate.plusDays(1).atStartOfDay(zoneId).toInstant();

        BusinessBooking first = booking(5L, profile, owner, "alice", BusinessBookingStatus.CONFIRMED, Instant.parse("2026-07-08T06:30:00Z"));
        BusinessBooking second = booking(6L, profile, owner, "bob", BusinessBookingStatus.PENDING_CONFIRMATION, Instant.parse("2026-07-09T10:00:00Z"));

        when(businessProfileRepository.findByOwnerId(owner.getId())).thenReturn(Optional.of(profile));
        when(businessBookingRepository.findDetailedByOwnerIdAndOverlap(owner.getId(), from, to)).thenReturn(List.of(first, second));
        when(businessBookingPresentationService.enrichForOwner(any(BusinessBookingResponseDTO.class), any(BusinessBooking.class), eq(owner)))
                .thenAnswer(invocation -> {
                    BusinessBookingResponseDTO dto = invocation.getArgument(0);
                    BusinessBooking booking = invocation.getArgument(1);
                    dto.setStatusLabel(booking.getStatus() == BusinessBookingStatus.CONFIRMED ? "Confirmed" : "Pending confirmation");
                    dto.setBlockingReason("");
                    dto.setAllowedActions(List.of(BusinessBookingAllowedActionDTO.CANCEL_AS_OWNER));
                    return dto;
                });

        var result = businessOwnerCalendarReadService.getMyCalendar(owner, from, to);

        assertEquals(profile.getTimezone(), result.getTimezone());
        assertEquals(from, result.getFrom());
        assertEquals(to, result.getTo());
        assertEquals(2, result.getTotalBookings());
        assertEquals(3, result.getDays().size());
        assertEquals(1, result.getDays().get(0).getBookingCount());
        assertEquals(1, result.getDays().get(1).getBookingCount());
        assertEquals(0, result.getDays().get(2).getBookingCount());
        assertEquals("Confirmed", result.getDays().get(0).getItems().getFirst().getStatusLabel());
        assertEquals("Pending confirmation", result.getDays().get(1).getItems().getFirst().getStatusLabel());
        assertEquals("Europe/Zurich", result.getDays().get(0).getItems().getFirst().getTimezone());
    }

    private BusinessProfile profile(AppUser owner) {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(10L);
        profile.setOwner(owner);
        profile.setBusinessName("Dog Groomer");
        profile.setSlug("dog-groomer");
        profile.setTimezone("Europe/Zurich");
        profile.setBookingEnabled(true);
        profile.setActive(true);
        return profile;
    }

    private BusinessBooking booking(Long id, BusinessProfile profile, AppUser owner, String customerUsername, BusinessBookingStatus status, Instant startsAt) {
        AppUser customer = user(id + 100, customerUsername);

        BusinessOffering offering = new BusinessOffering();
        offering.setId(id + 200);
        offering.setBusinessProfile(profile);
        offering.setTitle("Trim");
        offering.setSlug("trim");
        offering.setBasePriceAmount(new BigDecimal("25.00"));
        offering.setBasePriceCurrency("EUR");

        BusinessBooking booking = new BusinessBooking();
        booking.setId(id);
        booking.setBusinessProfile(profile);
        booking.setBusinessOffering(offering);
        booking.setCustomerUser(customer);
        booking.setStatus(status);
        booking.setSource(BusinessBookingSource.CUSTOMER);
        booking.setStartsAt(startsAt);
        booking.setEndsAt(startsAt.plusSeconds(3600));
        booking.setTimezone(profile.getTimezone());
        booking.setOfferingTitleSnapshot("Trim");
        booking.setPriceSnapshotAmount(new BigDecimal("25.00"));
        booking.setPriceSnapshotCurrency("EUR");
        booking.setDurationSnapshotMinutes(60);
        return booking;
    }

    private AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("encoded");
        return user;
    }
}
