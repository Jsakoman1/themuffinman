package com.themuffinman.app.identity.service;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.identity.model.RevokedAuthToken;
import com.themuffinman.app.identity.repository.RevokedAuthTokenRepository;
import com.themuffinman.app.identity.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthSessionService {
    private final RevokedAuthTokenRepository revokedAuthTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public ActionResultDTO revoke(String rawAuthorizationHeader) {
        String token = jwtService.extractBearerToken(rawAuthorizationHeader);
        RevokedAuthToken revokedToken = new RevokedAuthToken();
        revokedToken.setTokenHash(jwtService.hashToken(token));
        revokedToken.setExpiresAt(jwtService.extractExpiration(token));
        revokedAuthTokenRepository.save(revokedToken);
        return ActionResultDTO.builder().action("LOGOUT").message("Session revoked").build();
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(String token) {
        return revokedAuthTokenRepository.existsByTokenHashAndExpiresAtAfter(jwtService.hashToken(token), Instant.now());
    }

    @Transactional
    public int deleteExpiredTokens() {
        Instant now = Instant.now();
        long before = revokedAuthTokenRepository.count();
        revokedAuthTokenRepository.deleteByExpiresAtBefore(now);
        return (int) Math.max(0, before - revokedAuthTokenRepository.count());
    }

    @Scheduled(cron = "${app.retention.auth-tokens.cleanup-cron:0 15 3 * * *}")
    public void scheduledExpiredTokenCleanup() {
        deleteExpiredTokens();
    }
}
