package com.themuffinman.app.storage;

public interface ObjectStorageService {

    StoredObject store(String storageKey, String contentType, byte[] content);

    ObjectStorageAccess resolve(String storageKey);

    StoredObjectPayload load(String storageKey);
}
