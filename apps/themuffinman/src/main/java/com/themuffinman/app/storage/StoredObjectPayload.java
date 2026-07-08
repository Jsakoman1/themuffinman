package com.themuffinman.app.storage;

public record StoredObjectPayload(
        String provider,
        String storageKey,
        String contentType,
        byte[] content
) {
}
