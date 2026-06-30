package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.DashboardVoiceTranscriptionDTO;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DashboardVoiceService {

    private final OpenAiVoiceClient openAiVoiceClient;

    public DashboardVoiceService(OpenAiVoiceClient openAiVoiceClient) {
        this.openAiVoiceClient = openAiVoiceClient;
    }

    public DashboardVoiceTranscriptionDTO transcribe(MultipartFile audioFile, AppUser currentUser) {
        validateVoiceAccess(currentUser);
        OpenAiVoiceClient.DashboardVoiceTranscriptionResult result = openAiVoiceClient.transcribe(audioFile);
        return DashboardVoiceTranscriptionDTO.builder()
                .text(result.text())
                .provider(result.provider())
                .model(result.model())
                .build();
    }

    public ResponseEntity<byte[]> synthesize(String text, AppUser currentUser) {
        validateVoiceAccess(currentUser);
        byte[] audio = openAiVoiceClient.synthesize(text);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"speech.mp3\"")
                .body(audio);
    }

    private void validateVoiceAccess(AppUser currentUser) {
        if (currentUser == null) {
            throw ServiceErrors.forbidden("Authentication is required");
        }
    }
}
