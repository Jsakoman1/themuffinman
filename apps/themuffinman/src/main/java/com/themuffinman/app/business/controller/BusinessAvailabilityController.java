package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessAvailabilityExceptionListResponseDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityExceptionRequestDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityExceptionResponseDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityRuleListResponseDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityRuleRequestDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityRuleResponseDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityWindowListResponseDTO;
import com.themuffinman.app.business.service.BusinessAvailabilityReadService;
import com.themuffinman.app.business.service.BusinessAvailabilityService;
import com.themuffinman.app.identity.model.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/business")
public class BusinessAvailabilityController {

    private final BusinessAvailabilityService businessAvailabilityService;
    private final BusinessAvailabilityReadService businessAvailabilityReadService;

    @GetMapping("/availability-rules/me")
    public BusinessAvailabilityRuleListResponseDTO getMyRules(@AuthenticationPrincipal AppUser currentUser) {
        return businessAvailabilityService.getMyRules(currentUser);
    }

    @PostMapping("/availability-rules/me")
    public BusinessAvailabilityRuleResponseDTO createMyRule(
            @Valid @RequestBody BusinessAvailabilityRuleRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessAvailabilityService.createMyRule(dto, currentUser);
    }

    @PutMapping("/availability-rules/{ruleId}/me")
    public BusinessAvailabilityRuleResponseDTO updateMyRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody BusinessAvailabilityRuleRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessAvailabilityService.updateMyRule(ruleId, dto, currentUser);
    }

    @DeleteMapping("/availability-rules/{ruleId}/me")
    public void deleteMyRule(@PathVariable Long ruleId, @AuthenticationPrincipal AppUser currentUser) {
        businessAvailabilityService.deleteMyRule(ruleId, currentUser);
    }

    @GetMapping("/availability-exceptions/me")
    public BusinessAvailabilityExceptionListResponseDTO getMyExceptions(@AuthenticationPrincipal AppUser currentUser) {
        return businessAvailabilityService.getMyExceptions(currentUser);
    }

    @PostMapping("/availability-exceptions/me")
    public BusinessAvailabilityExceptionResponseDTO createMyException(
            @Valid @RequestBody BusinessAvailabilityExceptionRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessAvailabilityService.createMyException(dto, currentUser);
    }

    @PutMapping("/availability-exceptions/{exceptionId}/me")
    public BusinessAvailabilityExceptionResponseDTO updateMyException(
            @PathVariable Long exceptionId,
            @Valid @RequestBody BusinessAvailabilityExceptionRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessAvailabilityService.updateMyException(exceptionId, dto, currentUser);
    }

    @DeleteMapping("/availability-exceptions/{exceptionId}/me")
    public void deleteMyException(@PathVariable Long exceptionId, @AuthenticationPrincipal AppUser currentUser) {
        businessAvailabilityService.deleteMyException(exceptionId, currentUser);
    }

    @GetMapping("/public/{slug}/availability")
    public BusinessAvailabilityWindowListResponseDTO getPublicAvailability(
            @PathVariable String slug,
            @RequestParam(required = false) Long offeringId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return businessAvailabilityReadService.getPublicAvailability(slug, offeringId, from, to);
    }
}
