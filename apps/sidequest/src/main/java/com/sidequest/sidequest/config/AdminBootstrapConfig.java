package com.sidequest.sidequest.config;

import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.model.AppUserRole;
import com.sidequest.sidequest.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapConfig {

    @Bean
    CommandLineRunner seedAdminUser(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${app.admin.username}") String adminUsername,
            @Value("${app.admin.password}") String adminPassword,
            @Value("${app.test.email}") String testEmail,
            @Value("${app.test.username}") String testUsername,
            @Value("${app.test.password}") String testPassword
    ) {
        return args -> {
            seedUser(appUserRepository, passwordEncoder, adminEmail, adminUsername, adminPassword, AppUserRole.ADMIN);
            seedUser(appUserRepository, passwordEncoder, testEmail, testUsername, testPassword, AppUserRole.USER);
        };
    }

    private void seedUser(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            String email,
            String username,
            String password,
            AppUserRole role
    ) {
        AppUser user = appUserRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new AppUser();
            user.setEmail(email);
        }

        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        appUserRepository.save(user);
        log.info("Seeded {} account: {}", role.name().toLowerCase(), email);
    }
}
