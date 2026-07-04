package com.themuffinman.app.semantic;

import com.themuffinman.app.identity.model.AppUser;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class VisionEntityResolverRegistry {

    private final Map<SemanticEntityFamily, VisionEntityResolver<?>> resolvers = new EnumMap<>(SemanticEntityFamily.class);
    private final SemanticAliasRegistry semanticAliasRegistry;

    public VisionEntityResolverRegistry(List<VisionEntityResolver<?>> resolvers, SemanticAliasRegistry semanticAliasRegistry) {
        this.semanticAliasRegistry = semanticAliasRegistry == null ? new SemanticAliasRegistry() : semanticAliasRegistry;
        if (resolvers != null) {
            for (VisionEntityResolver<?> resolver : resolvers) {
                if (resolver != null) {
                    this.resolvers.put(resolver.family(), resolver);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> SemanticEntityResolution<T> resolve(SemanticEntityFamily family, AppUser currentUser, String targetEntityQuery) {
        String canonicalQuery = normalizeTargetEntityQuery(family, targetEntityQuery);
        VisionEntityResolver<?> resolver = resolvers.get(family == null ? SemanticEntityFamily.UNKNOWN : family);
        if (resolver == null) {
            return SemanticEntityResolution.<T>builder()
                    .entityFamily(family == null ? SemanticEntityFamily.UNKNOWN : family)
                    .status(SemanticEntityResolutionStatus.NOT_FOUND)
                    .targetEntityQuery(canonicalQuery)
                    .ambiguityReason("No resolver is registered for this entity family.")
                    .confidence(SemanticEntityResolutionSupport.confidenceForNotFound(canonicalQuery, "No resolver is registered for this entity family."))
                    .build();
        }
        return (SemanticEntityResolution<T>) resolver.resolve(currentUser, canonicalQuery);
    }

    private String normalizeTargetEntityQuery(SemanticEntityFamily family, String targetEntityQuery) {
        if (targetEntityQuery == null || targetEntityQuery.isBlank()) {
            return targetEntityQuery;
        }
        if (family == null || family == SemanticEntityFamily.UNKNOWN || semanticAliasRegistry == null) {
            return targetEntityQuery.trim();
        }
        return semanticAliasRegistry.normalizeQuery(family, targetEntityQuery).trim();
    }
}
