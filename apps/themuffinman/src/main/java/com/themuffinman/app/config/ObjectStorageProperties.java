package com.themuffinman.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.object-storage")
public class ObjectStorageProperties {

    private boolean enabled = false;
    private String provider = "s3";
    private String endpoint;
    private String region = "eu-central-1";
    private String bucket;
    private String accessKey;
    private String secretKey;
    private boolean pathStyleAccess = true;
    private String publicBaseUrl;
    private String keyPrefix = "chat";
    private long presignedUrlTtlSeconds = 900;
    private String localBasePath = "/tmp/themuffinman-object-storage";
    private String localProxyBaseUrl = "/chat/attachments/object";
}
