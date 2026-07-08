package com.themuffinman.app.business.mapper;

import com.themuffinman.app.business.dto.BusinessBookingResponseDTO;
import com.themuffinman.app.business.model.BusinessBooking;
import org.springframework.stereotype.Component;

@Component
public class BusinessBookingMgr {

    public BusinessBookingResponseDTO toDto(BusinessBooking booking) {
        if (booking == null) {
            return null;
        }

        return BusinessBookingResponseDTO.builder()
                .id(booking.getId())
                .businessProfileId(booking.getBusinessProfile().getId())
                .businessSlug(booking.getBusinessProfile().getSlug())
                .businessName(booking.getBusinessProfile().getBusinessName())
                .businessOfferingId(booking.getBusinessOffering().getId())
                .businessOfferingSlug(booking.getBusinessOffering().getSlug())
                .businessOfferingTitle(booking.getBusinessOffering().getTitle())
                .customerUserId(booking.getCustomerUser().getId())
                .customerUsername(booking.getCustomerUser().getUsername())
                .customerEmail(booking.getCustomerUser().getEmail())
                .status(booking.getStatus())
                .source(booking.getSource())
                .startsAt(booking.getStartsAt())
                .endsAt(booking.getEndsAt())
                .timezone(booking.getTimezone())
                .customerNote(booking.getCustomerNote())
                .ownerNote(booking.getOwnerNote())
                .offeringTitleSnapshot(booking.getOfferingTitleSnapshot())
                .priceSnapshotAmount(booking.getPriceSnapshotAmount())
                .priceSnapshotCurrency(booking.getPriceSnapshotCurrency())
                .durationSnapshotMinutes(booking.getDurationSnapshotMinutes())
                .idempotencyKey(booking.getIdempotencyKey())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
