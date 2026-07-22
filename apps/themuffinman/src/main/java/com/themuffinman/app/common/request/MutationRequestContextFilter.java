package com.themuffinman.app.common.request;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MutationRequestContextFilter extends OncePerRequestFilter {

    private static final int MAX_IDENTIFIER_LENGTH = 120;
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z0-9._:-]{1,120}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = safeHeader(request.getHeader(MutationRequestContext.REQUEST_ID_HEADER));
        String correlationId = safeHeader(request.getHeader(MutationRequestContext.CORRELATION_ID_HEADER));
        String idempotencyKey = safeHeader(request.getHeader(MutationRequestContext.IDEMPOTENCY_KEY_HEADER));
        String operationKey = safeHeader(request.getHeader(MutationRequestContext.OPERATION_KEY_HEADER));

        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        if (correlationId == null) {
            correlationId = requestId;
        }

        MutationRequestContext.Snapshot snapshot = new MutationRequestContext.Snapshot(
                requestId,
                correlationId,
                idempotencyKey,
                operationKey
        );
        MutationRequestContext.install(snapshot);
        response.setHeader(MutationRequestContext.REQUEST_ID_HEADER, requestId);
        response.setHeader(MutationRequestContext.CORRELATION_ID_HEADER, correlationId);
        putMdc(snapshot);
        try {
            filterChain.doFilter(request, response);
        } finally {
            clearMdc();
            MutationRequestContext.clear();
        }
    }

    private String safeHeader(String value) {
        if (value == null || value.isBlank() || value.length() > MAX_IDENTIFIER_LENGTH) {
            return null;
        }
        return SAFE_IDENTIFIER.matcher(value).matches() ? value : null;
    }

    private void putMdc(MutationRequestContext.Snapshot snapshot) {
        MDC.put("requestId", snapshot.requestId());
        MDC.put("correlationId", snapshot.correlationId());
        if (snapshot.idempotencyKey() != null) {
            MDC.put("idempotencyKey", snapshot.idempotencyKey());
        }
        if (snapshot.operationKey() != null) {
            MDC.put("operationKey", snapshot.operationKey());
        }
    }

    private void clearMdc() {
        MDC.remove("requestId");
        MDC.remove("correlationId");
        MDC.remove("idempotencyKey");
        MDC.remove("operationKey");
    }
}
