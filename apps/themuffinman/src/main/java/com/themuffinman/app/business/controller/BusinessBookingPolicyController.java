package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessBookingPolicyRequestDTO;
import com.themuffinman.app.business.dto.BusinessBookingPolicyResponseDTO;
import com.themuffinman.app.business.service.BusinessBookingPolicyService;
import com.themuffinman.app.identity.model.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/booking-policy")
@RequiredArgsConstructor
public class BusinessBookingPolicyController {

    private final BusinessBookingPolicyService businessBookingPolicyService;

    @GetMapping("/me")
    public BusinessBookingPolicyResponseDTO getMyPolicy(@AuthenticationPrincipal AppUser currentUser) {
        return businessBookingPolicyService.getMyPolicy(currentUser);
    }

    @PutMapping("/me")
    public BusinessBookingPolicyResponseDTO saveMyPolicy(
            @Valid @RequestBody BusinessBookingPolicyRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessBookingPolicyService.saveMyPolicy(dto, currentUser);
    }
}
