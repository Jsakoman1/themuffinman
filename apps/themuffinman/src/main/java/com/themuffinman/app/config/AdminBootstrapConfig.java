package com.themuffinman.app.config;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapConfig {

    private final BootstrapProperties bootstrapProperties;

    @Bean
    CommandLineRunner seedDefaultUsers(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (!bootstrapProperties.getSeed().getUsers().isEnabled()) {
                log.info("Skipping seeded default users because app.seed.users.enabled=false");
                return;
            }

            BootstrapProperties.UserCredentials admin = bootstrapProperties.getAdmin();
            BootstrapProperties.UserCredentials test = bootstrapProperties.getTest();
            requireConfiguredUser("app.admin", admin.getEmail(), admin.getUsername(), admin.getPassword());
            requireConfiguredUser("app.test", test.getEmail(), test.getUsername(), test.getPassword());
            seedUser(appUserRepository, passwordEncoder, admin.getEmail(), admin.getUsername(), admin.getPassword(), AppUserRole.ADMIN);
            seedUser(appUserRepository, passwordEncoder, test.getEmail(), test.getUsername(), test.getPassword(), AppUserRole.USER);
        };
    }

    @Bean
    CommandLineRunner bootstrapAdminUser(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            BootstrapProperties.Admin bootstrapAdmin = bootstrapProperties.getBootstrap().getAdmin();
            if (!bootstrapAdmin.isEnabled()) {
                return;
            }

            requireConfiguredUser(
                    "app.bootstrap.admin",
                    bootstrapAdmin.getEmail(),
                    bootstrapAdmin.getUsername(),
                    bootstrapAdmin.getPassword()
            );
            seedUser(
                    appUserRepository,
                    passwordEncoder,
                    bootstrapAdmin.getEmail(),
                    bootstrapAdmin.getUsername(),
                    bootstrapAdmin.getPassword(),
                    AppUserRole.ADMIN
            );
            log.warn("Bootstrap admin account ensured for {}", bootstrapAdmin.getEmail());
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
