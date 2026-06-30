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
    private String recognitionProvider = "browser";
    private String synthesisProvider = "browser";
    private String preferredLocale = "";
    private boolean interimResults = true;
    private boolean continuousRecognition = false;
    private int maxAlternatives = 1;
    private boolean autoSpeakResponses = false;
}
