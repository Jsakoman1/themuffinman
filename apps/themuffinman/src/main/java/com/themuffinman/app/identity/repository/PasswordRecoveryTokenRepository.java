package com.themuffinman.app.identity.repository;

import com.themuffinman.app.identity.model.PasswordRecoveryToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordRecoveryTokenRepository extends JpaRepository<PasswordRecoveryToken, Long> {
    Optional<PasswordRecoveryToken> findByTokenHash(String tokenHash);

    void deleteByUserIdAndConsumedAtIsNull(Long userId);

    void deleteByExpiresAtBefore(Instant instant);
}
