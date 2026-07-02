package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisionSemanticOrchestrationContextServiceTest {

    private final VoiceProperties voiceProperties = new VoiceProperties();
    private final VisionSemanticOrchestrationContextService service =
            new VisionSemanticOrchestrationContextService(voiceProperties);

    @Test
    void prefersClientRuntimeHintsOverCountryDefaults() {
        AppUser user = new AppUser();
        user.setId(42L);
        user.setUsername("jsak");
        user.setLocationCountryCode("ch");
        user.setLocationCountry("Switzerland");
        user.setLocationLocality("Zurich");
        user.setLocationLabel("Zurich, Switzerland");

        VisionSemanticUserContext context = service.buildUserContext(user, VisionSemanticRuntimeHints.builder()
                .clientLocale("en-GB")
                .clientTimezone("Europe/London")
                .build());

        assertEquals("en-GB", context.getPreferredLocale());
        assertEquals("en", context.getPreferredLanguage());
        assertEquals("client_runtime_hint", context.getPreferredLocaleSource());
        assertEquals("Europe/London", context.getTimezone());
        assertEquals("client_runtime_hint", context.getTimezoneSource());
        assertEquals("CH", context.getCountryCode());
        assertEquals("Zurich", context.getLocality());
    }

    @Test
    void fallsBackToConfiguredVoiceLocaleBeforeGlobalDefault() {
        voiceProperties.setPreferredLocale("de-CH");

        VisionSemanticUserContext context = service.buildUserContext(null);

        assertEquals("de-CH", context.getPreferredLocale());
        assertEquals("de", context.getPreferredLanguage());
        assertEquals("voice_config_default", context.getPreferredLocaleSource());
        assertEquals("UTC", context.getTimezone());
        assertEquals("default", context.getTimezoneSource());
    }

    @Test
    void usesCountryDefaultsWhenRuntimeHintsAreMissing() {
        AppUser user = new AppUser();
        user.setLocationCountryCode("hr");

        VisionSemanticUserContext context = service.buildUserContext(user, null);

        assertEquals("hr-HR", context.getPreferredLocale());
        assertEquals("country_default", context.getPreferredLocaleSource());
        assertEquals("Europe/Zagreb", context.getTimezone());
        assertEquals("country_default", context.getTimezoneSource());
    }
}
