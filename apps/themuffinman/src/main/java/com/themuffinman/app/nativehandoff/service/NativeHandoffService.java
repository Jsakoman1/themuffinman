package com.themuffinman.app.nativehandoff.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.NativeHandoffProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.nativehandoff.dto.NativeHandoffConsumeRequestDTO;
import com.themuffinman.app.nativehandoff.dto.NativeHandoffIssueRequestDTO;
import com.themuffinman.app.nativehandoff.dto.NativeHandoffIssueResponseDTO;
import com.themuffinman.app.nativehandoff.model.NativeHandoffToken;
import com.themuffinman.app.nativehandoff.repository.NativeHandoffTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NativeHandoffService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final NativeHandoffTokenRepository repository;
    private final NativeHandoffProperties properties;

    @Transactional
    public NativeHandoffIssueResponseDTO issue(NativeHandoffIssueRequestDTO request, AppUser user) {
        if (user == null) throw ServiceErrors.forbidden("Authentication is required");
        String token = randomToken();
        NativeHandoffToken handoff = new NativeHandoffToken();
        handoff.setUser(user);
        handoff.setTokenHash(hash(token));
        handoff.setTargetDevice(normalize(request.getTargetDevice()));
        handoff.setIntent(normalize(request.getIntent()));
        handoff.setResourceReference(normalize(request.getResourceReference()));
        handoff.setRedactedContext(request.getRedactedContext());
        handoff.setNonce(UUID.randomUUID().toString());
        handoff.setExpiresAt(Instant.now().plusSeconds(Math.max(properties.getTtlSeconds(), 1)));
        repository.save(handoff);
        return NativeHandoffIssueResponseDTO.builder()
                .token(token)
                .targetDevice(handoff.getTargetDevice())
                .intent(handoff.getIntent())
                .resourceReference(handoff.getResourceReference())
                .expiresAt(handoff.getExpiresAt())
                .contractVersion("native-handoff-v1")
                .build();
    }

    @Transactional
    public NativeHandoffToken consume(NativeHandoffConsumeRequestDTO request, AppUser user) {
        if (user == null) throw ServiceErrors.forbidden("Authentication is required");
        NativeHandoffToken handoff = repository.findByTokenHash(hash(request.getToken()))
                .orElseThrow(() -> ServiceErrors.notFound("Handoff token not found"));
        if (!handoff.getUser().getId().equals(user.getId())) throw ServiceErrors.forbidden("Handoff token user mismatch");
        if (!handoff.getTargetDevice().equalsIgnoreCase(normalize(request.getTargetDevice()))) throw ServiceErrors.forbidden("Handoff target mismatch");
        if (handoff.getConsumedAt() != null) throw ServiceErrors.conflict("Handoff token has already been consumed");
        if (!handoff.getExpiresAt().isAfter(Instant.now())) throw ServiceErrors.conflict("Handoff token has expired");
        handoff.setConsumedAt(Instant.now());
        return repository.save(handoff);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().isBlank() ? null : value.trim();
    }
}
