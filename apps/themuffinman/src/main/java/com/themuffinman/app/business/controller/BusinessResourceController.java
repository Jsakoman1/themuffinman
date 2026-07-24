package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.service.BusinessResourceService;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/business/resources")
public class BusinessResourceController {
    private final BusinessResourceService resourceService;

    @GetMapping("/profile/{profileId}/me")
    public Map<String, Object> get(@PathVariable Long profileId, @AuthenticationPrincipal AppUser owner) {
        return resourceService.getOwnerResources(profileId, owner);
    }

    @PostMapping("/profile/{profileId}/pools/me")
    public Map<String, Object> createPool(@PathVariable Long profileId, @RequestBody Map<String, Object> request, @AuthenticationPrincipal AppUser owner) {
        return resourceService.createPool(profileId, request, owner);
    }

    @PostMapping("/profile/{profileId}/resources/me")
    public Map<String, Object> createResource(@PathVariable Long profileId, @RequestBody Map<String, Object> request, @AuthenticationPrincipal AppUser owner) {
        return resourceService.createResource(profileId, request, owner);
    }

    @PostMapping("/profile/{profileId}/requirements/me")
    public Map<String, Object> createRequirement(@PathVariable Long profileId, @RequestBody Map<String, Object> request, @AuthenticationPrincipal AppUser owner) {
        return resourceService.createRequirement(profileId, request, owner);
    }
}
