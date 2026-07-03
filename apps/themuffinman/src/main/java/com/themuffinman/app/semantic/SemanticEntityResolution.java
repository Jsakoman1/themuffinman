package com.themuffinman.app.semantic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemanticEntityResolution<T> {

    private SemanticEntityFamily entityFamily;
    private SemanticEntityResolutionStatus status;
    private String targetEntityQuery;
    private T entity;
    private String canonicalLabel;
    private String ambiguityReason;
    @Builder.Default
    private List<String> aliasMatches = List.of();
    private Double confidence;

    public boolean resolved() {
        return status == SemanticEntityResolutionStatus.RESOLVED && entity != null;
    }

    public boolean ambiguous() {
        return status == SemanticEntityResolutionStatus.AMBIGUOUS;
    }

    public boolean notFound() {
        return status == SemanticEntityResolutionStatus.NOT_FOUND;
    }
}
