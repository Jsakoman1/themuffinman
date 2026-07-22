package com.themuffinman.app.common.reliability;

import org.junit.jupiter.api.Test;
import com.themuffinman.app.common.errors.CodedResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MutationIdempotencyServiceTest {

    private final MutationIdempotencyService service = new MutationIdempotencyService();

    @Test
    void fingerprintsTheSameCanonicalPayloadDeterministically() {
        assertThat(service.fingerprint("{\"amount\":10}")).isEqualTo(service.fingerprint("{\"amount\":10}"));
        assertThat(service.fingerprint("{\"amount\":10}")).hasSize(64);
    }

    @Test
    void requiresKeysOnlyForPoliciesThatNeedThem() {
        MutationOperationPolicy optional = new MutationOperationPolicy("read_like_command", false, false, false);
        assertThat(service.requireKey(null, optional)).isNull();
        assertThatThrownBy(() -> service.requireKey(null, MutationOperationPolicy.idempotentCreate("quest.create")))
                .isInstanceOfSatisfying(CodedResponseStatusException.class,
                        exception -> assertThat(exception.getCode()).isEqualTo("IDEMPOTENCY_KEY_REQUIRED"));
    }

    @Test
    void rejectsPayloadFingerprintMismatch() {
        assertThatThrownBy(() -> service.requireSameFingerprint("one", "two"))
                .isInstanceOfSatisfying(CodedResponseStatusException.class,
                        exception -> assertThat(exception.getCode()).isEqualTo("IDEMPOTENCY_PAYLOAD_MISMATCH"));
    }
}
