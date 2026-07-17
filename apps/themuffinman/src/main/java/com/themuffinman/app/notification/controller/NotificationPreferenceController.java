package com.themuffinman.app.notification.controller;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.notification.dto.NotificationPreferenceResponseDTO;
import com.themuffinman.app.notification.dto.NotificationPreferenceUpdateDTO;
import com.themuffinman.app.notification.service.NotificationPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification-preferences/me")
@RequiredArgsConstructor
public class NotificationPreferenceController {
    private final NotificationPreferenceService service;

    @GetMapping
    public NotificationPreferenceResponseDTO get(@AuthenticationPrincipal AppUser currentUser) {
        return service.getForUser(currentUser);
    }

    @PutMapping
    public NotificationPreferenceResponseDTO update(
            @Valid @RequestBody List<@Valid NotificationPreferenceUpdateDTO> updates,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return service.update(currentUser, updates);
    }
}
