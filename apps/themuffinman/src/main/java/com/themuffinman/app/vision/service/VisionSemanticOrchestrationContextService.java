package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionLearningExplainabilityDTO;
import com.themuffinman.app.vision.dto.VisionLearningPreferenceDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.model.VisionTurnSource;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.vision.repository.VisionMemoryFeedbackEventRepository;
import com.themuffinman.app.vision.repository.VisionMemorySummaryRepository;
import com.themuffinman.app.vision.repository.VisionUserPreferenceRepository;
import com.themuffinman.app.vision.repository.VisionTurnRepository;
import com.themuffinman.app.vision.model.VisionMemorySummary;
import com.themuffinman.app.vision.model.VisionUserPreference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

@Service
public class VisionSemanticOrchestrationContextService {

    private static final List<String> CONTEXT_PREFERENCE_PRIORITY = List.of(
            "preferred_input_type",
            "preferred_language",
            "last_entity_family",
            "last_intent",
            "last_requested_slot",
            "last_feedback_type",
            "last_conversation_status"
    );

    private static final String DEFAULT_LOCALE = "en";
    private static final String DEFAULT_TIMEZONE = "UTC";
    private static final String DEFAULT_SOURCE = "default";
    private static final String CLIENT_HINT_SOURCE = "client_runtime_hint";
    private static final String VOICE_CONFIG_SOURCE = "voice_config_default";
    private static final String COUNTRY_DEFAULT_SOURCE = "country_default";
    private static final Map<String, String> COUNTRY_TIMEZONES = Map.ofEntries(
            Map.entry("CH", "Europe/Zurich"),
            Map.entry("HR", "Europe/Zagreb"),
            Map.entry("DE", "Europe/Berlin"),
            Map.entry("AT", "Europe/Vienna"),
            Map.entry("US", "America/New_York")
    );
    private static final Map<String, String> COUNTRY_LOCALES = Map.ofEntries(
            Map.entry("CH", "de-CH"),
            Map.entry("HR", "hr-HR"),
            Map.entry("DE", "de-DE"),
            Map.entry("AT", "de-AT"),
            Map.entry("US", "en-US")
    );

    private final VoiceProperties voiceProperties;
    private final VisionProperties visionProperties;
    private final VisionConversationRepository visionConversationRepository;
    private final VisionTurnRepository visionTurnRepository;
    private final VisionUserPreferenceRepository visionUserPreferenceRepository;
    private final VisionMemoryFeedbackEventRepository visionMemoryFeedbackEventRepository;
    private final VisionMemorySummaryRepository visionMemorySummaryRepository;

    public VisionSemanticOrchestrationContextService(VoiceProperties voiceProperties) {
        this(voiceProperties, null, null, null, null, null, null);
    }

    public VisionSemanticOrchestrationContextService(
            VoiceProperties voiceProperties,
            VisionConversationRepository visionConversationRepository,
            VisionTurnRepository visionTurnRepository
    ) {
        this(voiceProperties, null, visionConversationRepository, visionTurnRepository, null, null, null);
    }

    @Autowired
    public VisionSemanticOrchestrationContextService(
            VoiceProperties voiceProperties,
            VisionProperties visionProperties,
            VisionConversationRepository visionConversationRepository,
            VisionTurnRepository visionTurnRepository,
            VisionUserPreferenceRepository visionUserPreferenceRepository,
            VisionMemoryFeedbackEventRepository visionMemoryFeedbackEventRepository,
            VisionMemorySummaryRepository visionMemorySummaryRepository
    ) {
        this.voiceProperties = voiceProperties;
        this.visionProperties = visionProperties;
        this.visionConversationRepository = visionConversationRepository;
        this.visionTurnRepository = visionTurnRepository;
        this.visionUserPreferenceRepository = visionUserPreferenceRepository;
        this.visionMemoryFeedbackEventRepository = visionMemoryFeedbackEventRepository;
        this.visionMemorySummaryRepository = visionMemorySummaryRepository;
    }

    public VisionSemanticUserContext buildUserContext(AppUser user) {
        return buildUserContext(user, null);
    }

