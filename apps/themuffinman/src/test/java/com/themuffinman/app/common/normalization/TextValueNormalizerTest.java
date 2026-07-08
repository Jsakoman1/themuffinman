package com.themuffinman.app.common.normalization;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextValueNormalizerTest {

    @Test
    void trimToNullCollapsesBlankValues() {
        assertNull(TextValueNormalizer.trimToNull("   "));
        assertEquals("hello", TextValueNormalizer.trimToNull(" hello "));
    }

    @Test
    void requireTrimmedRejectsBlankValues() {
        assertThrows(ResponseStatusException.class, () -> TextValueNormalizer.requireTrimmed(" ", "value is required"));
    }

    @Test
    void lowerAndUpperTrimHelpersNormalizeCase() {
        assertEquals("hello", TextValueNormalizer.lowerTrimToNull("  HeLLo "));
        assertEquals("HELLO", TextValueNormalizer.upperTrimToNull("  hello "));
    }
}
