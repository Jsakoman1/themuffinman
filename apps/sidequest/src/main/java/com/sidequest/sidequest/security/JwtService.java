package com.sidequest.sidequest.security;

import com.sidequest.sidequest.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;

@Service
public class JwtService {

    private static final String SECRET = "sidequest-secret-key-sidequest-secret-key-123456";
    private static final long EXPIRATION_TIME_IN_MILLIS = 864_000_000; // 24h

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AppUser appUser) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(appUser.getEmail())
                .claim("userId", appUser.getId())
                .claim("username", appUser.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(EXPIRATION_TIME_IN_MILLIS)))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}