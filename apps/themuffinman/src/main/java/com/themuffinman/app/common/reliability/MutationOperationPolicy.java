package com.themuffinman.app.common.reliability;

import java.util.Objects;

/** Immutable policy metadata for one mutation operation. */
public record MutationOperationPolicy(
        String operationKey,
        boolean idempotencyKeyRequired,
        boolean resourceVersionRequired,
        boolean retryable
) {

    public MutationOperationPolicy {
        if (operationKey == null || operationKey.isBlank()) {
            throw new IllegalArgumentException("Mutation operation key is required");
        }
        operationKey = operationKey.trim();
    }

    public static MutationOperationPolicy idempotentCreate(String operationKey) {
        return new MutationOperationPolicy(operationKey, true, false, true);
    }

    public static MutationOperationPolicy versionedUpdate(String operationKey) {
        return new MutationOperationPolicy(operationKey, true, true, true);
    }

    public static MutationOperationPolicy stateTransition(String operationKey) {
        return new MutationOperationPolicy(operationKey, true, true, false);
    }

    public boolean sameOperation(String candidate) {
        return Objects.equals(operationKey, candidate);
    }
}
