package com.indigo.synapse.webflux.filter;

import com.indigo.synapse.core.context.OperationContextPropagationKeys;
import com.indigo.synapse.webflux.context.ReactiveRequestContext;
import com.indigo.synapse.webflux.context.RequestContext;
import com.indigo.synapse.web.core.trace.TraceHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SynapseWebFluxContextFilterTest {

    private final SynapseWebFluxContextFilter filter = new SynapseWebFluxContextFilter();

    @Test
    void shouldWriteTraceAndRequestContextToReactorContext() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/items")
                .header(TraceHeaders.TRACE_ID, "trace-1")
                .header(TraceHeaders.REQUEST_ID, "request-1")
                .build());

        Mono<Void> result = filter.filter(exchange, currentExchange -> Mono.deferContextual(contextView -> {
            RequestContext requestContext = ReactiveRequestContext.requestContext(contextView).orElseThrow();
            assertEquals("trace-1", ReactiveRequestContext.traceId(contextView).orElseThrow());
            assertEquals("request-1", ReactiveRequestContext.requestId(contextView).orElseThrow());
            assertEquals("GET", requestContext.method());
            assertEquals("/items", requestContext.path());
            return Mono.empty();
        }));

        StepVerifier.create(result).verifyComplete();
        assertEquals("trace-1", exchange.getResponse().getHeaders().getFirst(TraceHeaders.TRACE_ID));
        assertEquals("request-1", exchange.getResponse().getHeaders().getFirst(TraceHeaders.REQUEST_ID));
    }

    @Test
    void shouldIgnoreUntrustedIdentityHeaders() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/items")
                .header(OperationContextPropagationKeys.ACTOR_TYPE, "USER")
                .header(OperationContextPropagationKeys.ACTOR_ID, "forged-user")
                .header(OperationContextPropagationKeys.TENANT_ID, "forged-tenant")
                .header(OperationContextPropagationKeys.INITIATOR_ID, "forged-initiator")
                .build());

        Mono<Void> result = filter.filter(exchange, currentExchange -> Mono.deferContextual(contextView -> {
            assertFalse(contextView.hasKey("synapse.operationContextSnapshot"));
            return Mono.empty();
        }));

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void shouldOnlyExposeTechnicalRequestContext() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/items").build());

        Mono<Void> result = filter.filter(exchange, currentExchange -> Mono.deferContextual(contextView -> {
            assertEquals("GET", ReactiveRequestContext.requestContext(contextView).orElseThrow().method());
            assertFalse(contextView.hasKey("synapse.operationContextSnapshot"));
            return Mono.empty();
        }));

        StepVerifier.create(result).verifyComplete();
    }
}
