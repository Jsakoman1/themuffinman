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
    private Memory memory = new Memory();

    @Getter
    @Setter
    public static class Memory {
        private String compactionCron = "0 20 4 * * *";
        private int recentFeedbackWindow = 20;
        private int summaryWindow = 5;
    }
}
