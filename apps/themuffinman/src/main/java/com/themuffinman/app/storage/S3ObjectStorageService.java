package com.themuffinman.app.storage;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.ObjectStorageProperties;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;

@Service
@ConditionalOnProperty(prefix = "app.object-storage", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "app.object-storage", name = "provider", havingValue = "s3", matchIfMissing = true)
public class S3ObjectStorageService implements ObjectStorageService {

    private final ObjectStorageProperties properties;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3ObjectStorageService(ObjectStorageProperties properties) {
        this.properties = properties;
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(
                required(properties.getAccessKey(), "Object storage access key"),
                required(properties.getSecretKey(), "Object storage secret key")
        ));
        Region region = Region.of(required(properties.getRegion(), "Object storage region"));
        S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(properties.isPathStyleAccess())
                .build();
        var clientBuilder = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(s3Configuration);
        var presignerBuilder = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(s3Configuration);
        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            URI endpoint = URI.create(properties.getEndpoint().trim());
            clientBuilder = clientBuilder.endpointOverride(endpoint);
            presignerBuilder = presignerBuilder.endpointOverride(endpoint);
        }
        this.s3Client = clientBuilder.build();
        this.s3Presigner = presignerBuilder.build();
    }

    @Override
    public StoredObject store(String storageKey, String contentType, byte[] content) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(required(properties.getBucket(), "Object storage bucket"))
                            .key(storageKey)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content)
            );
            return new StoredObject(properties.getProvider(), storageKey);
        } catch (RuntimeException exception) {
            throw ServiceErrors.serviceUnavailable("Chat attachment upload failed");
        }
    }

    @Override
    public ObjectStorageAccess resolve(String storageKey) {
        if (properties.getPublicBaseUrl() != null && !properties.getPublicBaseUrl().isBlank()) {
            String baseUrl = properties.getPublicBaseUrl().endsWith("/")
                    ? properties.getPublicBaseUrl().substring(0, properties.getPublicBaseUrl().length() - 1)
                    : properties.getPublicBaseUrl();
            return new ObjectStorageAccess(properties.getProvider(), storageKey, baseUrl + "/" + storageKey, null);
        }
        long ttlSeconds = Math.max(properties.getPresignedUrlTtlSeconds(), 60);
        Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);
        try {
            String url = s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofSeconds(ttlSeconds))
                            .getObjectRequest(GetObjectRequest.builder()
                                    .bucket(required(properties.getBucket(), "Object storage bucket"))
                                    .key(storageKey)
                                    .build())
                            .build())
                    .url()
                    .toString();
            return new ObjectStorageAccess(properties.getProvider(), storageKey, url, expiresAt);
        } catch (RuntimeException exception) {
            throw ServiceErrors.serviceUnavailable("Chat attachment access could not be resolved");
        }
    }

    @Override
    public StoredObjectPayload load(String storageKey) {
        try {
            var responseBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(required(properties.getBucket(), "Object storage bucket"))
                    .key(storageKey)
                    .build());
            String contentType = responseBytes.response().contentType();
            return new StoredObjectPayload(
                    properties.getProvider(),
                    storageKey,
                    contentType == null || contentType.isBlank() ? detectContentType(storageKey) : contentType,
                    responseBytes.asByteArray()
            );
        } catch (NoSuchKeyException exception) {
            throw ServiceErrors.notFound("Chat attachment object not found");
        } catch (RuntimeException exception) {
            throw ServiceErrors.serviceUnavailable("Chat attachment object could not be loaded");
        }
    }

    @PreDestroy
    public void close() {
        s3Client.close();
        s3Presigner.close();
    }

    private static String required(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(label + " must be configured when external object storage is enabled");
        }
        return value.trim();
    }

    private String detectContentType(String storageKey) {
        String lowerCaseKey = storageKey == null ? "" : storageKey.toLowerCase();
        if (lowerCaseKey.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (lowerCaseKey.endsWith(".json")) {
            return "application/json";
        }
        if (lowerCaseKey.endsWith(".txt")) {
            return "text/plain";
        }
        if (lowerCaseKey.endsWith(".png")) {
            return "image/png";
        }
        if (lowerCaseKey.endsWith(".jpg") || lowerCaseKey.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lowerCaseKey.endsWith(".webp")) {
            return "image/webp";
        }
        if (lowerCaseKey.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }
}
