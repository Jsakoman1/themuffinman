package com.themuffinman.app.activity.controller;

import com.themuffinman.app.activity.dto.WorkspaceNavigationResponseDTO;
import com.themuffinman.app.activity.service.WorkspaceNavigationService;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspace/navigation")
public class WorkspaceNavigationController {
    private final WorkspaceNavigationService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public WorkspaceNavigationResponseDTO getNavigation(@AuthenticationPrincipal AppUser user) {
        return service.getNavigation(user);
    }
}
