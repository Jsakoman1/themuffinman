package com.themuffinman.app.common.request;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MutationRequestContextFilterTest {

    private final MutationRequestContextFilter filter = new MutationRequestContextFilter();

    @AfterEach
    void clearContext() {
        MutationRequestContext.clear();
    }

    @Test
    void generatesRequestAndCorrelationIdsAndReturnsThemInResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/quests");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(MutationRequestContext.REQUEST_ID_HEADER)).isNotBlank();
        assertThat(response.getHeader(MutationRequestContext.CORRELATION_ID_HEADER))
                .isEqualTo(response.getHeader(MutationRequestContext.REQUEST_ID_HEADER));
        verify(chain).doFilter(request, response);
        assertThat(MutationRequestContext.current()).isEmpty();
    }

    @Test
    void preservesSafeClientIdentifiersAndDropsUnsafeHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/quests/1");
        request.addHeader(MutationRequestContext.REQUEST_ID_HEADER, "client.request-1");
        request.addHeader(MutationRequestContext.CORRELATION_ID_HEADER, "trace:1");
        request.addHeader(MutationRequestContext.IDEMPOTENCY_KEY_HEADER, "booking-key-1");
        request.addHeader(MutationRequestContext.OPERATION_KEY_HEADER, "booking.create");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertThat(response.getHeader(MutationRequestContext.REQUEST_ID_HEADER)).isEqualTo("client.request-1");
        assertThat(response.getHeader(MutationRequestContext.CORRELATION_ID_HEADER)).isEqualTo("trace:1");
        assertThat(MutationRequestContext.current()).isEmpty();
    }

    @Test
    void clearsRequestContextAndMdcWhenTheMutationFails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/bookings");
        request.addHeader(MutationRequestContext.CORRELATION_ID_HEADER, "booking.trace-1");
        request.addHeader(MutationRequestContext.IDEMPOTENCY_KEY_HEADER, "booking-key-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (currentRequest, currentResponse) -> {
            assertThat(MutationRequestContext.correlationId()).isEqualTo("booking.trace-1");
            assertThat(MutationRequestContext.current()).get()
                    .extracting(MutationRequestContext.Snapshot::idempotencyKey)
                    .isEqualTo("booking-key-1");
            assertThat(MDC.get("correlationId")).isEqualTo("booking.trace-1");
            throw new IllegalStateException("simulated mutation failure");
        };

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("simulated mutation failure");

        assertThat(response.getHeader(MutationRequestContext.CORRELATION_ID_HEADER)).isEqualTo("booking.trace-1");
        assertThat(MutationRequestContext.current()).isEmpty();
        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("correlationId")).isNull();
        assertThat(MDC.get("idempotencyKey")).isNull();
        assertThat(MDC.get("operationKey")).isNull();
    }
}
