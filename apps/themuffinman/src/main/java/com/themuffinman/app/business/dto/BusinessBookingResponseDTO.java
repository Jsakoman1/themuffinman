package com.themuffinman.app.business.dto;

import com.themuffinman.app.common.dto.ClientActionDTO;
import com.themuffinman.app.business.model.BusinessBookingSource;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class BusinessBookingResponseDTO {
    private Long id;
    private Long businessProfileId;
    private String businessSlug;
    private String businessName;
    private Long businessOfferingId;
    private String businessOfferingSlug;
    private String businessOfferingTitle;
    private Long customerUserId;
    private String customerUsername;
    private String customerEmail;
    private BusinessBookingStatus status;
    private BusinessBookingSource source;
    private Instant startsAt;
    private Instant endsAt;
    private String timezone;
    private String customerNote;
    private String ownerNote;
    private String offeringTitleSnapshot;
    private BigDecimal priceSnapshotAmount;
    private String priceSnapshotCurrency;
    private Integer durationSnapshotMinutes;
    private String idempotencyKey;
    private List<BusinessBookingAllowedActionDTO> allowedActions;
    private List<ClientActionDTO> actions;
    private String statusLabel;
    private String blockingReason;
    private BusinessBookingPresentationDTO presentation;
    private Instant createdAt;
    private Instant updatedAt;
}
