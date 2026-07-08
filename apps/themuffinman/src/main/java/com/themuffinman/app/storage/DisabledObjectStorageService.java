package com.themuffinman.app.storage;

import com.themuffinman.app.common.errors.ServiceErrors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.object-storage", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledObjectStorageService implements ObjectStorageService {

    @Override
    public StoredObject store(String storageKey, String contentType, byte[] content) {
        throw ServiceErrors.serviceUnavailable("External object storage is not configured");
    }

    @Override
    public ObjectStorageAccess resolve(String storageKey) {
        throw ServiceErrors.serviceUnavailable("External object storage is not configured");
    }

    @Override
    public StoredObjectPayload load(String storageKey) {
        throw ServiceErrors.serviceUnavailable("External object storage is not configured");
    }
}
