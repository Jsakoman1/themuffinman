package com.themuffinman.app.identity.repository;

import com.themuffinman.app.identity.model.RevokedAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RevokedAuthTokenRepository extends JpaRepository<RevokedAuthToken, Long> {
    boolean existsByTokenHashAndExpiresAtAfter(String tokenHash, Instant now);

    void deleteByExpiresAtBefore(Instant now);
}
