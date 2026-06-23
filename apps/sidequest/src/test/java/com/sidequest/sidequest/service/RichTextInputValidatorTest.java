package com.sidequest.sidequest.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RichTextInputValidatorTest {

    @Test
    void sanitizeKeepsPlainTextAsIs() {
        assertEquals("Hello world", RichTextInputValidator.sanitize("Hello world"));
    }

    @Test
    void sanitizeRemovesUnsafeHtml() {
        assertEquals("<p>Hello</p>", RichTextInputValidator.sanitize("<p>Hello</p><script>alert(1)</script>"));
    }

    @Test
    void hasContentTreatsEmptyMarkupAsEmpty() {
        assertFalse(RichTextInputValidator.hasContent("<p><br></p>"));
    }

    @Test
    void sanitizeReturnsNullForBlankInput() {
        assertNull(RichTextInputValidator.sanitize("   "));
    }

    @Test
    void hasContentKeepsPlainTextContent() {
        assertTrue(RichTextInputValidator.hasContent("Need help"));
    }
}
