package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessOfferingListResponseDTO;
import com.themuffinman.app.business.dto.BusinessBookingPreviewRequestDTO;
import com.themuffinman.app.business.dto.BusinessBookingPreviewResponseDTO;
import com.themuffinman.app.business.dto.BusinessPublicPageDTO;
import com.themuffinman.app.business.dto.BusinessQuoteRequestDTO;
import com.themuffinman.app.business.dto.BusinessQuoteResponseDTO;
import com.themuffinman.app.business.dto.BusinessServiceSchemaDTO;
import com.themuffinman.app.business.service.BusinessPublicReadService;
import com.themuffinman.app.business.service.BusinessBookingPreviewService;
import com.themuffinman.app.business.service.BusinessOfferingSchemaService;
import com.themuffinman.app.business.service.BusinessQuoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/public")
@RequiredArgsConstructor
public class BusinessPublicController {

    private final BusinessPublicReadService businessPublicReadService;
    private final BusinessBookingPreviewService businessBookingPreviewService;
    private final BusinessOfferingSchemaService businessOfferingSchemaService;
    private final BusinessQuoteService businessQuoteService;

    @GetMapping("/{slug}")
    public BusinessPublicPageDTO getPublicBusinessPage(@PathVariable String slug) {
        return businessPublicReadService.getPublicBusinessPage(slug);
    }

    @GetMapping("/{slug}/offerings")
    public BusinessOfferingListResponseDTO getPublicOfferings(@PathVariable String slug) {
        return businessPublicReadService.getPublicOfferings(slug);
    }

    @GetMapping("/{slug}/offerings/{offeringId}/schema")
    public BusinessServiceSchemaDTO getPublicSchema(@PathVariable String slug, @PathVariable Long offeringId) {
        return businessOfferingSchemaService.getPublicSchema(slug, offeringId);
    }

    @PostMapping("/{slug}/quote")
    public BusinessQuoteResponseDTO quote(@PathVariable String slug, @Valid @RequestBody BusinessQuoteRequestDTO request) {
        return businessQuoteService.quote(slug, request);
    }

    @PostMapping("/{slug}/booking-preview")
    public BusinessBookingPreviewResponseDTO previewBooking(
            @PathVariable String slug,
            @Valid @RequestBody BusinessBookingPreviewRequestDTO request
    ) {
        return businessBookingPreviewService.preview(slug, request);
    }
}
