package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessBookingQueryDTO;
import com.themuffinman.app.business.dto.BusinessBookingResponseDTO;
import com.themuffinman.app.business.mapper.BusinessBookingMgr;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingSource;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessBookingReadServiceTest {

    @Mock
    private BusinessBookingRepository businessBookingRepository;

    @Spy
    private BusinessBookingMgr businessBookingMgr = new BusinessBookingMgr();

    @Mock
    private BusinessBookingPresentationService businessBookingPresentationService;

    @Spy
    private BusinessProperties businessProperties = new BusinessProperties();

    @InjectMocks
    private BusinessBookingReadService businessBookingReadService;

    @Test
    void getMyBookingsPaginatesAndSortsByStartTimeDescending() {
        AppUser owner = user(1L, "owner");
        AppUser customer = user(2L, "customer");
        BusinessBooking earlier = booking(5L, owner, customer, BusinessBookingStatus.CONFIRMED, "2026-07-09T09:00:00Z");
        BusinessBooking later = booking(6L, owner, customer, BusinessBookingStatus.CONFIRMED, "2026-07-09T11:00:00Z");

        when(businessBookingRepository.findDetailedByCustomerUserId(customer.getId())).thenReturn(List.of(earlier, later));
        when(businessBookingPresentationService.enrichForCustomer(any(BusinessBookingResponseDTO.class), any(BusinessBooking.class), any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessBookingQueryDTO query = new BusinessBookingQueryDTO();
        query.setPage(0);
        query.setSize(1);

        var result = businessBookingReadService.getMyBookings(query, customer);

        assertEquals(0, result.getPage());
        assertEquals(1, result.getSize());
        assertEquals(2, result.getTotalItems());
        assertEquals(2, result.getTotalPages());
        assertEquals(later.getId(), result.getItems().getFirst().getId());
    }

    @Test
    void getOwnerBookingsAppliesStatusAndSearchFilters() {
        AppUser owner = user(1L, "owner");
        AppUser customerOne = user(2L, "alice");
        customerOne.setEmail("alice@example.com");
        AppUser customerTwo = user(3L, "bob");
        customerTwo.setEmail("bob@example.com");

        BusinessBooking matching = booking(5L, owner, customerOne, BusinessBookingStatus.CONFIRMED, "2026-07-09T09:00:00Z");
        BusinessBooking other = booking(6L, owner, customerTwo, BusinessBookingStatus.PENDING_CONFIRMATION, "2026-07-09T11:00:00Z");

        when(businessBookingRepository.findDetailedByOwnerId(owner.getId())).thenReturn(List.of(matching, other));
        when(businessBookingPresentationService.enrichForOwner(any(BusinessBookingResponseDTO.class), any(BusinessBooking.class), any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessBookingQueryDTO query = new BusinessBookingQueryDTO();
        query.setQ("alice@example.com");
        query.setStatus(BusinessBookingStatus.CONFIRMED);
        query.setPage(0);
        query.setSize(10);

        var result = businessBookingReadService.getOwnerBookings(query, owner);

        assertEquals(1, result.getTotalItems());
        assertEquals(1, result.getItems().size());
        assertEquals(matching.getId(), result.getItems().getFirst().getId());
    }

    private BusinessBooking booking(Long id, AppUser owner, AppUser customer, BusinessBookingStatus status, String startsAt) {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(10L);
        profile.setOwner(owner);
        profile.setBusinessName("Dog Groomer");
        profile.setSlug("dog-groomer");
        profile.setTimezone("Europe/Zurich");
        profile.setBookingEnabled(true);
        profile.setActive(true);

        BusinessOffering offering = new BusinessOffering();
        offering.setId(20L);
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
        booking.setStartsAt(Instant.parse(startsAt));
        booking.setEndsAt(Instant.parse(startsAt).plusSeconds(3600));
        booking.setTimezone("Europe/Zurich");
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
