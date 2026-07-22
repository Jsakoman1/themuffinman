package com.themuffinman.app.common.observability;

import com.themuffinman.app.config.RuntimeObservabilityProperties;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * Dev-only runtime evidence headers. The values are intentionally opt-in because
 * Hibernate statistics are a measurement aid, not a production monitoring system.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class RuntimeObservabilityFilter extends OncePerRequestFilter {

    private static final String QUERY_COUNT = "X-Runtime-Query-Count";
    private static final String REQUEST_DURATION = "X-Runtime-Request-Duration-Ms";

    private final EntityManagerFactory entityManagerFactory;
    private final RuntimeObservabilityProperties properties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        long queryCountBefore = statistics.getQueryExecutionCount();
        long statementCountBefore = statistics.getPrepareStatementCount();
        long startedAt = System.nanoTime();
        ContentCachingResponseWrapper measuredResponse = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, measuredResponse);
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000L;
            long queryCount = Math.max(0L, statistics.getQueryExecutionCount() - queryCountBefore);
            long statementCount = Math.max(0L, statistics.getPrepareStatementCount() - statementCountBefore);
            measuredResponse.setHeader(QUERY_COUNT, Long.toString(Math.max(queryCount, statementCount)));
            measuredResponse.setHeader(REQUEST_DURATION, Long.toString(durationMs));
            measuredResponse.copyBodyToResponse();
        }
    }
}
