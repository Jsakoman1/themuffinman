package com.themuffinman.app.storage;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.ObjectStorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@ConditionalOnProperty(prefix = "app.object-storage", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "app.object-storage", name = "provider", havingValue = "local")
public class LocalObjectStorageService implements ObjectStorageService {

    private static final String PROVIDER = "local";
    private static final String CONTENT_TYPE_SUFFIX = ".content-type";

    private final ObjectStorageProperties properties;

    public LocalObjectStorageService(ObjectStorageProperties properties) {
        this.properties = properties;
        if (!"local".equalsIgnoreCase(properties.getProvider())) {
            throw new IllegalStateException("LocalObjectStorageService requires app.object-storage.provider=local");
        }
    }

    @Override
    public StoredObject store(String storageKey, String contentType, byte[] content) {
        Path objectPath = resolvePath(storageKey);
        Path contentTypePath = contentTypePath(storageKey);
        try {
            Files.createDirectories(objectPath.getParent());
            Files.write(objectPath, content);
            Files.writeString(contentTypePath, contentType, StandardCharsets.UTF_8);
            return new StoredObject(PROVIDER, storageKey);
        } catch (IOException exception) {
            throw ServiceErrors.serviceUnavailable("Local chat attachment storage failed");
        }
    }

    @Override
    public ObjectStorageAccess resolve(String storageKey) {
        String baseUrl = properties.getLocalProxyBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "/chat/attachments/object";
        }
        String separator = baseUrl.contains("?") ? "&" : "?";
        return new ObjectStorageAccess(PROVIDER, storageKey, baseUrl + separator + "key=" + URLEncoder.encode(storageKey, StandardCharsets.UTF_8), null);
    }

    @Override
    public StoredObjectPayload load(String storageKey) {
        Path objectPath = resolvePath(storageKey);
        Path contentTypePath = contentTypePath(storageKey);
        try {
            if (!Files.exists(objectPath)) {
                throw ServiceErrors.notFound("Chat attachment object not found");
            }
            String contentType = Files.exists(contentTypePath)
                    ? Files.readString(contentTypePath, StandardCharsets.UTF_8).trim()
                    : "application/octet-stream";
            return new StoredObjectPayload(PROVIDER, storageKey, contentType, Files.readAllBytes(objectPath));
        } catch (IOException exception) {
            throw ServiceErrors.serviceUnavailable("Local chat attachment object could not be loaded");
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolvePath(storageKey));
            Files.deleteIfExists(contentTypePath(storageKey));
        } catch (IOException exception) {
            throw ServiceErrors.serviceUnavailable("Local gallery image cleanup failed");
        }
    }

    private Path resolvePath(String storageKey) {
        Path basePath = Path.of(properties.getLocalBasePath()).toAbsolutePath().normalize();
        Path resolvedPath = basePath.resolve(storageKey).normalize();
        if (!resolvedPath.startsWith(basePath)) {
            throw ServiceErrors.badRequest("Attachment storage key is invalid");
        }
        return resolvedPath;
    }

    private Path contentTypePath(String storageKey) {
        Path objectPath = resolvePath(storageKey);
        return objectPath.resolveSibling(objectPath.getFileName() + CONTENT_TYPE_SUFFIX);
    }
}
