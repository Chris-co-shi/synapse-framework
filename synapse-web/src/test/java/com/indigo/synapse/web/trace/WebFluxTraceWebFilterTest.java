package com.indigo.synapse.web.trace;

import com.indigo.synapse.web.context.RequestContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebFluxTraceWebFilterTest {

    @AfterEach
    void tearDown() {
        WebTraceLifecycle.end();
    }

    @Test
    void shouldSetTraceHeaderAndClearContext() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/admin/users")
                        .header(TraceHeaders.TRACE_ID, "trace-webflux")
        );
        WebFilterChain chain = serverWebExchange -> Mono.empty();

        new WebFluxTraceWebFilter().filter(exchange, chain).block();

        assertEquals("trace-webflux", exchange.getResponse().getHeaders().getFirst(TraceHeaders.TRACE_ID));
        assertTrue(TraceContext.currentTraceId().isEmpty());
        assertTrue(RequestContextHolder.current().isEmpty());
    }
}
