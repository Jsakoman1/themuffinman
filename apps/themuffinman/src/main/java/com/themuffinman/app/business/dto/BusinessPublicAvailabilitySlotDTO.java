package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/** Safe public slot projection: capacity and owner scheduling details remain private. */
@Getter
@Builder
public class BusinessPublicAvailabilitySlotDTO {
    private Instant startsAt;
    private Instant endsAt;
    private String timezone;
}
