package com.themuffinman.app.common.validation;

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
    void sanitizeKeepsMarkdownLikePlainTextAsIs() {
        assertEquals("**Play** _harder_", RichTextInputValidator.sanitize("**Play** _harder_"));
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
    void sanitizePreservesLeadingWhitespaceInsideRichText() {
        assertEquals("<p>&nbsp;&nbsp;Need help</p>", RichTextInputValidator.sanitize("<p>  Need help</p>"));
    }

    @Test
    void hasContentKeepsPlainTextContent() {
        assertTrue(RichTextInputValidator.hasContent("Need help"));
    }

    @Test
    void extractPlainTextReadsSanitizedHtmlContent() {
        assertEquals("Need help today", RichTextInputValidator.extractPlainText("<p><strong>Need</strong> help <em>today</em></p>"));
    }
}
