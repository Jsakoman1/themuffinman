package com.themuffinman.app.semantic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SemanticAliasRegistryTest {

    private final SemanticAliasRegistry registry = new SemanticAliasRegistry();

    @Test
    void normalizesQuestAliasesToCanonicalQuery() {
        assertEquals("wash my luggage", registry.normalizeQuery(SemanticEntityFamily.QUEST, "wash my suitcases"));
        assertEquals("wash my luggage", registry.normalizeQuery(SemanticEntityFamily.QUEST, "wash my kofere"));
        assertEquals("finish my quest", registry.normalizeQuery(SemanticEntityFamily.QUEST, "finish my task"));
        assertEquals("finish my quest", registry.normalizeQuery(SemanticEntityFamily.QUEST, "finish my job"));
    }

    @Test
    void normalizesCircleAliasesToCanonicalQuery() {
        assertEquals("create circle Neighbours", registry.normalizeQuery(SemanticEntityFamily.CIRCLE, "create group Neighbours"));
        assertEquals("create circle Neighbours", registry.normalizeQuery(SemanticEntityFamily.CIRCLE, "create team Neighbours"));
        assertEquals("create circle Neighbours", registry.normalizeQuery(SemanticEntityFamily.CIRCLE, "create community Neighbours"));
    }

    @Test
    void normalizesApplicationAndUserAliasesToCanonicalQuery() {
        assertEquals("open application details", registry.normalizeQuery(SemanticEntityFamily.APPLICATION, "open request details"));
        assertEquals("open application details", registry.normalizeQuery(SemanticEntityFamily.APPLICATION, "open submission details"));
        assertEquals("open application details", registry.normalizeQuery(SemanticEntityFamily.APPLICATION, "open apply details"));
        assertEquals("open user details", registry.normalizeQuery(SemanticEntityFamily.USER, "open profile details"));
        assertEquals("open user details", registry.normalizeQuery(SemanticEntityFamily.USER, "open member details"));
        assertEquals("open user details", registry.normalizeQuery(SemanticEntityFamily.USER, "open account details"));
    }

    @Test
    void leavesAliasesInsideLongerWordsUntouched() {
        assertEquals("jobless market update", registry.normalizeQuery(SemanticEntityFamily.QUEST, "jobless market update"));
        assertEquals("requester details", registry.normalizeQuery(SemanticEntityFamily.APPLICATION, "requester details"));
        assertEquals("accounting note", registry.normalizeQuery(SemanticEntityFamily.USER, "accounting note"));
        assertEquals("communitywide circle", registry.normalizeQuery(SemanticEntityFamily.CIRCLE, "communitywide circle"));
    }
}
