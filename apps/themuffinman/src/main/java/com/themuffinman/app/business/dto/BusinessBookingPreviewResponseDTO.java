package com.themuffinman.app.business.dto;

import com.themuffinman.app.common.dto.ClientActionDTO;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class BusinessBookingPreviewResponseDTO {
    private Long businessOfferingId;
    private String offeringTitle;
    private Instant startsAt;
    private Instant endsAt;
    private String timezone;
    private Integer durationMinutes;
    private List<ClientActionDTO> actions;
}
