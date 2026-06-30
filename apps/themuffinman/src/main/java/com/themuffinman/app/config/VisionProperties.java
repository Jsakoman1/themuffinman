package com.themuffinman.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.vision")
public class VisionProperties {
    private boolean enabled = true;
    private boolean executionEnabled = false;
    private boolean createQuestEnabled = true;
    private int conversationTtlDays = 14;
    private int maxTurnsPerConversation = 30;
    private int maxPromptLength = 2000;
}
