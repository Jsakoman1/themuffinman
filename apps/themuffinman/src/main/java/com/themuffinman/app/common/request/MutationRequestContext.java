package com.themuffinman.app.common.request;

import java.util.Optional;

/**
 * Request-scoped reliability metadata shared by controllers, services, and
 * structured error responses. It carries identity for diagnostics only; it
 * does not decide permissions, workflow transitions, or idempotency policy.
 */
public final class MutationRequestContext {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    public static final String OPERATION_KEY_HEADER = "X-Operation-Key";

    private static final ThreadLocal<Snapshot> CURRENT = new ThreadLocal<>();

    private MutationRequestContext() {
    }

    public static void install(Snapshot snapshot) {
        CURRENT.set(snapshot);
    }

    public static Optional<Snapshot> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static String correlationId() {
        return current().map(Snapshot::correlationId).orElse(null);
    }

    public static String operationKey() {
        return current().map(Snapshot::operationKey).orElse(null);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public record Snapshot(
            String requestId,
            String correlationId,
            String idempotencyKey,
            String operationKey
    ) {
    }
}
