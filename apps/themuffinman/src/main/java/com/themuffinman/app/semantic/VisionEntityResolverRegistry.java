package com.themuffinman.app.semantic;

import com.themuffinman.app.identity.model.AppUser;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class VisionEntityResolverRegistry {

    private final Map<SemanticEntityFamily, VisionEntityResolver<?>> resolvers = new EnumMap<>(SemanticEntityFamily.class);

    public VisionEntityResolverRegistry(List<VisionEntityResolver<?>> resolvers) {
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
        VisionEntityResolver<?> resolver = resolvers.get(family == null ? SemanticEntityFamily.UNKNOWN : family);
        if (resolver == null) {
            return SemanticEntityResolution.<T>builder()
                    .entityFamily(family == null ? SemanticEntityFamily.UNKNOWN : family)
                    .status(SemanticEntityResolutionStatus.NOT_FOUND)
                    .targetEntityQuery(targetEntityQuery)
                    .ambiguityReason("No resolver is registered for this entity family.")
                    .confidence(SemanticEntityResolutionSupport.confidenceForNotFound(targetEntityQuery, "No resolver is registered for this entity family."))
                    .build();
        }
        return (SemanticEntityResolution<T>) resolver.resolve(currentUser, targetEntityQuery);
    }
}
