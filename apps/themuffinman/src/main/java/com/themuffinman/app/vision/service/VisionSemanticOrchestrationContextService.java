package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

@Service
public class VisionSemanticOrchestrationContextService {

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

    public VisionSemanticOrchestrationContextService(VoiceProperties voiceProperties) {
        this.voiceProperties = voiceProperties;
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
