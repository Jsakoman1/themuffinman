package com.themuffinman.app.semantic;

import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionEntityResolverRegistryTest {

    @Test
    void returnsNotFoundWhenResolverIsMissing() {
        VisionEntityResolverRegistry registry = new VisionEntityResolverRegistry(List.of());

        SemanticEntityResolution<String> result = registry.resolve(SemanticEntityFamily.UNKNOWN, new AppUser(), "anything");

        assertEquals(SemanticEntityResolutionStatus.NOT_FOUND, result.getStatus());
        assertEquals(SemanticEntityFamily.UNKNOWN, result.getEntityFamily());
        assertEquals(0.10d, result.getConfidence());
    }

    @Test
    void delegatesToResolverForRegisteredFamily() {
        VisionEntityResolver<String> resolver = new VisionEntityResolver<>() {
            @Override
            public SemanticEntityFamily family() {
                return SemanticEntityFamily.USER;
            }

            @Override
            public SemanticEntityResolution<String> resolve(AppUser currentUser, String targetEntityQuery) {
                return SemanticEntityResolution.<String>builder()
                        .entityFamily(SemanticEntityFamily.USER)
                        .status(SemanticEntityResolutionStatus.RESOLVED)
                        .targetEntityQuery(targetEntityQuery)
                        .entity("Josip")
                        .canonicalLabel("Josip")
                        .confidence(0.97d)
                        .build();
            }
        };

        VisionEntityResolverRegistry registry = new VisionEntityResolverRegistry(List.of(resolver));
        SemanticEntityResolution<String> result = registry.resolve(SemanticEntityFamily.USER, new AppUser(), "Josip");

        assertTrue(result.resolved());
        assertEquals("Josip", result.getCanonicalLabel());
        assertEquals(0.97d, result.getConfidence());
    }
}
