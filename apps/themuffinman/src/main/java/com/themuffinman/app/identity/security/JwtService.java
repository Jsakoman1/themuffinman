package com.themuffinman.app.identity.security;

import com.themuffinman.app.identity.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;

@Service
public class JwtService {

    private final String secret;
    private final long expirationTimeInMillis;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-millis:86400000}") long expirationTimeInMillis
    ) {
        this.secret = secret;
        this.expirationTimeInMillis = expirationTimeInMillis;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AppUser appUser) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(appUser.getEmail())
                .claim("userId", appUser.getId())
                .claim("username", appUser.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationTimeInMillis)))
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
