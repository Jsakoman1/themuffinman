package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessBookingListResponseDTO;
import com.themuffinman.app.business.dto.BusinessBookingQueryDTO;
import com.themuffinman.app.business.dto.BusinessBookingResponseDTO;
import com.themuffinman.app.business.mapper.BusinessBookingMgr;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.pagination.PageResponseFactory;
import com.themuffinman.app.common.pagination.PaginationSupport;
import com.themuffinman.app.common.search.SearchTextSupport;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.config.BusinessProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessBookingReadService {

    private final BusinessBookingRepository businessBookingRepository;
    private final BusinessBookingMgr businessBookingMgr;
    private final BusinessBookingPresentationService businessBookingPresentationService;
    private final BusinessProperties businessProperties;

    public BusinessBookingListResponseDTO getMyBookings(BusinessBookingQueryDTO query, AppUser currentUser) {
        int safePage = PaginationSupport.safePage(query == null ? null : query.getPage());
        int safeSize = PaginationSupport.safeSize(
                query == null ? null : query.getSize(),
                businessProperties.getLists().getDefaultPageSize(),
                businessProperties.getLists().getMaxPageSize()
        );
        String normalizedQuery = SearchTextSupport.normalizeQuery(query == null ? null : query.getQ());

        List<BusinessBookingResponseDTO> filtered = businessBookingRepository.findDetailedByCustomerUserId(currentUser.getId()).stream()
                .filter(booking -> matchesQuery(booking, normalizedQuery, query))
                .sorted(Comparator.comparing(BusinessBooking::getStartsAt).reversed())
                .map(booking -> businessBookingPresentationService.enrichForCustomer(businessBookingMgr.toDto(booking), booking, currentUser))
                .toList();

        return PageResponseFactory.fromItems(filtered, safePage, safeSize, pageWindow -> BusinessBookingListResponseDTO.builder()
                .items(pageWindow.items())
                .page(pageWindow.page())
                .size(pageWindow.size())
                .totalItems(pageWindow.totalItems())
                .totalPages(pageWindow.totalPages())
                .build());
    }

    public BusinessBookingResponseDTO getMyBooking(Long bookingId, AppUser currentUser) {
        BusinessBooking booking = businessBookingRepository.findDetailedByIdAndCustomerUserId(bookingId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Booking not found"));
        return businessBookingPresentationService.enrichForCustomer(businessBookingMgr.toDto(booking), booking, currentUser);
    }

    public BusinessBookingListResponseDTO getOwnerBookings(BusinessBookingQueryDTO query, AppUser currentUser) {
        int safePage = PaginationSupport.safePage(query == null ? null : query.getPage());
        int safeSize = PaginationSupport.safeSize(
                query == null ? null : query.getSize(),
                businessProperties.getLists().getDefaultPageSize(),
                businessProperties.getLists().getMaxPageSize()
        );
        String normalizedQuery = SearchTextSupport.normalizeQuery(query == null ? null : query.getQ());

        List<BusinessBookingResponseDTO> filtered = businessBookingRepository.findDetailedByOwnerId(currentUser.getId()).stream()
                .filter(booking -> matchesQuery(booking, normalizedQuery, query))
                .sorted(Comparator.comparing(BusinessBooking::getStartsAt).reversed())
                .map(booking -> businessBookingPresentationService.enrichForOwner(businessBookingMgr.toDto(booking), booking, currentUser))
                .toList();

        return PageResponseFactory.fromItems(filtered, safePage, safeSize, pageWindow -> BusinessBookingListResponseDTO.builder()
                .items(pageWindow.items())
                .page(pageWindow.page())
                .size(pageWindow.size())
                .totalItems(pageWindow.totalItems())
                .totalPages(pageWindow.totalPages())
                .build());
    }

    public BusinessBookingResponseDTO getOwnerBooking(Long bookingId, AppUser currentUser) {
        BusinessBooking booking = businessBookingRepository.findDetailedByIdAndOwnerId(bookingId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Booking not found"));
        return businessBookingPresentationService.enrichForOwner(businessBookingMgr.toDto(booking), booking, currentUser);
    }

    private boolean matchesQuery(BusinessBooking booking, String normalizedQuery, BusinessBookingQueryDTO query) {
        if (query != null && query.getBusinessProfileId() != null && !booking.getBusinessProfile().getId().equals(query.getBusinessProfileId())) {
            return false;
        }
        if (query != null && query.getStatus() != null && booking.getStatus() != query.getStatus()) {
            return false;
        }
        if (query != null && query.getFrom() != null && !booking.getEndsAt().isAfter(query.getFrom())) {
            return false;
        }
        if (query != null && query.getTo() != null && !booking.getStartsAt().isBefore(query.getTo())) {
            return false;
        }
        if (normalizedQuery.isBlank()) {
            return true;
        }
        return SearchTextSupport.containsAnyNormalized(normalizedQuery,
                booking.getBusinessProfile().getSlug(),
                booking.getBusinessProfile().getBusinessName(),
                booking.getCustomerUser().getUsername(),
                booking.getCustomerUser().getEmail(),
                booking.getOfferingTitleSnapshot(),
                booking.getStatus().name()
        );
    }
}