    public VisionSemanticUserContext buildUserContext(AppUser user, VisionSemanticRuntimeHints runtimeHints) {
        if (user == null) {
            ResolvedLocale resolvedLocale = resolveLocale(null, runtimeHints);
            ResolvedTimezone resolvedTimezone = resolveTimezone(null, runtimeHints);
            return VisionSemanticUserContext.builder()
                    .preferredLocale(resolvedLocale.value())
                    .preferredLocaleSource(resolvedLocale.source())
                    .preferredLanguage(languageForLocale(resolvedLocale.value()))
                    .timezone(resolvedTimezone.value())
                    .timezoneSource(resolvedTimezone.source())
                    .build();
        }

        String countryCode = normalizeCountryCode(user.getLocationCountryCode());
        ResolvedLocale resolvedLocale = resolveLocale(countryCode, runtimeHints);
        ResolvedTimezone resolvedTimezone = resolveTimezone(countryCode, runtimeHints);
        return VisionSemanticUserContext.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole() == null ? "" : user.getRole().name())
                .preferredLocale(resolvedLocale.value())
                .preferredLocaleSource(resolvedLocale.source())
                .preferredLanguage(languageForLocale(resolvedLocale.value()))
                .timezone(resolvedTimezone.value())
                .timezoneSource(resolvedTimezone.source())
                .countryCode(countryCode)
                .country(clean(user.getLocationCountry()))
                .locality(clean(user.getLocationLocality()))
                .locationLabel(clean(user.getLocationLabel()))
                .build();
    }

    public VisionSemanticConversationContext buildConversationContext(VisionConversation conversation) {
        if (conversation == null) {
            return VisionSemanticConversationContext.builder()
                    .slotData(Map.of())
                    .build();
        }

        return VisionSemanticConversationContext.builder()
                .conversationId(conversation.getId())
                .currentIntent(conversation.getIntent() == null ? "" : conversation.getIntent().name())
                .requestedSlot(clean(conversation.getRequestedSlot()))
                .slotData(conversation.getSlotData() == null ? Map.of() : new LinkedHashMap<>(conversation.getSlotData()))
                .activeSlot(clean(conversation.getRequestedSlot()))
                .draftSnapshot(conversation.getSlotData() == null ? Map.of() : new LinkedHashMap<>(conversation.getSlotData()))
                .build();
    }

    public VisionSemanticMemoryContext buildMemoryContext(AppUser user, VisionConversation conversation) {
        return VisionSemanticMemoryContext.builder()
                .userMemory(buildUserMemoryContext(user, conversation))
                .sessionMemory(buildSessionMemoryContext(conversation))
                .recentConversations(buildRecentConversationMemory(user))
                .build();
    }

    public VisionSemanticRuntimeContext buildRuntimeContext(VisionSemanticRuntimeHints runtimeHints) {
        if (runtimeHints == null) {
            return VisionSemanticRuntimeContext.builder()
                    .inputType("text")
                    .clientCapabilities(List.of())
                    .build();
        }

        return VisionSemanticRuntimeContext.builder()
                .inputType(clean(runtimeHints.getInputType()) == null ? "text" : clean(runtimeHints.getInputType()))
                .clientLocale(normalizeLocale(runtimeHints.getClientLocale()))
                .clientTimezone(normalizeTimezone(runtimeHints.getClientTimezone()))
                .clientCapabilities(runtimeHints.getClientCapabilities() == null ? List.of() : runtimeHints.getClientCapabilities())
                .clientStateVersion(clean(runtimeHints.getClientStateVersion()))
                .build();
    }

    String timezoneForCountry(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return DEFAULT_TIMEZONE;
        }
        return COUNTRY_TIMEZONES.getOrDefault(countryCode.trim().toUpperCase(Locale.ROOT), DEFAULT_TIMEZONE);
    }

    private ResolvedLocale resolveLocale(String countryCode, VisionSemanticRuntimeHints runtimeHints) {
        String clientLocale = runtimeHints == null ? null : normalizeLocale(runtimeHints.getClientLocale());
        if (clientLocale != null) {
            return new ResolvedLocale(clientLocale, CLIENT_HINT_SOURCE);
        }

        String configuredLocale = normalizeLocale(voiceProperties.getPreferredLocale());
        if (configuredLocale != null) {
            return new ResolvedLocale(configuredLocale, VOICE_CONFIG_SOURCE);
        }

        String countryLocale = localeForCountry(countryCode);
        if (countryLocale != null) {
            return new ResolvedLocale(countryLocale, COUNTRY_DEFAULT_SOURCE);
        }

        return new ResolvedLocale(DEFAULT_LOCALE, DEFAULT_SOURCE);
    }

    private ResolvedTimezone resolveTimezone(String countryCode, VisionSemanticRuntimeHints runtimeHints) {
        String clientTimezone = runtimeHints == null ? null : normalizeTimezone(runtimeHints.getClientTimezone());
        if (clientTimezone != null) {
            return new ResolvedTimezone(clientTimezone, CLIENT_HINT_SOURCE);
        }

        String countryTimezone = timezoneForCountry(countryCode);
        if (countryTimezone != null && !DEFAULT_TIMEZONE.equals(countryTimezone)) {
            return new ResolvedTimezone(countryTimezone, COUNTRY_DEFAULT_SOURCE);
        }

        return new ResolvedTimezone(DEFAULT_TIMEZONE, DEFAULT_SOURCE);
    }

    private String localeForCountry(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return null;
        }
        return COUNTRY_LOCALES.get(countryCode);
    }

    private String languageForLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return DEFAULT_LOCALE;
        }
        int separator = locale.indexOf('-');
        return separator > 0 ? locale.substring(0, separator) : locale;
    }

    private VisionSemanticUserMemoryContext buildUserMemoryContext(AppUser user, VisionConversation conversation) {
        if (user == null) {
            return VisionSemanticUserMemoryContext.builder()
                    .preferredLocale(DEFAULT_LOCALE)
                    .timezone(DEFAULT_TIMEZONE)
                    .preferredInputType(null)
                    .preferredInputTypeConfidence(null)
                    .preferredEntityFamily(null)
                    .preferredEntityFamilyConfidence(null)
                    .learningSummary(null)
                    .retrievalSummary(null)
                    .recentFeedbackTypes(List.of())
                    .recentIntentTypes(List.of())
                    .recentEntityFamilies(List.of())
                    .learnedPreferences(List.of())
                    .explainabilityRecords(List.of())
                    .retrievedEntityFamily(null)
                    .retrievedEntityFamilyConfidence(null)
                    .build();
        }

        List<String> recentIntentTypes = buildRecentIntentTypes(user, conversation);
        List<VisionLearningPreferenceDTO> learnedPreferences = buildPreferenceSignals(user);
        List<VisionLearningExplainabilityDTO> explainabilityRecords = buildExplainabilityRecords(user);
        VisionLearningPreferenceDTO preferredInputType = preferenceByKey(learnedPreferences, "preferred_input_type");
        VisionLearningPreferenceDTO preferredEntityFamily = preferenceByKey(learnedPreferences, "last_entity_family");
        VisionMemorySummary latestSummary = latestSummary(user);
        List<String> recentEntityFamilies = buildRecentEntityFamilies(user);
        String retrievedEntityFamily = retrievedEntityFamily(preferredEntityFamily, recentEntityFamilies);
        Double retrievedEntityFamilyConfidence = retrievedEntityFamilyConfidence(preferredEntityFamily, retrievedEntityFamily);
        String retrievalSummary = retrievalSummary(latestSummary, retrievedEntityFamily, recentEntityFamilies);
        String countryCode = normalizeCountryCode(user.getLocationCountryCode());
        ResolvedLocale resolvedLocale = resolveLocale(countryCode, null);
        ResolvedTimezone resolvedTimezone = resolveTimezone(countryCode, null);
        double confidenceThreshold = VisionPreferenceConfidenceSupport.threshold(visionProperties == null ? null : visionProperties.getMemory());
        return VisionSemanticUserMemoryContext.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole() == null ? "" : user.getRole().name())
                .preferredLocale(resolvedLocale.value())
                .timezone(resolvedTimezone.value())
                .countryCode(countryCode)
                .country(clean(user.getLocationCountry()))
                .locality(clean(user.getLocationLocality()))
                .locationLabel(clean(user.getLocationLabel()))
                .preferredInputType(preferredInputType == null || preferredInputType.getConfidenceScore() < confidenceThreshold
                        ? null
                        : clean(preferredInputType.getPreferenceValue()))
                .preferredInputTypeConfidence(preferredInputType == null ? null : preferredInputType.getConfidenceScore())
                .preferredEntityFamily(preferredEntityFamily == null || preferredEntityFamily.getConfidenceScore() < confidenceThreshold
                        ? null
                        : clean(preferredEntityFamily.getPreferenceValue()))
                .preferredEntityFamilyConfidence(preferredEntityFamily == null ? null : preferredEntityFamily.getConfidenceScore())
                .learningSummary(latestSummary == null ? null : clean(latestSummary.getSummaryText()))
                .retrievalSummary(retrievalSummary)
                .recentFeedbackTypes(buildRecentFeedbackTypes(user))
                .recentIntentTypes(recentIntentTypes)
                .recentEntityFamilies(recentEntityFamilies)
                .learnedPreferences(learnedPreferences)
                .explainabilityRecords(explainabilityRecords)
                .retrievedEntityFamily(retrievedEntityFamily)
                .retrievedEntityFamilyConfidence(retrievedEntityFamilyConfidence)
                .build();
    }

    private VisionSemanticSessionMemoryContext buildSessionMemoryContext(VisionConversation conversation) {
        if (conversation == null) {
            return VisionSemanticSessionMemoryContext.builder()
                    .slotData(Map.of())
                    .recentTurns(List.of())
                    .openQuestions(List.of())
                    .recentActions(List.of())
                    .build();
        }

        List<VisionSemanticTurnMemoryItem> recentTurns = buildRecentTurnMemory(conversation);
        return VisionSemanticSessionMemoryContext.builder()
                .conversationId(conversation.getId())
                .currentIntent(conversation.getIntent() == null ? "" : conversation.getIntent().name())
                .currentEntityFamily(entityFamilyFor(conversation.getIntent()))
                .status(conversation.getStatus() == null ? "" : conversation.getStatus().name())
                .requestedSlot(clean(conversation.getRequestedSlot()))
                .sessionSummary(buildSessionSummary(conversation))
                .lastUserPrompt(clean(conversation.getLastUserPrompt()))
                .lastNormalizedPrompt(clean(conversation.getLastNormalizedPrompt()))
                .lastAssistantMessage(clean(conversation.getLastAssistantMessage()))
                .translationReliable(conversation.isLastTranslationReliable())
                .sessionMemorySnapshot(clean(conversation.getSessionMemorySnapshot()))
                .slotData(conversation.getSlotData() == null ? Map.of() : new LinkedHashMap<>(conversation.getSlotData()))
                .openQuestions(buildOpenQuestions(conversation))
                .recentActions(buildRecentActions(recentTurns))
                .recentTurns(recentTurns)
                .build();
    }

    private String buildSessionSummary(VisionConversation conversation) {
        if (conversation == null) {
            return "";
        }

        String assistantMessage = clean(conversation.getLastAssistantMessage());
        if (assistantMessage != null) {
            return assistantMessage;
        }

        String requestedSlot = clean(conversation.getRequestedSlot());
        if (requestedSlot != null) {
            return "Waiting for " + requestedSlot;
        }

        return switch (conversation.getStatus() == null ? VisionConversationStatus.ACTIVE : conversation.getStatus()) {
            case REVIEW_READY -> "Review ready";
            case COMPLETED -> "Conversation complete";
            case BLOCKED -> "Conversation blocked";
            case ACTIVE -> "Conversation active";
        };
    }

    private List<String> buildOpenQuestions(VisionConversation conversation) {
        if (conversation == null) {
            return List.of();
        }

        String requestedSlot = clean(conversation.getRequestedSlot());
        if (requestedSlot == null) {
            return List.of();
        }

        String assistantMessage = clean(conversation.getLastAssistantMessage());
        if (assistantMessage != null && assistantMessage.contains("?")) {
            return List.of(assistantMessage);
        }

        return List.of("Waiting for " + requestedSlot);
    }

    private List<String> buildRecentActions(List<VisionSemanticTurnMemoryItem> recentTurns) {
        if (recentTurns == null || recentTurns.isEmpty()) {
            return List.of();
        }

        return recentTurns.stream()
                .limit(3)
                .map(this::describeRecentAction)
                .filter(action -> action != null && !action.isBlank())
                .toList();
    }

    private String describeRecentAction(VisionSemanticTurnMemoryItem turn) {
        if (turn == null) {
            return null;
        }

        String prompt = clean(turn.getNormalizedPrompt());
        if (prompt == null) {
            prompt = clean(turn.getPrompt());
        }
        String assistantMessage = clean(turn.getAssistantMessage());
        if (prompt == null && assistantMessage == null) {
            return null;
        }
        if (prompt == null) {
            return assistantMessage;
        }
        if (assistantMessage == null) {
            return prompt;
        }
        return prompt + " -> " + assistantMessage;
    }

    private List<VisionSemanticConversationMemoryItem> buildRecentConversationMemory(AppUser user) {
        if (user == null || visionConversationRepository == null) {
            return List.of();
        }

        return visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(user).stream()
                .map(this::toConversationMemoryItem)
                .toList();
    }

    private List<VisionSemanticTurnMemoryItem> buildRecentTurnMemory(VisionConversation conversation) {
        if (conversation == null || visionTurnRepository == null) {
            return List.of();
        }

        List<VisionTurn> turns = visionTurnRepository.findTop10ByConversationOrderByTurnIndexDesc(conversation);
        if (turns == null || turns.isEmpty()) {
            return List.of();
        }

        List<VisionSemanticTurnMemoryItem> items = new ArrayList<>();
        for (int index = turns.size() - 1; index >= 0; index--) {
            VisionTurn turn = turns.get(index);
            items.add(VisionSemanticTurnMemoryItem.builder()
                    .turnIndex(turn.getTurnIndex())
                    .source(turn.getSource() == null ? VisionTurnSource.TEXT.name() : turn.getSource().name())
                    .prompt(clean(turn.getPrompt()))
                    .normalizedPrompt(clean(turn.getNormalizedPrompt()))
                    .detectedIntent(turn.getDetectedIntent() == null ? "" : turn.getDetectedIntent().name())
                    .requestedSlot(clean(turn.getRequestedSlot()))
                    .assistantMessage(clean(turn.getAssistantMessage()))
                    .build());
        }
        return items;
    }

    private List<String> buildRecentIntentTypes(AppUser user, VisionConversation currentConversation) {
        if (user == null || visionConversationRepository == null) {
            return List.of();
        }

        List<VisionConversation> conversations = visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(user);
        if (conversations == null || conversations.isEmpty()) {
            return List.of();
        }

        List<String> intentTypes = new ArrayList<>();
        for (VisionConversation item : conversations) {
            if (item == null || item.getIntent() == null) {
                continue;
            }
            String intent = item.getIntent().name();
            if (!intentTypes.contains(intent)) {
                intentTypes.add(intent);
            }
        }
        if (currentConversation != null && currentConversation.getIntent() != null) {
            String currentIntent = currentConversation.getIntent().name();
            if (!intentTypes.contains(currentIntent)) {
                intentTypes.add(0, currentIntent);
            }
        }
        return intentTypes;
    }

    private List<String> buildRecentEntityFamilies(AppUser user) {
        if (user == null || visionConversationRepository == null) {
            return List.of();
        }

        List<VisionConversation> conversations = visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(user);
        if (conversations == null || conversations.isEmpty()) {
            return List.of();
        }

        List<String> families = new ArrayList<>();
        for (VisionConversation conversation : conversations) {
            String family = entityFamilyFor(conversation == null ? null : conversation.getIntent());
            if (family != null && !family.isBlank() && !families.contains(family)) {
                families.add(family);
            }
        }
        return families;
    }

    private List<String> buildRecentFeedbackTypes(AppUser user) {
        if (user == null || visionMemoryFeedbackEventRepository == null) {
            return List.of();
        }

        return visionMemoryFeedbackEventRepository.findTop20ByUserOrderByCreatedAtDesc(user).stream()
                .map(event -> event.getFeedbackType() == null ? null : event.getFeedbackType().name())
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .limit(3)
                .toList();
    }

    private List<VisionLearningPreferenceDTO> buildPreferenceSignals(AppUser user) {
        if (user == null || visionUserPreferenceRepository == null) {
            return List.of();
        }

        List<VisionUserPreference> preferences = visionUserPreferenceRepository.findByUser(user);
        if (preferences == null || preferences.isEmpty()) {
            return List.of();
        }

        return VisionPreferenceConfidenceSupport.toPreferenceSignals(
                preferences,
                Instant.now(),
                visionProperties == null ? null : visionProperties.getMemory(),
                8,
                CONTEXT_PREFERENCE_PRIORITY
        );
    }

    private List<VisionLearningExplainabilityDTO> buildExplainabilityRecords(AppUser user) {
        if (user == null || visionUserPreferenceRepository == null) {
            return List.of();
        }

        List<VisionUserPreference> preferences = visionUserPreferenceRepository.findByUser(user);
        if (preferences == null || preferences.isEmpty()) {
            return List.of();
        }

        int explainabilityWindow = 8;
        List<VisionLearningPreferenceDTO> explainabilitySignals = VisionPreferenceConfidenceSupport.toPreferenceSignals(
                preferences,
                Instant.now(),
                visionProperties == null ? null : visionProperties.getMemory(),
                explainabilityWindow,
                CONTEXT_PREFERENCE_PRIORITY
        );
        return VisionPreferenceConfidenceSupport.toExplainabilityRecords(explainabilitySignals, explainabilityWindow);
    }

    private String retrievedEntityFamily(VisionLearningPreferenceDTO preferredEntityFamily, List<String> recentEntityFamilies) {
        double confidenceThreshold = VisionPreferenceConfidenceSupport.threshold(visionProperties == null ? null : visionProperties.getMemory());
        if (preferredEntityFamily != null
                && preferredEntityFamily.getConfidenceScore() != null
                && preferredEntityFamily.getConfidenceScore() >= confidenceThreshold) {
            return clean(preferredEntityFamily.getPreferenceValue());
        }

        if (recentEntityFamilies == null || recentEntityFamilies.isEmpty()) {
            return null;
        }

        for (String family : recentEntityFamilies) {
            if (family != null && !family.isBlank()) {
                return clean(family);
            }
        }
        return null;
    }

    private Double retrievedEntityFamilyConfidence(VisionLearningPreferenceDTO preferredEntityFamily, String retrievedEntityFamily) {
        if (retrievedEntityFamily == null) {
            return null;
        }

        double threshold = VisionPreferenceConfidenceSupport.threshold(visionProperties == null ? null : visionProperties.getMemory());
        if (preferredEntityFamily != null
                && preferredEntityFamily.getConfidenceScore() != null
                && preferredEntityFamily.getPreferenceValue() != null
                && retrievedEntityFamily.equals(clean(preferredEntityFamily.getPreferenceValue()))
                && preferredEntityFamily.getConfidenceScore() >= threshold) {
            return preferredEntityFamily.getConfidenceScore();
        }

        return threshold;
    }

    private String retrievalSummary(VisionMemorySummary latestSummary, String retrievedEntityFamily, List<String> recentEntityFamilies) {
        String summaryText = latestSummary == null ? null : clean(latestSummary.getSummaryText());
        if (retrievedEntityFamily == null) {
            return summaryText;
        }

        String familyText = "retrieval_focus=" + retrievedEntityFamily;
        if (summaryText == null || summaryText.isBlank()) {
            return familyText;
        }
        if (recentEntityFamilies == null || recentEntityFamilies.isEmpty()) {
            return summaryText + " | " + familyText;
        }
        return summaryText + " | " + familyText + " | recent_entity_families=" + String.join(",", recentEntityFamilies);
    }

    private VisionLearningPreferenceDTO preferenceByKey(List<VisionLearningPreferenceDTO> preferences, String preferenceKey) {
        if (preferences == null || preferences.isEmpty() || preferenceKey == null || preferenceKey.isBlank()) {
            return null;
        }

        for (VisionLearningPreferenceDTO preference : preferences) {
            if (preference != null && preferenceKey.equals(preference.getPreferenceKey())) {
                return preference;
            }
        }
        return null;
    }

    private VisionMemorySummary latestSummary(AppUser user) {
        if (user == null || visionMemorySummaryRepository == null) {
            return null;
        }

        return visionMemorySummaryRepository.findTopByUserOrderByCreatedAtDesc(user).orElse(null);
    }

    private VisionSemanticConversationMemoryItem toConversationMemoryItem(VisionConversation conversation) {
        if (conversation == null) {
            return null;
        }

        return VisionSemanticConversationMemoryItem.builder()
                .conversationId(conversation.getId())
                .intent(conversation.getIntent() == null ? "" : conversation.getIntent().name())
                .status(conversation.getStatus() == null ? "" : conversation.getStatus().name())
                .requestedSlot(clean(conversation.getRequestedSlot()))
                .title(memoryTitle(conversation))
                .subtitle(memorySubtitle(conversation))
                .groupKey(memoryGroupKey(conversation))
                .stageLabel(memoryStageLabel(conversation))
                .progressLabel(memoryProgressLabel(conversation))
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private String memoryTitle(VisionConversation conversation) {
        if (conversation == null || conversation.getSlotData() == null) {
            return conversation == null || conversation.getIntent() == null ? "" : conversation.getIntent().name();
        }

        String title = conversation.getSlotData().get("quest_title");
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("circle_name");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("profile_username");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("search_query");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getIntent() == null ? "" : conversation.getIntent().name();
        }
        return title;
    }

    private String memorySubtitle(VisionConversation conversation) {
        if (conversation == null) {
            return "";
        }
        if (conversation.getLastAssistantMessage() != null && !conversation.getLastAssistantMessage().isBlank()) {
            return conversation.getLastAssistantMessage();
        }
        return conversation.getRequestedSlot() == null
                ? "No pending prompt"
                : "Waiting for " + conversation.getRequestedSlot();
    }

    private String memoryGroupKey(VisionConversation conversation) {
        if (conversation == null || conversation.getStatus() == null) {
            return "active";
        }
        return switch (conversation.getStatus()) {
            case ACTIVE -> "active";
            case REVIEW_READY -> "review_ready";
            case BLOCKED -> "blocked";
            case COMPLETED -> "completed";
        };
    }

    private String memoryStageLabel(VisionConversation conversation) {
        if (conversation == null || conversation.getStatus() == null) {
            return "In progress";
        }
        return switch (conversation.getStatus()) {
            case ACTIVE -> conversation.getRequestedSlot() == null ? "In progress" : "Needs input";
            case REVIEW_READY -> "Review ready";
            case COMPLETED -> "Complete";
            case BLOCKED -> "Blocked";
        };
    }

    private String memoryProgressLabel(VisionConversation conversation) {
        if (conversation == null || conversation.getStatus() == null) {
            return "Conversation is active.";
        }
        return switch (conversation.getStatus()) {
            case ACTIVE -> conversation.getRequestedSlot() == null
                    ? "Conversation is active."
                    : "Next step: " + conversation.getRequestedSlot();
            case REVIEW_READY -> "Ready for review and confirmation.";
            case COMPLETED -> "Task finished.";
            case BLOCKED -> "Conversation stopped until the user starts a supported task.";
        };
    }

    private String entityFamilyFor(com.themuffinman.app.vision.model.VisionIntent intent) {
        if (intent == null) {
            return null;
        }
        return switch (intent) {
            case VIEW_PROFILE, VIEW_SETTINGS, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION -> "profile";
            case VIEW_NOTIFICATIONS -> "notifications";
            case VIEW_CIRCLES, VIEW_CIRCLE_DETAIL, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST,
                    DELETE_CIRCLE_REQUEST, UPDATE_CIRCLE, DELETE_CIRCLE -> "circles";
            case VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL, CREATE_APPLICATION, UPDATE_APPLICATION,
                    WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION -> "applications";
            case DISCOVER_QUESTS, CREATE_QUEST, VIEW_QUEST_DETAIL -> "quests";
            case VIEW_QUEST_NEWS -> "quest news";
            case OPEN_CHAT, VIEW_CHAT_WORKSPACE -> "chat";
            default -> "other";
        };
    }

    private String normalizeCountryCode(String countryCode) {
        String cleaned = clean(countryCode);
        return cleaned == null ? "" : cleaned.toUpperCase(Locale.ROOT);
    }

    private String normalizeLocale(String locale) {
        String cleaned = clean(locale);
        if (cleaned == null) {
            return null;
        }
        String normalized = cleaned.replace('_', '-');
        String[] parts = normalized.split("-");
        if (parts.length == 1) {
            return parts[0].toLowerCase(Locale.ROOT);
        }
        return parts[0].toLowerCase(Locale.ROOT) + "-" + parts[1].toUpperCase(Locale.ROOT);
    }

    private String normalizeTimezone(String timezone) {
        String cleaned = clean(timezone);
        if (cleaned == null) {
            return null;
        }
        Set<String> availableTimezones = Set.of(TimeZone.getAvailableIDs());
        return availableTimezones.contains(cleaned) ? cleaned : null;
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record ResolvedLocale(String value, String source) {
    }

    private record ResolvedTimezone(String value, String source) {
    }
}
