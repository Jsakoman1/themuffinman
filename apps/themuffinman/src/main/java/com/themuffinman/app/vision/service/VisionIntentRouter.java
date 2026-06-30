package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.model.VisionIntent;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class VisionIntentRouter {

    private final VisionProperties visionProperties;

    public VisionIntentRouter(VisionProperties visionProperties) {
        this.visionProperties = visionProperties;
    }

    public VisionIntent detectIntent(String prompt) {
        if (!visionProperties.isCreateQuestEnabled()) {
            return VisionIntent.UNSUPPORTED;
        }
        String lower = prompt.toLowerCase(Locale.ROOT);
        if (lower.contains("create") && lower.contains("quest")) {
            return VisionIntent.CREATE_QUEST;
        }
        if (lower.contains("post") && lower.contains("quest")) {
            return VisionIntent.CREATE_QUEST;
        }
        if (containsAny(lower,
                "create quest",
                "new quest",
                "post a quest",
                "post quest",
                "need someone",
                "looking for someone",
                "i need help",
                "can someone",
                "task for someone",
                "help me with")) {
            return VisionIntent.CREATE_QUEST;
        }
        return VisionIntent.UNSUPPORTED;
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
