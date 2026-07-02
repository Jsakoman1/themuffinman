package com.themuffinman.app.vision.service;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class VisionSemanticOrchestrationRequest {

    private String contractVersion;
    private String rawPrompt;
    private VisionSemanticUserContext userContext;
    private VisionSemanticConversationContext conversationContext;
    private VisionSemanticRuntimeContext runtimeContext;
    private List<VisionSemanticRouteDescriptor> allowedRoutes;
    private Map<String, Object> responseContract;
}

@Getter
@Builder
class VisionSemanticUserContext {
    private Long userId;
    private String username;
    private String role;
    private String preferredLocale;
    private String preferredLocaleSource;
    private String preferredLanguage;
    private String timezone;
    private String timezoneSource;
    private String countryCode;
    private String country;
    private String locality;
    private String locationLabel;
}

@Getter
@Builder
class VisionSemanticConversationContext {
    private Long conversationId;
    private String currentIntent;
    private String requestedSlot;
    private Map<String, String> slotData;
}

@Getter
@Builder
class VisionSemanticRuntimeContext {
    private String inputType;
    private String clientLocale;
    private String clientTimezone;
    private List<String> clientCapabilities;
    private String clientStateVersion;
}

@Getter
@Builder
class VisionSemanticRouteDescriptor {
    private String routeKey;
    private String entityType;
    private String intent;
    private String capabilityId;
    private String purpose;
    private boolean mutating;
    private boolean requiresReview;
    private List<VisionSemanticSlotDescriptor> slots;
}

@Getter
@Builder
class VisionSemanticSlotDescriptor {
    private String slotId;
    private String fieldName;
    private String kind;
    private boolean required;
    private String description;
    private List<String> allowedValues;
}
