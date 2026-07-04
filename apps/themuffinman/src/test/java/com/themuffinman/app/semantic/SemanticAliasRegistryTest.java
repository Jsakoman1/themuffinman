package com.themuffinman.app.semantic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SemanticAliasRegistryTest {

    private final SemanticAliasRegistry registry = new SemanticAliasRegistry();

    @Test
    void normalizesQuestAliasesToCanonicalQuery() {
        assertEquals("wash my luggage", registry.normalizeQuery(SemanticEntityFamily.QUEST, "wash my suitcases"));
        assertEquals("wash my luggage", registry.normalizeQuery(SemanticEntityFamily.QUEST, "wash my kofere"));
    }

    @Test
    void normalizesCircleAliasesToCanonicalQuery() {
        assertEquals("create circle Neighbours", registry.normalizeQuery(SemanticEntityFamily.CIRCLE, "create group Neighbours"));
    }

    @Test
    void normalizesApplicationAndUserAliasesToCanonicalQuery() {
        assertEquals("open application details", registry.normalizeQuery(SemanticEntityFamily.APPLICATION, "open request details"));
        assertEquals("open user details", registry.normalizeQuery(SemanticEntityFamily.USER, "open profile details"));
    }
}
