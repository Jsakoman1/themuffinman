package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionConversationTurnRequestDTO {
    private Long conversationId;
    private String prompt;
    private String text;
    private String source;
    private String inputType;
    @Builder.Default
    private List<String> clientCapabilities = List.of();
    private String clientStateVersion;
    private String clientLocale;
    private String clientTimezone;
    private String clientDeviceRole;
    private String clientRequestId;
    private String selectedOptionId;
    private String fieldValue;
    private Boolean confirmation;
    private String action;
    private String reviewTarget;

    public String getEffectivePrompt() {
        if (text != null && !text.isBlank()) {
            return text.trim();
        }
        if (prompt != null && !prompt.isBlank()) {
            return prompt.trim();
        }
        return "";
    }

    public String getEffectiveSource() {
        if (source != null && !source.isBlank()) {
            return source.trim();
        }
        if (inputType != null && !inputType.isBlank()) {
            return inputType.trim();
        }
        return "text";
    }
}
