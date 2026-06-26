package com.themuffinman.app.identity.security;

import com.themuffinman.app.config.SecurityProperties;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtServiceTest {

    private static final String SECRET = "sidequest-test-secret-sidequest-test-secret-123456";

    @Test
    void generateTokenIncludesUserEmailAsSubject() {
        SecurityProperties securityProperties = new SecurityProperties();
        securityProperties.getJwt().setSecret(SECRET);
        securityProperties.getJwt().setExpirationMillis(86_400_000L);
        JwtService jwtService = new JwtService(securityProperties);
        AppUser user = new AppUser();
        user.setId(42L);
        user.setEmail("user@example.com");
        user.setUsername("user");

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("user@example.com", jwtService.extractEmail(token));
    }
}
