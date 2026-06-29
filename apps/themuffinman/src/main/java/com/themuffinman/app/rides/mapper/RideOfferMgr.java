package com.themuffinman.app.rides.mapper;

import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.rides.dto.RideOfferResponseDTO;
import com.themuffinman.app.rides.model.RideOffer;
import com.themuffinman.app.social.model.CircleGroup;
import org.springframework.stereotype.Component;

@Component
public class RideOfferMgr {
    public RideOfferResponseDTO toDto(RideOffer offer) {
        return RideOfferResponseDTO.builder()
                .id(offer.getId())
                .driverId(offer.getDriver().getId())
                .driverUsername(offer.getDriver().getUsername())
                .origin(offer.getOrigin())
                .destination(offer.getDestination())
                .departureAt(offer.getDepartureAt())
                .seats(offer.getSeats())
                .note(RichTextInputValidator.sanitize(offer.getNote()))
                .active(offer.isActive())
                .visibleCircleNames(offer.getVisibleCircles().stream().map(CircleGroup::getName).sorted().toList())
                .createdAt(offer.getCreatedAt())
                .build();
    }
}
