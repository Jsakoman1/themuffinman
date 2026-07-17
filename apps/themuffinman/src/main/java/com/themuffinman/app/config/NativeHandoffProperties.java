package com.themuffinman.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.native-handoff")
public class NativeHandoffProperties {
    private long ttlSeconds = 300;
}
