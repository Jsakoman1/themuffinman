package com.themuffinman.app.chat.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.themuffinman.app.chat.model.ChatAuditEventType;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRateLimitService {

    private final ChatAuditService chatAuditService;

    private final Cache<String, AtomicInteger> perMinuteCounters = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(1))
            .maximumSize(10_000)
            .build();

    public void assertWithinPerMinuteLimit(String actionKey, String actorKey, int limit, String message) {
        if (limit <= 0) {
            return;
        }
        String normalizedActorKey = actorKey == null || actorKey.isBlank() ? "anonymous" : TextValueNormalizer.lowerTrimToEmpty(actorKey);
        String cacheKey = actionKey + "::" + normalizedActorKey;
        AtomicInteger counter = perMinuteCounters.get(cacheKey, ignored -> new AtomicInteger());
        int current = counter.incrementAndGet();
        if (current <= limit) {
            return;
        }

        log.warn("Chat rate limited action={} actor={}", actionKey, normalizedActorKey);
        chatAuditService.record(
                ChatAuditEventType.RATE_LIMIT_EXCEEDED,
                null,
                null,
                null,
                null,
                null,
                Map.of("actionKey", actionKey, "actorKey", normalizedActorKey, "limit", limit)
        );
        throw ServiceErrors.badRequest(message);
    }
}
