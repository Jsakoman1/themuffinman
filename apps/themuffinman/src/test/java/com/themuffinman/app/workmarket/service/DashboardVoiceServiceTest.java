package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.DashboardVoiceTranscriptionDTO;
import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardVoiceServiceTest {

    @Mock
    private OpenAiVoiceClient openAiVoiceClient;

    @Test
    void transcribeReturnsBackendTextAndMetadata() {
        MockMultipartFile audio = new MockMultipartFile("audio", "voice.webm", "audio/webm", new byte[] {1, 2, 3});
        when(openAiVoiceClient.transcribe(audio)).thenReturn(new OpenAiVoiceClient.DashboardVoiceTranscriptionResult(
                "hello from openai",
                "openai",
                "gpt-4o-mini-transcribe"
        ));

        DashboardVoiceService service = new DashboardVoiceService(openAiVoiceClient, voiceProperties());
        DashboardVoiceTranscriptionDTO response = service.transcribe(audio, admin());

        assertEquals("hello from openai", response.getText());
        assertEquals("openai", response.getProvider());
        assertEquals("gpt-4o-mini-transcribe", response.getModel());
    }

    @Test
    void synthesizeReturnsAudioPayloadForAuthenticatedUser() {
        when(openAiVoiceClient.synthesize("hello")).thenReturn(new byte[] {4, 5, 6});

        DashboardVoiceService service = new DashboardVoiceService(openAiVoiceClient, voiceProperties());
        var response = service.synthesize("hello", admin());

        assertEquals(MediaType.valueOf("audio/mpeg"), response.getHeaders().getContentType());
        assertArrayEquals(new byte[] {4, 5, 6}, response.getBody());
    }

    @Test
    void voiceAccessIsRejectedForAnonymousUsers() {
        DashboardVoiceService service = new DashboardVoiceService(openAiVoiceClient, voiceProperties());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.transcribe(new MockMultipartFile("audio", "voice.webm", "audio/webm", new byte[] {1}), null));

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void oversizedAudioIsRejectedBeforeOpenAiCall() {
        VoiceProperties voiceProperties = voiceProperties();
        voiceProperties.setMaxAudioBytes(2);
        DashboardVoiceService service = new DashboardVoiceService(openAiVoiceClient, voiceProperties);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.transcribe(new MockMultipartFile("audio", "voice.webm", "audio/webm", new byte[] {1, 2, 3}), admin()));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void oversizedSpeechTextIsRejectedBeforeOpenAiCall() {
        VoiceProperties voiceProperties = voiceProperties();
        voiceProperties.setMaxSpeechTextLength(3);
        DashboardVoiceService service = new DashboardVoiceService(openAiVoiceClient, voiceProperties);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.synthesize("hello", admin()));

        assertEquals(400, exception.getStatusCode().value());
    }

    private AppUser admin() {
        AppUser admin = new AppUser();
        admin.setId(11L);
        return admin;
    }

    private VoiceProperties voiceProperties() {
        return new VoiceProperties();
    }
}
