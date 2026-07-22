package com.themuffinman.app.config;

import com.themuffinman.app.identity.security.ApiAuthenticationEntryPoint;
import com.themuffinman.app.identity.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ApiAuthenticationEntryPoint authenticationEntryPoint;
    private final SecurityProperties securityProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/register", "/auth/login", "/auth/password-recovery", "/auth/password-reset").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/auth/logout").authenticated()
                        .requestMatchers("/ws/chat/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/app_users/me").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/app_users/me").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/app_users/*/profile-view").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/app_users/*").authenticated()
                        .requestMatchers("/chat/admin/**", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/chat/**").authenticated()
                        .requestMatchers("/quests/**").authenticated()
                        .requestMatchers("/app_users/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(securityProperties.getCors().getAllowedOrigins().stream()
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList());
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Request-Id",
                "X-Correlation-Id",
                "Idempotency-Key",
                "X-Operation-Key"
        ));
        configuration.setExposedHeaders(java.util.List.of(
                "Authorization",
                "X-Request-Id",
                "X-Correlation-Id",
                "X-Runtime-Query-Count",
                "X-Runtime-Request-Duration-Ms"
        ));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
