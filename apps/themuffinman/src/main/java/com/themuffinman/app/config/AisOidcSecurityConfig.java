package com.themuffinman.app.config;

import java.util.ArrayList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import com.themuffinman.app.identity.repository.AppUserRepository;

@Configuration
@EnableConfigurationProperties(AisOidcProperties.class)
@ConditionalOnProperty(name = "app.ais.oidc.enabled", havingValue = "true")
public class AisOidcSecurityConfig {
    @Bean
    JwtDecoder aisJwtDecoder(AisOidcProperties properties) {
        if (properties.getIssuer() == null || properties.getIssuer().isBlank()
                || properties.getAudience() == null || properties.getAudience().isBlank()
                || properties.getJwkSetUri() == null || properties.getJwkSetUri().isBlank()) {
            throw new IllegalStateException("AIS OIDC requires explicit issuer, audience and JWKS URI");
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(properties.getJwkSetUri()).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.getIssuer()), jwt ->
                    jwt.getExpiresAt() != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()
                    && jwt.getAudience().contains(properties.getAudience())
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"))));
        return decoder;
    }

    @Bean
    @Order(1)
    SecurityFilterChain aisOidcReadChain(HttpSecurity http, JwtDecoder aisJwtDecoder,
            AisOidcProperties properties, AppUserRepository accounts) throws Exception {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        http.securityMatcher("/ais/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(requests -> requests
                .requestMatchers(HttpMethod.GET, "/ais/chat-status").hasAuthority("SCOPE_ais.chat-status.read")
                .anyRequest().denyAll())
            .oauth2ResourceServer(resource -> resource.jwt(jwt -> jwt.decoder(aisJwtDecoder)
                .jwtAuthenticationConverter(token -> {
                    Long accountId = properties.getSubjectAccountIds().get(token.getSubject());
                    if (accountId == null) throw unlinked();
                    var account = accounts.findById(accountId).orElseThrow(AisOidcSecurityConfig::unlinked);
                    var authorities = new ArrayList<>(scopes.convert(token));
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + (account.getRole() == null ? "USER" : account.getRole().name())));
                    // Domain identity is resolved only from an explicit local account binding.
                    // Token email/roles never choose the account or household permissions.
                    return new UsernamePasswordAuthenticationToken(account, null, authorities);
                })));
        return http.build();
    }

    private static OAuth2AuthenticationException unlinked() {
        return new OAuth2AuthenticationException(new OAuth2Error("invalid_token"));
    }
}
