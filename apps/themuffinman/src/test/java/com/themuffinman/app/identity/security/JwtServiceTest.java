package com.themuffinman.app.identity.security;

import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtServiceTest {

    private static final String SECRET = "sidequest-test-secret-sidequest-test-secret-123456";

    @Test
    void generateTokenIncludesUserEmailAsSubject() {
        JwtService jwtService = new JwtService(SECRET, 86_400_000L);
        AppUser user = new AppUser();
        user.setId(42L);
        user.setEmail("user@example.com");
        user.setUsername("user");

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("user@example.com", jwtService.extractEmail(token));
    }
}
