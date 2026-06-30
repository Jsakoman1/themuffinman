package com.themuffinman.app.vision.controller;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionConversationTurnRequestDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.service.VisionConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vision/conversations")
@RequiredArgsConstructor
public class VisionConversationController {

    private final VisionConversationService visionConversationService;

    @PostMapping("/turns")
    public VisionConversationTurnResponseDTO processTurn(
            @Valid @RequestBody VisionConversationTurnRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return visionConversationService.processTurn(dto, currentUser);
    }
}
