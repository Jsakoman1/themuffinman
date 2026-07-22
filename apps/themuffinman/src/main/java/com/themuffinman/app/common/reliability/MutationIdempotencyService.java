package com.themuffinman.app.common.reliability;

import com.themuffinman.app.common.errors.ServiceErrors;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Shared validation and fingerprint primitives. Persistence and replay storage
 * remain operation-specific until the adoption slice adds them to a mutation.
 */
@Service
public class MutationIdempotencyService {

    public static final int MAX_KEY_LENGTH = 120;

    public String requireKey(String key, MutationOperationPolicy policy) {
        if (key == null || key.isBlank()) {
            if (policy != null && !policy.idempotencyKeyRequired()) {
                return null;
            }
            throw ServiceErrors.badRequest("IDEMPOTENCY_KEY_REQUIRED", "An idempotency key is required for this mutation");
        }
        return normalizeKey(key);
    }

    public String normalizeKey(String key) {
        if (key == null || key.isBlank() || key.length() > MAX_KEY_LENGTH) {
            throw ServiceErrors.badRequest("INVALID_IDEMPOTENCY_KEY", "The idempotency key is invalid");
        }
        return key.trim();
    }

    public String fingerprint(String canonicalPayload) {
        if (canonicalPayload == null) {
            throw ServiceErrors.badRequest("IDEMPOTENCY_PAYLOAD_REQUIRED", "A payload is required to fingerprint a mutation");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalPayload.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public void requireSameFingerprint(String existingFingerprint, String requestedFingerprint) {
        if (existingFingerprint == null || requestedFingerprint == null || !existingFingerprint.equals(requestedFingerprint)) {
            throw ServiceErrors.conflict("IDEMPOTENCY_PAYLOAD_MISMATCH", "The idempotency key was already used with a different payload");
        }
    }
}
