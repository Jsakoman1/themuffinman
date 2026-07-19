package com.themuffinman.app.nativehandoff.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.nativehandoff.dto.NativePresentationDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class NativePresentationService {
    public NativePresentationDTO getContract(String targetDevice) {
        String device = targetDevice == null ? "" : targetDevice.trim().toUpperCase(Locale.ROOT);
        if (!device.equals("MOBILE") && !device.equals("WATCH")) {
            throw ServiceErrors.badRequest("Target device must be MOBILE or WATCH");
        }
        boolean watch = device.equals("WATCH");
        return NativePresentationDTO.builder()
                .contractVersion("native-presentation-v1")
                .targetDevice(device)
                .presentationMode(watch ? "GLANCE_ACTION" : "FULL_CONTEXT")
                .maxVisibleActions(watch ? 2 : 5)
                .supportsVoiceInput(true)
                .supportsTextInput(!watch)
                .supportsHapticFeedback(watch)
                .supportsBackgroundRefresh(watch)
                .allowedCapabilities(List.of("VIEW_NOTIFICATIONS", "VIEW_ACTIVITY", "OPEN_DETAIL", "CONFIRM_ACTION", "RETRY_ACTION"))
                .offlinePolicy(watch ? "READ_ONLY_CACHED_CONTEXT; MUTATIONS_REQUIRE_RECONNECT" : "QUEUE_ONLY_EXPLICITLY_RESUMABLE_ACTIONS")
                .accessibilityPolicy("SYSTEM_DYNAMIC_TYPE_AND_VOICEOVER_REQUIRED")
                .build();
    }
}
