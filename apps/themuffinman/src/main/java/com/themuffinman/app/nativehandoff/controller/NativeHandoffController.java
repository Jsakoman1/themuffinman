package com.themuffinman.app.nativehandoff.controller;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.nativehandoff.dto.NativeHandoffConsumeRequestDTO;
import com.themuffinman.app.nativehandoff.dto.NativeHandoffIssueRequestDTO;
import com.themuffinman.app.nativehandoff.dto.NativeHandoffIssueResponseDTO;
import com.themuffinman.app.nativehandoff.model.NativeHandoffToken;
import com.themuffinman.app.nativehandoff.service.NativeHandoffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/native/handoffs")
@RequiredArgsConstructor
public class NativeHandoffController {
    private final NativeHandoffService service;

    @PostMapping
    public NativeHandoffIssueResponseDTO issue(@Valid @RequestBody NativeHandoffIssueRequestDTO request, @AuthenticationPrincipal AppUser user) {
        return service.issue(request, user);
    }

    @PostMapping("/consume")
    public Map<String, Object> consume(@Valid @RequestBody NativeHandoffConsumeRequestDTO request, @AuthenticationPrincipal AppUser user) {
        NativeHandoffToken token = service.consume(request, user);
        return Map.of("contractVersion", "native-handoff-v1", "intent", token.getIntent(), "targetDevice", token.getTargetDevice(), "resourceReference", token.getResourceReference() == null ? "" : token.getResourceReference(), "redactedContext", token.getRedactedContext() == null ? "" : token.getRedactedContext());
    }
}
