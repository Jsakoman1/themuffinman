package com.themuffinman.app.vision.controller;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionSearchDiscoveryDTO;
import com.themuffinman.app.vision.service.VisionSearchDiscoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class UniversalSearchController {
    private final VisionSearchDiscoveryService service;

    @GetMapping
    public VisionSearchDiscoveryDTO search(@RequestParam(defaultValue = "") String q,
                                           @RequestParam(defaultValue = "0") int page,
                                           @AuthenticationPrincipal AppUser currentUser) {
        return service.discoverWeb(currentUser, q, page);
    }
}
