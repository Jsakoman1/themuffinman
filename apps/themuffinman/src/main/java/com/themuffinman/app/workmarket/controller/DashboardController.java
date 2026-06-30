package com.themuffinman.app.workmarket.controller;

import com.themuffinman.app.workmarket.dto.DashboardResponseDTO;
import com.themuffinman.app.workmarket.dto.DashboardSummaryDTO;
import com.themuffinman.app.workmarket.dto.DashboardVisionPromptRequestDTO;
import com.themuffinman.app.workmarket.dto.DashboardVisionPromptResponseDTO;
import com.themuffinman.app.workmarket.dto.DashboardVoiceConfigDTO;
import com.themuffinman.app.workmarket.dto.DashboardVoiceSpeechRequestDTO;
import com.themuffinman.app.workmarket.dto.DashboardVoiceTranscriptionDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.service.DashboardService;
import com.themuffinman.app.workmarket.service.DashboardVisionPromptService;
import com.themuffinman.app.workmarket.service.DashboardVoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardVisionPromptService dashboardVisionPromptService;
    private final DashboardVoiceService dashboardVoiceService;

    @GetMapping("/me")
    public DashboardResponseDTO getMyDashboard(@AuthenticationPrincipal AppUser currentUser) {
        return dashboardService.getMyDashboard(currentUser);
    }

    @GetMapping("/me/summary")
    public DashboardSummaryDTO getMySummary(@AuthenticationPrincipal AppUser currentUser) {
        return dashboardService.getMySummary(currentUser);
    }

    @GetMapping("/me/voice-config")
    public DashboardVoiceConfigDTO getMyVoiceConfig(@AuthenticationPrincipal AppUser currentUser) {
        return dashboardService.getMyVoiceConfig(currentUser);
    }

    @PostMapping("/me/vision/prompt")
    public DashboardVisionPromptResponseDTO processVisionPrompt(
            @Valid @RequestBody DashboardVisionPromptRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return dashboardVisionPromptService.process(dto, currentUser);
    }

    @PostMapping("/me/voice/transcribe")
    public DashboardVoiceTranscriptionDTO transcribeVoice(
            @RequestPart("audio") MultipartFile audio,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return dashboardVoiceService.transcribe(audio, currentUser);
    }

    @PostMapping("/me/voice/speak")
    public ResponseEntity<byte[]> speakVoice(
            @Valid @RequestBody DashboardVoiceSpeechRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return dashboardVoiceService.synthesize(dto.getText(), currentUser);
    }
}
