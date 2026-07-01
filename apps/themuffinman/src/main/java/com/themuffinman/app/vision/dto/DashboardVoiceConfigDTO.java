package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVoiceConfigDTO {
    private boolean enabled;
    private boolean speechToTextEnabled;
    private boolean textToSpeechEnabled;
    private String recognitionProvider;
    private String synthesisProvider;
    private String preferredLocale;
    private boolean interimResults;
    private boolean continuousRecognition;
    private int maxAlternatives;
    private boolean autoSpeakResponses;
    private long maxRecordingMillis;
    private long maxAudioBytes;
    private int maxSpeechTextLength;
}
