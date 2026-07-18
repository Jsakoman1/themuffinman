package com.themuffinman.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.account-recovery")
public class AccountRecoveryProperties {
    private int requestsPerWindow = 5;
    private long windowSeconds = 900;
}
