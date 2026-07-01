package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.dto.DashboardVoiceTranscriptionDTO;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.identity.model.AppUser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DashboardVoiceService {

    private final OpenAiVoiceClient openAiVoiceClient;
    private final VoiceProperties voiceProperties;

    public DashboardVoiceService(OpenAiVoiceClient openAiVoiceClient, VoiceProperties voiceProperties) {
        this.openAiVoiceClient = openAiVoiceClient;
        this.voiceProperties = voiceProperties;
    }

    public DashboardVoiceTranscriptionDTO transcribe(MultipartFile audioFile, AppUser currentUser) {
        validateVoiceAccess(currentUser);
        validateAudioFile(audioFile);
        OpenAiVoiceClient.DashboardVoiceTranscriptionResult result = openAiVoiceClient.transcribe(audioFile);
        return DashboardVoiceTranscriptionDTO.builder()
                .text(result.text())
                .provider(result.provider())
                .model(result.model())
                .build();
    }

    public ResponseEntity<byte[]> synthesize(String text, AppUser currentUser) {
        validateVoiceAccess(currentUser);
        validateSpeechText(text);
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

    private void validateAudioFile(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw ServiceErrors.badRequest("Audio recording is required");
        }

        long maxAudioBytes = voiceProperties.getMaxAudioBytes();
        if (maxAudioBytes > 0 && audioFile.getSize() > maxAudioBytes) {
            throw ServiceErrors.badRequest("Audio recording is too large");
        }
    }

    private void validateSpeechText(String text) {
        int maxSpeechTextLength = voiceProperties.getMaxSpeechTextLength();
        if (maxSpeechTextLength > 0 && text != null && text.length() > maxSpeechTextLength) {
            throw ServiceErrors.badRequest("Speech text is too long");
        }
    }
}
