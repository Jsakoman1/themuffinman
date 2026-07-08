package com.themuffinman.app.storage;

import java.time.Instant;

public record ObjectStorageAccess(
        String provider,
        String storageKey,
        String url,
        Instant expiresAt
) {
}
