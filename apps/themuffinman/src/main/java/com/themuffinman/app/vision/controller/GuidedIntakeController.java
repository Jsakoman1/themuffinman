package com.themuffinman.app.vision.controller;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.GuidedIntakeRequestDTO;
import com.themuffinman.app.vision.dto.GuidedIntakeResponseDTO;
import com.themuffinman.app.vision.service.GuidedIntakeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor @RequestMapping("/guided-intake")
public class GuidedIntakeController {
    private final GuidedIntakeService service;

    @PostMapping("/step")
    public GuidedIntakeResponseDTO advance(@Valid @RequestBody GuidedIntakeRequestDTO request, @AuthenticationPrincipal AppUser user) {
        return service.advance(request);
    }
}
