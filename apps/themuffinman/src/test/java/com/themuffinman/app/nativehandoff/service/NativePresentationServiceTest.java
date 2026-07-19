package com.themuffinman.app.nativehandoff.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NativePresentationServiceTest {
    private final NativePresentationService service = new NativePresentationService();

    @Test
    void watchContractIsCompactAndReconnectSafe() {
        var result = service.getContract("watch");
        assertEquals("native-presentation-v1", result.getContractVersion());
        assertEquals("GLANCE_ACTION", result.getPresentationMode());
        assertEquals(2, result.getMaxVisibleActions());
        assertEquals("READ_ONLY_CACHED_CONTEXT; MUTATIONS_REQUIRE_RECONNECT", result.getOfflinePolicy());
    }

    @Test
    void rejectsUnknownDevice() {
        assertThrows(ResponseStatusException.class, () -> service.getContract("tablet"));
    }
}
