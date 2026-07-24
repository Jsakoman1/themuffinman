package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessWorkspaceContextDTO;
import com.themuffinman.app.business.service.BusinessWorkspaceContextService;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/business/workspace")
public class BusinessWorkspaceContextController {
    private final BusinessWorkspaceContextService service;

    @GetMapping("/me")
    public BusinessWorkspaceContextDTO get(
            @RequestParam(required = false) Long profileId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @AuthenticationPrincipal AppUser viewer
    ) {
        return service.getContext(viewer, profileId, from, to);
    }
}
