package com.themuffinman.app.common.concepts;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record CircleVisibilitySelection(List<Long> circleIds) {

    public static CircleVisibilitySelection from(List<Long> circleIds) {
        return new CircleVisibilitySelection(circleIds == null ? List.of() : List.copyOf(circleIds));
    }

    public boolean unrestricted() {
        return circleIds.isEmpty();
    }

    public int distinctCount() {
        return asDistinctSet().size();
    }

    public Set<Long> asDistinctSet() {
        return new LinkedHashSet<>(circleIds);
    }
}
