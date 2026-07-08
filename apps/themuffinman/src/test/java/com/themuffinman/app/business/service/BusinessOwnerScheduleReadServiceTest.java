package com.themuffinman.app.business.service;

import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingSource;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
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
class BusinessOwnerScheduleReadServiceTest {

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    @Mock
    private BusinessBookingRepository businessBookingRepository;

    @Mock
    private BusinessBookingPresentationService businessBookingPresentationService;

    @InjectMocks
    private BusinessOwnerScheduleReadService businessOwnerScheduleReadService;

    @Test
    void getMyScheduleSummaryUsesBusinessTimezoneAndSharedProjectionData() {
        AppUser owner = user(1L, "owner");
        BusinessProfile profile = profile(owner);
        ZoneId zoneId = ZoneId.of(profile.getTimezone());
        LocalDate today = LocalDate.now(zoneId);
        Instant dayStart = today.atStartOfDay(zoneId).toInstant();
        Instant dayEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant();

        BusinessBooking todayBooking = booking(5L, profile, owner, "alice", BusinessBookingStatus.PENDING_CONFIRMATION, dayStart.plus(Duration.ofHours(2)));
        BusinessBooking upcomingBooking = booking(6L, profile, owner, "bob", BusinessBookingStatus.CONFIRMED, dayEnd.plus(Duration.ofHours(2)));

        when(businessProfileRepository.findByOwnerId(owner.getId())).thenReturn(Optional.of(profile));
        when(businessBookingRepository.findDetailedByOwnerIdAndStartsAtBetween(owner.getId(), dayStart, dayEnd)).thenReturn(List.of(todayBooking));
        when(businessBookingRepository.findUpcomingDetailedByOwnerId(eq(owner.getId()), anyCollection(), any(Instant.class)))
                .thenReturn(List.of(todayBooking, upcomingBooking));
        when(businessBookingPresentationService.formatStatusLabel(BusinessBookingStatus.PENDING_CONFIRMATION)).thenReturn("Pending confirmation");
        when(businessBookingPresentationService.formatStatusLabel(BusinessBookingStatus.CONFIRMED)).thenReturn("Confirmed");

        var result = businessOwnerScheduleReadService.getMyScheduleSummary(owner);

        assertEquals(profile.getTimezone(), result.getTimezone());
        assertEquals(1, result.getTodayCount());
        assertEquals(1, result.getPendingConfirmationCount());
        assertEquals(2, result.getUpcomingCount());
        assertEquals(2, result.getNextItems().size());
        assertEquals("Pending confirmation", result.getNextItems().getFirst().getStatusLabel());
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
