package com.themuffinman.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.agent")
public class AgentProperties {
    private String provider = "mock";
    private int promptMaxLength = 4000;
    private String model = "gpt-4o-mini";
    private String semanticModel = "gpt-4o-mini";
    private boolean semanticModelUpgradeEnabled = false;
    private String semanticModelUpgrade = "gpt-5.4-mini";
    private String creativeModel = "gpt-4o-mini";
    private String reasoningEffort = "medium";
    private String apiKey;
    private String baseUrl = "https://api.openai.com/v1";
    /** Development/test fixture switch; production application.properties disables it explicitly. */
    private boolean localEmergencyEnabled = true;
    private boolean adminExecutionEnabled = false;
    private int adminQuestBatchLimit = 10;
    private String syntheticQuestMarker = "[SYNTHETIC]";
}
