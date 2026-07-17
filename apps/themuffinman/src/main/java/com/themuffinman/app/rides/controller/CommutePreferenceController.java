package com.themuffinman.app.rides.controller;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.rides.dto.CommutePreferenceRequestDTO;
import com.themuffinman.app.rides.dto.CommutePreferenceResponseDTO;
import com.themuffinman.app.rides.service.CommutePreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rides/commute")
public class CommutePreferenceController {
    private final CommutePreferenceService service;

    @GetMapping("/me")
    public CommutePreferenceResponseDTO getMine(@AuthenticationPrincipal AppUser user) { return service.getMine(user); }

    @PutMapping("/me")
    public CommutePreferenceResponseDTO updateMine(@RequestBody CommutePreferenceRequestDTO request, @AuthenticationPrincipal AppUser user) { return service.updateMine(request, user); }
}
