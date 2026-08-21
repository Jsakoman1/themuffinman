package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessResourceConfigurationDTO;
import com.themuffinman.app.business.dto.BusinessResourcePoolRequestDTO;
import com.themuffinman.app.business.dto.BusinessResourceRequestDTO;
import com.themuffinman.app.business.dto.BusinessResourceRequirementRequestDTO;
import com.themuffinman.app.business.service.BusinessResourceService;
import com.themuffinman.app.identity.model.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/business/resources")
public class BusinessResourceController {
    private final BusinessResourceService resourceService;

    @GetMapping("/profile/{profileId}/me")
    public BusinessResourceConfigurationDTO get(@PathVariable Long profileId, @AuthenticationPrincipal AppUser owner) {
        return resourceService.getOwnerResourceConfiguration(profileId, owner);
    }

    @PostMapping("/profile/{profileId}/pools/me")
    public BusinessResourceConfigurationDTO createPool(@PathVariable Long profileId, @Valid @RequestBody BusinessResourcePoolRequestDTO request, @AuthenticationPrincipal AppUser owner) {
        return resourceService.createPool(profileId, request, owner);
    }

    @PostMapping("/profile/{profileId}/resources/me")
    public BusinessResourceConfigurationDTO createResource(@PathVariable Long profileId, @Valid @RequestBody BusinessResourceRequestDTO request, @AuthenticationPrincipal AppUser owner) {
        return resourceService.createResource(profileId, request, owner);
    }

    @PostMapping("/profile/{profileId}/requirements/me")
    public BusinessResourceConfigurationDTO createRequirement(@PathVariable Long profileId, @Valid @RequestBody BusinessResourceRequirementRequestDTO request, @AuthenticationPrincipal AppUser owner) {
        return resourceService.createRequirement(profileId, request, owner);
    }

    @PutMapping("/profile/{profileId}/pools/{poolId}/me")
    public BusinessResourceConfigurationDTO updatePool(@PathVariable Long profileId, @PathVariable Long poolId, @Valid @RequestBody BusinessResourcePoolRequestDTO request, @AuthenticationPrincipal AppUser owner) {
        return resourceService.updatePool(profileId, poolId, request, owner);
    }

    @DeleteMapping("/profile/{profileId}/pools/{poolId}/me")
    public BusinessResourceConfigurationDTO deletePool(@PathVariable Long profileId, @PathVariable Long poolId, @AuthenticationPrincipal AppUser owner) {
        return resourceService.deletePool(profileId, poolId, owner);
    }

    @PutMapping("/profile/{profileId}/resources/{resourceId}/me")
    public BusinessResourceConfigurationDTO updateResource(@PathVariable Long profileId, @PathVariable Long resourceId, @Valid @RequestBody BusinessResourceRequestDTO request, @AuthenticationPrincipal AppUser owner) {
        return resourceService.updateResource(profileId, resourceId, request, owner);
    }

    @DeleteMapping("/profile/{profileId}/resources/{resourceId}/me")
    public BusinessResourceConfigurationDTO deleteResource(@PathVariable Long profileId, @PathVariable Long resourceId, @AuthenticationPrincipal AppUser owner) {
        return resourceService.deleteResource(profileId, resourceId, owner);
    }

    @PutMapping("/profile/{profileId}/requirements/{requirementId}/me")
    public BusinessResourceConfigurationDTO updateRequirement(@PathVariable Long profileId, @PathVariable Long requirementId, @Valid @RequestBody BusinessResourceRequirementRequestDTO request, @AuthenticationPrincipal AppUser owner) {
        return resourceService.updateRequirement(profileId, requirementId, request, owner);
    }

    @DeleteMapping("/profile/{profileId}/requirements/{requirementId}/me")
    public BusinessResourceConfigurationDTO deleteRequirement(@PathVariable Long profileId, @PathVariable Long requirementId, @AuthenticationPrincipal AppUser owner) {
        return resourceService.deleteRequirement(profileId, requirementId, owner);
    }
}
