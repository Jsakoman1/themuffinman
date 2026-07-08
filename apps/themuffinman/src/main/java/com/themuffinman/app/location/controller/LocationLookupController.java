package com.themuffinman.app.location.controller;

import com.themuffinman.app.location.dto.LocationLookupRequestDTO;
import com.themuffinman.app.location.dto.LocationLookupResponseDTO;
import com.themuffinman.app.location.dto.LocationLookupCandidateDTO;
import com.themuffinman.app.location.dto.LocationDebugStatusViewDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.location.dto.LocationReverseLookupRequestDTO;
import com.themuffinman.app.location.service.LocationLookupService;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import com.themuffinman.app.common.errors.ServiceErrors;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationLookupController {
    private final LocationLookupService locationLookupService;

    @PostMapping("/lookup")
    public LocationLookupResponseDTO lookup(@Valid @RequestBody LocationLookupRequestDTO dto, Authentication authentication) {
        return locationLookupService.lookup(dto.getQuery(), actorKey(authentication));
    }

    @PostMapping("/reverse-lookup")
    public LocationLookupCandidateDTO reverseLookup(@Valid @RequestBody LocationReverseLookupRequestDTO dto, Authentication authentication) {
        return locationLookupService.reverseLookup(dto.getLatitude(), dto.getLongitude(), actorKey(authentication));
    }

    @GetMapping("/admin/status")
    public LocationDebugStatusViewDTO getDebugStatus(@AuthenticationPrincipal AppUser currentUser) {
        validateAdmin(currentUser);
        return locationLookupService.getDebugStatus();
    }

    private String actorKey(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "anonymous";
        }

        return TextValueNormalizer.lowerTrimToEmpty(authentication.getName());
    }

    private void validateAdmin(AppUser currentUser) {
        if (currentUser == null || currentUser.getRole() != AppUserRole.ADMIN) {
            throw ServiceErrors.forbidden("Admin access is required");
        }
    }
}
