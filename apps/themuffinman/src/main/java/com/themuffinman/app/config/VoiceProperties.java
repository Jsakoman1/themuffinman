package com.themuffinman.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.voice")
public class VoiceProperties {
    private boolean enabled = true;
    private boolean speechToTextEnabled = true;
    private boolean textToSpeechEnabled = true;
    private String provider = "openai";
    private String recognitionProvider = "openai";
    private String synthesisProvider = "openai";
    private String transcriptionModel = "gpt-4o-mini-transcribe";
    private String speechModel = "gpt-4o-mini-tts";
    private String speechVoice = "alloy";
    private String preferredLocale = "";
    private boolean interimResults = true;
    private boolean continuousRecognition = false;
    private int maxAlternatives = 1;
    private boolean autoSpeakResponses = false;
    private String apiKey;
    private String baseUrl = "https://api.openai.com/v1";

    public boolean isConfigured() {
        return "openai".equalsIgnoreCase(provider)
                && apiKey != null
                && !apiKey.isBlank();
    }
}
