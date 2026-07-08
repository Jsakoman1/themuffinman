package com.themuffinman.app.common.normalization;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SlugNormalizerTest {

    @Test
    void normalizeSlugPreservesValidInput() {
        assertEquals("dog-groomer", SlugNormalizer.normalizeSlug("  dog-groomer ", "required", "invalid"));
    }

    @Test
    void slugifyDerivesKebabCaseSlug() {
        assertEquals("dog-groomer", SlugNormalizer.slugify("Dog Groomer", "cannot derive"));
    }

    @Test
    void normalizeSlugRejectsInvalidCharacters() {
        assertThrows(ResponseStatusException.class, () -> SlugNormalizer.normalizeSlug("Dog Groomer!", "required", "invalid"));
    }
}
