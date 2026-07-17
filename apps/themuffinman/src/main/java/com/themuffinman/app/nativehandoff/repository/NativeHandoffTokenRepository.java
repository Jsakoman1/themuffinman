package com.themuffinman.app.nativehandoff.repository;

import com.themuffinman.app.nativehandoff.model.NativeHandoffToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NativeHandoffTokenRepository extends JpaRepository<NativeHandoffToken, Long> {
    Optional<NativeHandoffToken> findByTokenHash(String tokenHash);
}
