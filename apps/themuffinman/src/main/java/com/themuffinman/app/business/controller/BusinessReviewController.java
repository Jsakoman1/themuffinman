package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessReviewListResponseDTO;
import com.themuffinman.app.business.dto.BusinessReviewRequestDTO;
import com.themuffinman.app.business.dto.BusinessReviewResponseDTO;
import com.themuffinman.app.business.service.BusinessReviewService;
import com.themuffinman.app.identity.model.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
public class BusinessReviewController {

    private final BusinessReviewService businessReviewService;

    @GetMapping("/public/{slug}/reviews")
    public BusinessReviewListResponseDTO getPublicReviews(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return businessReviewService.getPublicReviews(slug, page, size);
    }

    @PostMapping("/bookings/me/{bookingId}/review")
    public BusinessReviewResponseDTO createOrUpdateReview(
            @PathVariable Long bookingId,
            @Valid @RequestBody BusinessReviewRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessReviewService.createOrUpdateReview(bookingId, request, currentUser);
    }
}
