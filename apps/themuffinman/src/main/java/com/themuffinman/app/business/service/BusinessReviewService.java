package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessRatingSummaryDTO;
import com.themuffinman.app.business.dto.BusinessReviewListResponseDTO;
import com.themuffinman.app.business.dto.BusinessReviewRequestDTO;
import com.themuffinman.app.business.dto.BusinessReviewResponseDTO;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.model.BusinessReview;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.business.repository.BusinessReviewRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BusinessReviewService {

    private static final int MAX_COMMENT_LENGTH = 2000;
    private static final int MAX_PUBLIC_PAGE_SIZE = 50;

    private final BusinessBookingRepository businessBookingRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessReviewRepository businessReviewRepository;

    @Transactional
    public BusinessReviewResponseDTO createOrUpdateReview(
            Long bookingId,
            BusinessReviewRequestDTO request,
            AppUser currentUser
    ) {
        validateRequest(request);
        if (currentUser == null) {
            throw ServiceErrors.unauthorized("Authentication is required");
        }

        BusinessBooking booking = businessBookingRepository.findDetailedById(bookingId)
                .orElseThrow(() -> ServiceErrors.notFound("Booking not found"));
        if (!booking.getCustomerUser().getId().equals(currentUser.getId())) {
            throw ServiceErrors.forbidden("Only the booking customer can review this business");
        }
        if (booking.getStatus() != BusinessBookingStatus.COMPLETED) {
            throw ServiceErrors.badRequest("Reviews are only allowed after a booking is completed");
        }
        if (booking.getBusinessProfile().getOwner().getId().equals(currentUser.getId())) {
            throw ServiceErrors.badRequest("You cannot review your own business");
        }

        BusinessReview review = businessReviewRepository
                .findByBusinessBookingIdAndReviewerId(bookingId, currentUser.getId())
                .orElseGet(BusinessReview::new);
        if (review.getId() == null) {
            review.setBusinessBooking(booking);
            review.setBusinessProfile(booking.getBusinessProfile());
            review.setReviewer(currentUser);
            review.setCreatedAt(Instant.now());
        }

        review.setStars(request.getStars().shortValue());
        review.setComment(normalizeComment(request.getComment()));
        review.setUpdatedAt(Instant.now());
        return toResponse(businessReviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public BusinessReviewListResponseDTO getPublicReviews(String slug, int page, int size) {
        BusinessProfile profile = requireActiveProfile(slug);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PUBLIC_PAGE_SIZE);
        return BusinessReviewListResponseDTO.builder()
                .items(businessReviewRepository.findPublicByBusinessProfileId(profile.getId(), PageRequest.of(safePage, safeSize))
                        .stream()
                        .map(this::toResponse)
                        .toList())
                .page(safePage)
                .size(safeSize)
                .build();
    }

    @Transactional(readOnly = true)
    public BusinessRatingSummaryDTO getPublicRatingSummary(Long businessProfileId) {
        BusinessReviewRepository.BusinessRatingSummaryProjection summary = businessReviewRepository
                .summarizeByBusinessProfileId(businessProfileId);
        if (summary == null || summary.getReviewCount() == 0) {
            return BusinessRatingSummaryDTO.builder().averageStars(0.0).reviewCount(0).build();
        }
        return BusinessRatingSummaryDTO.builder()
                .averageStars(Math.round(summary.getAverageStars() * 10.0) / 10.0)
                .reviewCount(summary.getReviewCount())
                .build();
    }

    private BusinessProfile requireActiveProfile(String slug) {
        return businessProfileRepository.findBySlug(slug)
                .filter(BusinessProfile::isActive)
                .orElseThrow(() -> ServiceErrors.notFound("Business profile not found"));
    }

    private void validateRequest(BusinessReviewRequestDTO request) {
        if (request == null || request.getStars() == null || request.getStars() < 1 || request.getStars() > 5) {
            throw ServiceErrors.badRequest("Stars must be between 1 and 5");
        }
        String normalizedComment = normalizeComment(request.getComment());
        if (normalizedComment != null && normalizedComment.length() > MAX_COMMENT_LENGTH) {
            throw ServiceErrors.badRequest("Comment must be at most 2000 characters");
        }
    }

    private String normalizeComment(String comment) {
        String normalized = RichTextInputValidator.extractPlainText(comment);
        return normalized.isBlank() ? null : normalized;
    }

    private BusinessReviewResponseDTO toResponse(BusinessReview review) {
        BusinessBooking booking = review.getBusinessBooking();
        return BusinessReviewResponseDTO.builder()
                .id(review.getId())
                .bookingId(booking.getId())
                .reviewerUsername(review.getReviewer().getUsername())
                .stars(review.getStars())
                .comment(review.getComment())
                .serviceTitle(booking.getOfferingTitleSnapshot())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
