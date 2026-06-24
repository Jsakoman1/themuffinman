package com.themuffinman.app.config;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
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
    CommandLineRunner seedDefaultUsers(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.users.enabled:false}") boolean seedUsersEnabled,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.username:}") String adminUsername,
            @Value("${app.admin.password:}") String adminPassword,
            @Value("${app.test.email:}") String testEmail,
            @Value("${app.test.username:}") String testUsername,
            @Value("${app.test.password:}") String testPassword
    ) {
        return args -> {
            if (!seedUsersEnabled) {
                log.info("Skipping seeded default users because app.seed.users.enabled=false");
                return;
            }

            requireConfiguredUser("app.admin", adminEmail, adminUsername, adminPassword);
            requireConfiguredUser("app.test", testEmail, testUsername, testPassword);
            seedUser(appUserRepository, passwordEncoder, adminEmail, adminUsername, adminPassword, AppUserRole.ADMIN);
            seedUser(appUserRepository, passwordEncoder, testEmail, testUsername, testPassword, AppUserRole.USER);
        };
    }

    @Bean
    CommandLineRunner bootstrapAdminUser(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.enabled:false}") boolean bootstrapAdminEnabled,
            @Value("${app.bootstrap.admin.email:}") String bootstrapAdminEmail,
            @Value("${app.bootstrap.admin.username:}") String bootstrapAdminUsername,
            @Value("${app.bootstrap.admin.password:}") String bootstrapAdminPassword
    ) {
        return args -> {
            if (!bootstrapAdminEnabled) {
                return;
            }

            requireConfiguredUser("app.bootstrap.admin", bootstrapAdminEmail, bootstrapAdminUsername, bootstrapAdminPassword);
            seedUser(
                    appUserRepository,
                    passwordEncoder,
                    bootstrapAdminEmail,
                    bootstrapAdminUsername,
                    bootstrapAdminPassword,
                    AppUserRole.ADMIN
            );
            log.warn("Bootstrap admin account ensured for {}", bootstrapAdminEmail);
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

    private void requireConfiguredUser(String propertyPrefix, String email, String username, String password) {
        if (isBlank(email) || isBlank(username) || isBlank(password)) {
            throw new IllegalStateException(propertyPrefix + " credentials must be configured explicitly");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
