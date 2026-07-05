package com.themuffinman.app.vision.controller;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionConversationListResponseDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnRequestDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.service.VisionConversationLifecycleService;
import com.themuffinman.app.vision.service.VisionConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vision/conversations")
@RequiredArgsConstructor
public class VisionConversationController {

    private final VisionConversationService visionConversationService;
    private final VisionConversationLifecycleService visionConversationLifecycleService;

    @PostMapping("/turns")
    public VisionConversationTurnResponseDTO processTurn(
            @Valid @RequestBody VisionConversationTurnRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return visionConversationService.processTurn(dto, currentUser);
    }

    @PostMapping("/{conversationId}/reset")
    public VisionConversationTurnResponseDTO resetConversation(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return visionConversationLifecycleService.resetConversation(conversationId, currentUser);
    }

    @PostMapping("/{conversationId}/cancel")
    public VisionConversationTurnResponseDTO cancelConversation(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return visionConversationLifecycleService.cancelConversation(conversationId, currentUser);
    }

    @GetMapping("/recent")
    public VisionConversationListResponseDTO getRecentConversations(
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return visionConversationLifecycleService.listRecentConversations(currentUser);
    }

    @GetMapping("/{conversationId}")
    public VisionConversationTurnResponseDTO getConversation(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return visionConversationLifecycleService.loadConversation(conversationId, currentUser);
    }
}
