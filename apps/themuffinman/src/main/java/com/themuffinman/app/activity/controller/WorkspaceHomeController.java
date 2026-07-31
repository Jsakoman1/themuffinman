package com.themuffinman.app.activity.controller;

import com.themuffinman.app.activity.dto.WorkspaceHomeResponseDTO;
import com.themuffinman.app.activity.service.WorkspaceHomeReadService;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspace/home")
public class WorkspaceHomeController {
    private final WorkspaceHomeReadService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public WorkspaceHomeResponseDTO getHome(@AuthenticationPrincipal AppUser user) {
        return service.getHome(user);
    }
}
