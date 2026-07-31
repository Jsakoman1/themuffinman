package com.themuffinman.app.calendar.service;

import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.rides.repository.RideOfferRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import com.themuffinman.app.workmarket.service.WorkmarketQuestVisibilityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarReadServiceTest {
    @Mock private BusinessBookingRepository businessBookingRepository;
    @Mock private WorkmarketQuestRepository questRepository;
    @Mock private WorkmarketQuestVisibilityService questVisibilityService;
    @Mock private RideOfferRepository rideOfferRepository;
    @InjectMocks private CalendarReadService calendarReadService;

    @Test
    void sourceFilterKeepsReadScopedToRequestedModule() {
        AppUser viewer = new AppUser();
        viewer.setId(7L);
        Instant from = Instant.parse("2026-07-24T00:00:00Z");
        Instant to = Instant.parse("2026-07-25T00:00:00Z");
        when(businessBookingRepository.findDetailedByCustomerIdAndOverlap(7L, from, to)).thenReturn(List.of());
        when(businessBookingRepository.findDetailedByOwnerIdAndOverlap(7L, from, to)).thenReturn(List.of());

        var result = calendarReadService.getCalendar(viewer, from, to, List.of("business"), null, "day");

        assertEquals(List.of(), result.getEvents());
        assertEquals(List.of("business", "quest", "ride"), result.getAvailableSources());
        assertEquals("Business bookings", result.getSources().getFirst().getLabel());
        assertEquals("#527a9b", result.getSources().get(1).getColor());
        assertEquals("DAY", result.getView());
        verify(questRepository, never()).findForQuestList();
        verify(rideOfferRepository, never()).findVisibleActiveOffers(7L);
    }

    @Test
    void rejectsUnknownSource() {
        AppUser viewer = new AppUser();
        viewer.setId(7L);

        assertThrows(RuntimeException.class, () -> calendarReadService.getCalendar(
            viewer, Instant.now(), Instant.now().plusSeconds(3600), List.of("unknown"), null, "day"));
    }

    @Test
    void rejectsUnknownView() {
        AppUser viewer = new AppUser();
        viewer.setId(7L);
        assertThrows(RuntimeException.class, () -> calendarReadService.getCalendar(viewer, Instant.now(), Instant.now().plusSeconds(3600), List.of("business"), null, "year"));
    }
}
