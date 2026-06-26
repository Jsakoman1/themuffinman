package com.themuffinman.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.location")
public class LocationProviderProperties {
    private String provider = "none";
    private String geoapifyApiKey;
    private String geoapifyBaseUrl = "https://api.geoapify.com/v1";
}
