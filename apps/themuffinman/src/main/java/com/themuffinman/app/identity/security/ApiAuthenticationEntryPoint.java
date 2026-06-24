package com.themuffinman.app.identity.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuffinman.app.dto.ApiErrorResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        String message = authException == null || authException.getMessage() == null || authException.getMessage().isBlank()
                ? "Authentication is required"
                : authException.getMessage();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiErrorResponseDTO.builder()
                .code("UNAUTHORIZED")
                .message(message)
                .fieldErrors(List.of())
                .build());
    }
}
