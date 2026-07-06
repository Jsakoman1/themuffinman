package com.themuffinman.app.vision.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VisionPromptTextSupportTest {

    @Test
    void extractsCreateCircleNamesFromStandardPrefixes() {
        assertEquals(
                "Lover",
                VisionPromptTextSupport.extractAfterAnyPrefix(
                        "create new circle Lover",
                        List.of("create new circle", "create circle", "circle")
                )
        );
        assertEquals(
                "Friends",
                VisionPromptTextSupport.extractAfterAnyPrefix(
                        "rename circle Friends to Core Team",
                        List.of("rename circle", "update circle"),
                        List.of(" to ")
                )
        );
        assertEquals(
                "Core Team",
                VisionPromptTextSupport.extractAfterAnyPrefix(
                        "rename to Core Team",
                        List.of("rename to", "new name", "change name to")
                )
        );
    }

    @Test
    void extractsEntityQueriesFromReadOnlyPrefixes() {
        assertEquals(
                "#42",
                VisionPromptTextSupport.extractAfterAnyPrefix(
                        "show application #42",
                        List.of("show application", "open application", "application")
                )
        );
        assertEquals(
            "Josip",
            VisionPromptTextSupport.stripLeadingWords(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            "chat with user Josip",
                            List.of("chat with", "open chat with")
                    ),
                    List.of("user", "users", "person", "people", "contact", "contacts", "member", "members")
            )
        );
    }

    @Test
    void returnsNullWhenNoPrefixMatches() {
        assertNull(VisionPromptTextSupport.extractAfterAnyPrefix("Lover", List.of("create circle", "circle")));
    }

    @Test
    void returnsNullForBlankInputWhenStrippingWords() {
        assertNull(VisionPromptTextSupport.stripLeadingWords("   ", List.of("called", "named")));
    }
}
