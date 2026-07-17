package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessBookingListResponseDTO;
import com.themuffinman.app.business.dto.BusinessBookingQueryDTO;
import com.themuffinman.app.business.dto.BusinessBookingRequestDTO;
import com.themuffinman.app.business.dto.BusinessBookingRescheduleRequestDTO;
import com.themuffinman.app.business.dto.BusinessBookingResponseDTO;
import com.themuffinman.app.business.dto.BusinessOwnerCalendarProjectionDTO;
import com.themuffinman.app.business.dto.BusinessOwnerBookingCreateRequestDTO;
import com.themuffinman.app.business.service.BusinessBookingReadService;
import com.themuffinman.app.business.service.BusinessCancelBookingUseCase;
import com.themuffinman.app.business.service.BusinessCompleteBookingUseCase;
import com.themuffinman.app.business.service.BusinessConfirmBookingUseCase;
import com.themuffinman.app.business.service.BusinessCreateBookingUseCase;
import com.themuffinman.app.business.service.BusinessNoShowBookingUseCase;
import com.themuffinman.app.business.service.BusinessOwnerCalendarReadService;
import com.themuffinman.app.business.service.BusinessOwnerScheduleReadService;
import com.themuffinman.app.business.service.BusinessRejectBookingUseCase;
import com.themuffinman.app.business.service.BusinessRescheduleBookingUseCase;
import com.themuffinman.app.identity.model.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/business/bookings")
@RequiredArgsConstructor
public class BusinessBookingController {

    private final BusinessCreateBookingUseCase businessCreateBookingUseCase;
    private final BusinessBookingReadService businessBookingReadService;
    private final BusinessCancelBookingUseCase businessCancelBookingUseCase;
    private final BusinessConfirmBookingUseCase businessConfirmBookingUseCase;
    private final BusinessRejectBookingUseCase businessRejectBookingUseCase;
    private final BusinessCompleteBookingUseCase businessCompleteBookingUseCase;
    private final BusinessNoShowBookingUseCase businessNoShowBookingUseCase;
    private final BusinessOwnerScheduleReadService businessOwnerScheduleReadService;
    private final BusinessOwnerCalendarReadService businessOwnerCalendarReadService;
    private final BusinessRescheduleBookingUseCase businessRescheduleBookingUseCase;

    @PostMapping
    public BusinessBookingResponseDTO createBooking(
            @Valid @RequestBody BusinessBookingRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessCreateBookingUseCase.createCustomerBooking(dto, currentUser);
    }

    @GetMapping("/me")
    public BusinessBookingListResponseDTO getMyBookings(
            @ModelAttribute BusinessBookingQueryDTO query,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessBookingReadService.getMyBookings(query, currentUser);
    }

    @GetMapping("/me/{bookingId}")
    public BusinessBookingResponseDTO getMyBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessBookingReadService.getMyBooking(bookingId, currentUser);
    }

    @PostMapping("/me/{bookingId}/cancel")
    public BusinessBookingResponseDTO cancelMyBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessCancelBookingUseCase.cancelAsCustomer(bookingId, currentUser);
    }

    @PostMapping("/me/{bookingId}/reschedule")
    public BusinessBookingResponseDTO rescheduleMyBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody BusinessBookingRescheduleRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessRescheduleBookingUseCase.rescheduleAsCustomer(bookingId, request, currentUser);
    }

    @PostMapping("/owner")
    public BusinessBookingResponseDTO createOwnerBooking(
            @Valid @RequestBody BusinessOwnerBookingCreateRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessCreateBookingUseCase.createOwnerBooking(dto, currentUser);
    }

    @GetMapping("/owner")
    public BusinessBookingListResponseDTO getOwnerBookings(
            @ModelAttribute BusinessBookingQueryDTO query,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessBookingReadService.getOwnerBookings(query, currentUser);
    }

    @GetMapping("/owner/schedule")
    public com.themuffinman.app.business.dto.BusinessOwnerScheduleSummaryDTO getOwnerSchedule(
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessOwnerScheduleReadService.getMyScheduleSummary(currentUser);
    }

    @GetMapping("/owner/calendar")
    public BusinessOwnerCalendarProjectionDTO getOwnerCalendar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessOwnerCalendarReadService.getMyCalendar(currentUser, from, to);
    }

    @GetMapping("/owner/{bookingId}")
    public BusinessBookingResponseDTO getOwnerBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessBookingReadService.getOwnerBooking(bookingId, currentUser);
    }

    @PostMapping("/owner/{bookingId}/confirm")
    public BusinessBookingResponseDTO confirmBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessConfirmBookingUseCase.execute(bookingId, currentUser);
    }

    @PostMapping("/owner/{bookingId}/reject")
    public BusinessBookingResponseDTO rejectBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessRejectBookingUseCase.execute(bookingId, currentUser);
    }

    @PostMapping("/owner/{bookingId}/cancel")
    public BusinessBookingResponseDTO cancelOwnerBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessCancelBookingUseCase.cancelAsOwner(bookingId, currentUser);
    }

    @PostMapping("/owner/{bookingId}/reschedule")
    public BusinessBookingResponseDTO rescheduleOwnerBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody BusinessBookingRescheduleRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessRescheduleBookingUseCase.rescheduleAsOwner(bookingId, request, currentUser);
    }

    @PostMapping("/owner/{bookingId}/complete")
    public BusinessBookingResponseDTO completeBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessCompleteBookingUseCase.complete(bookingId, currentUser);
    }

    @PostMapping("/owner/{bookingId}/mark-no-show")
    public BusinessBookingResponseDTO markNoShow(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessNoShowBookingUseCase.markNoShow(bookingId, currentUser);
    }
}
