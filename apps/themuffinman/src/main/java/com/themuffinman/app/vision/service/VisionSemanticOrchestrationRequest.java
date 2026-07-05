package com.themuffinman.app.vision.service;

import lombok.Builder;
import lombok.Getter;

import com.themuffinman.app.vision.dto.VisionLearningPreferenceDTO;
import com.themuffinman.app.vision.dto.VisionLearningExplainabilityDTO;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class VisionSemanticOrchestrationRequest {

    private String contractVersion;
    private String rawPrompt;
    private VisionSemanticUserContext userContext;
    private VisionSemanticMemoryContext memoryContext;
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
class VisionSemanticMemoryContext {
    private VisionSemanticUserMemoryContext userMemory;
    private VisionSemanticSessionMemoryContext sessionMemory;
    private List<VisionSemanticConversationMemoryItem> recentConversations;
}

@Getter
@Builder
class VisionSemanticUserMemoryContext {
    private Long userId;
    private String username;
    private String role;
    private String preferredLocale;
    private String timezone;
    private String countryCode;
    private String country;
    private String locality;
    private String locationLabel;
    private String preferredInputType;
    private Double preferredInputTypeConfidence;
    private String preferredEntityFamily;
    private Double preferredEntityFamilyConfidence;
    private String learningSummary;
    private String retrievalSummary;
    private List<String> recentFeedbackTypes;
    private List<String> recentIntentTypes;
    private List<String> recentEntityFamilies;
    private List<VisionLearningPreferenceDTO> learnedPreferences;
    private List<VisionLearningExplainabilityDTO> explainabilityRecords;
    private String retrievedEntityFamily;
    private Double retrievedEntityFamilyConfidence;
}

@Getter
@Builder
class VisionSemanticSessionMemoryContext {
    private Long conversationId;
    private String currentIntent;
    private String currentEntityFamily;
    private String status;
    private String requestedSlot;
    private String sessionSummary;
    private String lastUserPrompt;
    private String lastNormalizedPrompt;
    private String lastAssistantMessage;
    private boolean translationReliable;
    private String sessionMemorySnapshot;
    private List<String> openQuestions;
    private List<String> recentActions;
    private Map<String, String> slotData;
    private List<VisionSemanticTurnMemoryItem> recentTurns;
}

@Getter
@Builder
class VisionSemanticConversationMemoryItem {
    private Long conversationId;
    private String intent;
    private String status;
    private String requestedSlot;
    private String title;
    private String subtitle;
    private String groupKey;
    private String stageLabel;
    private String progressLabel;
    private Instant updatedAt;
}

@Getter
@Builder
class VisionSemanticTurnMemoryItem {
    private int turnIndex;
    private String source;
    private String prompt;
    private String normalizedPrompt;
    private String detectedIntent;
    private String requestedSlot;
    private String assistantMessage;
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
    private String entityFamily;
    private String intent;
    private String capabilityId;
    private String dtoType;
    private String validatorKey;
    private String executorKey;
    private Double minimumConfidence;
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
