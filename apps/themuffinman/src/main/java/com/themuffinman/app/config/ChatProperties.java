package com.themuffinman.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.chat")
public class ChatProperties {

    private Presence presence = new Presence();
    private Messages messages = new Messages();
    private Attachments attachments = new Attachments();
    private Typing typing = new Typing();
    private Moderation moderation = new Moderation();
    private Support support = new Support();

    @Getter
    @Setter
    public static class Presence {
        private long onlineWindowSeconds = 120;
        private int heartbeatLimitPerMinute = 120;
    }

    @Getter
    @Setter
    public static class Messages {
        private int defaultPageSize = 50;
        private int maxPageSize = 100;
        private int openLimitPerMinute = 30;
        private int sendLimitPerMinute = 40;
        private int deliveryLimitPerMinute = 240;
        private int readLimitPerMinute = 120;
        private long editWindowSeconds = 900;
        private long deleteWindowSeconds = 3_600;
        private String deletedMessagePlaceholder = "Message deleted";
    }

    @Getter
    @Setter
    public static class Attachments {
        private int maxImageBytes = 262_144;
        private List<String> allowedImageMimeTypes = List.of("image/jpeg", "image/png", "image/webp", "image/gif");
        private int maxAttachmentBytes = 524_288;
        private List<String> allowedAttachmentMimeTypes = List.of(
                "application/pdf",
                "text/plain",
                "application/json",
                "image/jpeg",
                "image/png",
                "image/webp"
        );
    }

    @Getter
    @Setter
    public static class Typing {
        private int updateLimitPerMinute = 180;
        private int timeoutSeconds = 8;
    }

    @Getter
    @Setter
    public static class Moderation {
        private int auditListMaxLimit = 200;
        private int reactionLimitPerMinute = 120;
        private int messageRemovalLimitPerMinute = 60;
    }

    @Getter
    @Setter
    public static class Support {
        private int recentMessagesLimit = 25;
        private int recentAuditLimit = 25;
    }
}
