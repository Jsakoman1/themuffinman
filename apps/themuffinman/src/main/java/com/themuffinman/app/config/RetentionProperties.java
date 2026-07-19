package com.themuffinman.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.retention")
public class RetentionProperties {

    private Notifications notifications = new Notifications();
    private Chat chat = new Chat();
    private AuthTokens authTokens = new AuthTokens();

    @Getter
    @Setter
    public static class Notifications {
        private int days = 7;
        private String cleanupCron = "0 30 3 * * *";
    }

    @Getter
    @Setter
    public static class Chat {
        private int imageDays = 30;
        private int messageDays = 180;
        private String cleanupCron = "0 45 3 * * *";
        private String expiredImagePlaceholder = "Image expired";
    }

    @Getter
    @Setter
    public static class AuthTokens {
        private String cleanupCron = "0 15 3 * * *";
    }
}
