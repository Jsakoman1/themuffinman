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
    private String model = "gpt-5.5";
    private String apiKey;
    private String baseUrl = "https://api.openai.com/v1";
}
