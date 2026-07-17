package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessBookingAllowedActionDTO;
import com.themuffinman.app.business.dto.BusinessBookingPresentationDTO;
import com.themuffinman.app.business.dto.BusinessBookingResponseDTO;
import com.themuffinman.app.common.dto.ClientActionDTO;
import com.themuffinman.app.common.dto.ClientActionToneDTO;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.common.time.TimeSupport;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessBookingPresentationService {

    private final BusinessBookingPolicyService businessBookingPolicyService;

    public BusinessBookingResponseDTO enrichForCustomer(BusinessBookingResponseDTO dto, BusinessBooking booking, AppUser currentUser) {
        Instant now = TimeSupport.now();
        var policy = businessBookingPolicyService.resolveEffectivePolicy(booking.getBusinessProfile().getOwner());
        List<BusinessBookingAllowedActionDTO> allowedActions = resolveCustomerAllowedActions(booking, currentUser, now, policy);
        String blockingReason = resolveCustomerBlockingReason(booking, currentUser, now, policy);
        dto.setAllowedActions(List.copyOf(allowedActions));
        dto.setActions(toClientActions(allowedActions, "customer"));
        dto.setStatusLabel(formatStatusLabel(booking.getStatus()));
        dto.setBlockingReason(blockingReason);
        dto.setPresentation(BusinessBookingPresentationDTO.builder()
                .statusLabel(dto.getStatusLabel())
                .blockingReason(blockingReason)
                .build());
        return dto;
    }

    public BusinessBookingResponseDTO enrichForOwner(BusinessBookingResponseDTO dto, BusinessBooking booking, AppUser currentUser) {
        Instant now = TimeSupport.now();
        var policy = businessBookingPolicyService.resolveEffectivePolicy(currentUser);
        List<BusinessBookingAllowedActionDTO> allowedActions = resolveOwnerAllowedActions(booking, currentUser, now, policy);
        String blockingReason = resolveOwnerBlockingReason(booking, currentUser, now);
        dto.setAllowedActions(List.copyOf(allowedActions));
        dto.setActions(toClientActions(allowedActions, "owner"));
        dto.setStatusLabel(formatStatusLabel(booking.getStatus()));
        dto.setBlockingReason(blockingReason);
        dto.setPresentation(BusinessBookingPresentationDTO.builder()
                .statusLabel(dto.getStatusLabel())
                .blockingReason(blockingReason)
                .build());
        return dto;
    }

    public List<BusinessBookingAllowedActionDTO> resolveCustomerAllowedActions(BusinessBooking booking, AppUser currentUser, Instant now) {
        if (booking == null || currentUser == null) {
            return List.of();
        }
        return resolveCustomerAllowedActions(
                booking,
                currentUser,
                now,
                businessBookingPolicyService.resolveEffectivePolicy(booking.getBusinessProfile().getOwner())
        );
    }

    private List<BusinessBookingAllowedActionDTO> resolveCustomerAllowedActions(
            BusinessBooking booking,
            AppUser currentUser,
            Instant now,
            com.themuffinman.app.business.model.BusinessBookingPolicy policy
    ) {
        if (booking == null || currentUser == null || !booking.getCustomerUser().getId().equals(currentUser.getId())) {
            return List.of();
        }

        if ((booking.getStatus() == BusinessBookingStatus.PENDING_CONFIRMATION || booking.getStatus() == BusinessBookingStatus.CONFIRMED)
                && booking.getStartsAt().minusSeconds(cancelWindowSeconds(policy)).isAfter(now)) {
            List<BusinessBookingAllowedActionDTO> actions = new ArrayList<>();
            if (policy.isAllowCustomerCancellation()) {
                actions.add(BusinessBookingAllowedActionDTO.CANCEL);
            }
            actions.add(BusinessBookingAllowedActionDTO.RESCHEDULE);
            return List.copyOf(actions);
        }

        return List.of();
    }

    public List<BusinessBookingAllowedActionDTO> resolveOwnerAllowedActions(BusinessBooking booking, AppUser currentUser, Instant now) {
        if (booking == null || currentUser == null) {
            return List.of();
        }
        return resolveOwnerAllowedActions(
                booking,
                currentUser,
                now,
                businessBookingPolicyService.resolveEffectivePolicy(currentUser)
        );
    }

    private List<BusinessBookingAllowedActionDTO> resolveOwnerAllowedActions(
            BusinessBooking booking,
            AppUser currentUser,
            Instant now,
            com.themuffinman.app.business.model.BusinessBookingPolicy policy
    ) {
        if (booking == null || currentUser == null || !booking.getBusinessProfile().getOwner().getId().equals(currentUser.getId())) {
            return List.of();
        }

        List<BusinessBookingAllowedActionDTO> actions = new ArrayList<>();
        if (booking.getStatus() == BusinessBookingStatus.PENDING_CONFIRMATION) {
            if (policy.isAllowOwnerManualApproval()) {
                actions.add(BusinessBookingAllowedActionDTO.CONFIRM);
            }
            if (policy.isAllowOwnerManualRejection()) {
                actions.add(BusinessBookingAllowedActionDTO.REJECT);
            }
            actions.add(BusinessBookingAllowedActionDTO.CANCEL_AS_OWNER);
            return List.copyOf(actions);
        }

        if (booking.getStatus() == BusinessBookingStatus.CONFIRMED) {
            actions.add(BusinessBookingAllowedActionDTO.CANCEL_AS_OWNER);
            if (booking.getStartsAt().minusSeconds(policy.getOwnerRescheduleWindowMinutes() * 60L).isAfter(now)) {
                actions.add(BusinessBookingAllowedActionDTO.RESCHEDULE);
            }
            if (!booking.getStartsAt().isAfter(now)) {
                actions.add(BusinessBookingAllowedActionDTO.MARK_NO_SHOW);
            }
            if (!booking.getEndsAt().isAfter(now)) {
                actions.add(BusinessBookingAllowedActionDTO.COMPLETE);
            }
        }

        return List.copyOf(actions);
    }

    public String formatStatusLabel(BusinessBookingStatus status) {
        return switch (status) {
            case PENDING_CONFIRMATION -> "Pending confirmation";
            case CONFIRMED -> "Confirmed";
            case REJECTED -> "Rejected";
            case CANCELLED_BY_CUSTOMER -> "Cancelled by customer";
            case CANCELLED_BY_OWNER -> "Cancelled by owner";
            case COMPLETED -> "Completed";
            case NO_SHOW -> "No-show";
        };
    }

    private List<ClientActionDTO> toClientActions(List<BusinessBookingAllowedActionDTO> actions, String actor) {
        return actions.stream().map(action -> {
            boolean destructive = action == BusinessBookingAllowedActionDTO.CANCEL
                    || action == BusinessBookingAllowedActionDTO.CANCEL_AS_OWNER
                    || action == BusinessBookingAllowedActionDTO.REJECT;
            String label = switch (action) {
                case CANCEL -> "Cancel booking";
                case CONFIRM -> "Confirm booking";
                case REJECT -> "Reject booking";
                case COMPLETE -> "Mark complete";
                case MARK_NO_SHOW -> "Mark no-show";
                case CANCEL_AS_OWNER -> "Cancel booking";
                case RESCHEDULE -> "Reschedule booking";
            };
            return ClientActionDTO.builder()
                    .id(action.name())
                    .label(label)
                    .tone(destructive ? ClientActionToneDTO.DANGER : ClientActionToneDTO.PRIMARY)
                    .enabled(true)
                    .requiresConfirmation(destructive || action == BusinessBookingAllowedActionDTO.CONFIRM)
                    .confirmationTitle(label)
                    .confirmationMessage(actor.equals("owner") && action == BusinessBookingAllowedActionDTO.REJECT
                            ? "Reject this booking request?"
                            : label + "?")
                    .build();
        }).toList();
    }

    private String resolveCustomerBlockingReason(BusinessBooking booking, AppUser currentUser, Instant now) {
        if (booking == null || currentUser == null) {
            return "Booking is not accessible.";
        }
        return resolveCustomerBlockingReason(
                booking,
                currentUser,
                now,
                businessBookingPolicyService.resolveEffectivePolicy(booking.getBusinessProfile().getOwner())
        );
    }

    private String resolveCustomerBlockingReason(
            BusinessBooking booking,
            AppUser currentUser,
            Instant now,
            com.themuffinman.app.business.model.BusinessBookingPolicy policy
    ) {
        if (booking == null || currentUser == null || !booking.getCustomerUser().getId().equals(currentUser.getId())) {
            return "Booking is not accessible.";
        }
        if (!policy.isAllowCustomerCancellation()) {
            return "Customer cancellation is disabled by business policy.";
        }
        if (booking.getStatus() != BusinessBookingStatus.PENDING_CONFIRMATION && booking.getStatus() != BusinessBookingStatus.CONFIRMED) {
            return "";
        }
        if (!booking.getStartsAt().minusSeconds(cancelWindowSeconds(policy)).isAfter(now)) {
            return "Customer cancellation window has closed.";
        }
        return "";
    }

    private String resolveOwnerBlockingReason(BusinessBooking booking, AppUser currentUser, Instant now) {
        if (booking == null || currentUser == null || !booking.getBusinessProfile().getOwner().getId().equals(currentUser.getId())) {
            return "Booking is not accessible.";
        }
        if (booking.getStatus() == BusinessBookingStatus.PENDING_CONFIRMATION) {
            return "";
        }
        if (booking.getStatus() == BusinessBookingStatus.CONFIRMED && booking.getEndsAt().isAfter(now)) {
            return "Completion stays blocked until the booking end time.";
        }
        return "";
    }

    private long cancelWindowSeconds(com.themuffinman.app.business.model.BusinessBookingPolicy policy) {
        return (long) policy.getCustomerCancellationWindowMinutes() * 60L;
    }
}
