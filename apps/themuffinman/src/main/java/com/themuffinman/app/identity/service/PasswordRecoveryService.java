package com.themuffinman.app.identity.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.normalization.UserInputNormalizer;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.themuffinman.app.config.AccountRecoveryProperties;
import com.themuffinman.app.identity.dto.auth.PasswordRecoveryRequestDTO;
import com.themuffinman.app.identity.dto.auth.PasswordRecoveryResponseDTO;
import com.themuffinman.app.identity.dto.auth.PasswordResetRequestDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.PasswordRecoveryToken;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.repository.PasswordRecoveryTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PasswordRecoveryService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);

    private final AppUserRepository appUserRepository;
    private final PasswordRecoveryTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final AccountRecoveryProperties properties;
    private final Cache<String, AtomicInteger> requestCounters;

    public PasswordRecoveryService(
            AppUserRepository appUserRepository,
            PasswordRecoveryTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher,
            AccountRecoveryProperties properties) {
        this.appUserRepository = appUserRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.requestCounters = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(Math.max(properties.getWindowSeconds(), 1)))
                .maximumSize(50_000)
                .build();
    }

    /**
     * Always returns the same accepted response so callers cannot enumerate accounts.
     * The event is the explicit handoff boundary for a future email delivery provider.
     */
    @Transactional
    public PasswordRecoveryResponseDTO request(PasswordRecoveryRequestDTO request) {
        return request(request, "anonymous");
    }

    @Transactional
    public PasswordRecoveryResponseDTO request(PasswordRecoveryRequestDTO request, String sourceKey) {
        String email = UserInputNormalizer.normalizeEmail(request.email());
        assertWithinLimit(email, sourceKey);
        appUserRepository.findByEmail(email).ifPresent(user -> {
            tokenRepository.deleteByUserIdAndConsumedAtIsNull(user.getId());
            String rawToken = randomToken();
            PasswordRecoveryToken token = new PasswordRecoveryToken();
            token.setUser(user);
            token.setTokenHash(hash(rawToken));
            token.setExpiresAt(Instant.now().plus(TOKEN_TTL));
            PasswordRecoveryToken saved = tokenRepository.save(token);
            eventPublisher.publishEvent(new PasswordRecoveryRequestedEvent(
                    saved.getId(), email, rawToken, saved.getExpiresAt()));
        });
        return new PasswordRecoveryResponseDTO(true);
    }

    private void assertWithinLimit(String email, String sourceKey) {
        int limit = properties.getRequestsPerWindow();
        if (limit <= 0) return;
        String normalizedSource = sourceKey == null || sourceKey.isBlank() ? "anonymous" : sourceKey.trim();
        String key = hash(email + "::" + normalizedSource);
        AtomicInteger counter = requestCounters.get(key, ignored -> new AtomicInteger());
        if (counter.incrementAndGet() > limit) {
            throw ServiceErrors.tooManyRequests("Too many recovery requests. Try again later.");
        }
    }

    @Transactional
    public void reset(PasswordResetRequestDTO request) {
        PasswordRecoveryToken token = tokenRepository.findByTokenHash(hash(request.token()))
                .orElseThrow(() -> ServiceErrors.badRequest("Invalid or expired password recovery token"));
        Instant now = Instant.now();
        if (token.getConsumedAt() != null || !token.getExpiresAt().isAfter(now)) {
            throw ServiceErrors.badRequest("Invalid or expired password recovery token");
        }
        AppUser user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        token.setConsumedAt(now);
        tokenRepository.save(token);
        appUserRepository.save(user);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record PasswordRecoveryRequestedEvent(Long requestId, String email, String token, Instant expiresAt) {
    }
}
