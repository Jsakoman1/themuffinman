package com.themuffinman.app.config;

import com.themuffinman.app.identity.security.ApiAuthenticationEntryPoint;
import com.themuffinman.app.identity.security.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigCorsTest {

    @Test
    void exposesMutationTraceHeadersAndAllowsMutationRequestHeaders() {
        SecurityProperties properties = new SecurityProperties();
        SecurityConfig config = new SecurityConfig(
                mock(JwtAuthFilter.class),
                mock(ApiAuthenticationEntryPoint.class),
                properties
        );

        CorsConfiguration cors = config.corsConfigurationSource()
                .getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest());

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedHeaders()).contains(
                "X-Request-Id",
                "X-Correlation-Id",
                "Idempotency-Key",
                "X-Operation-Key"
        );
        assertThat(cors.getExposedHeaders()).contains(
                "X-Request-Id",
                "X-Correlation-Id",
                "X-Runtime-Query-Count",
                "X-Runtime-Request-Duration-Ms"
        );
    }
}
