package com.indigo.synapse.webflux.filter;

import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContextSnapshot;
import com.indigo.synapse.webflux.context.OperationContextHeaders;
import com.indigo.synapse.webflux.context.OperationContextWebFluxCodec;
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

    private final SynapseWebFluxContextFilter filter =
            new SynapseWebFluxContextFilter(new OperationContextWebFluxCodec());

    @Test
    void shouldWriteTraceAndRequestContextToReactorContext() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/items")
                .header(TraceHeaders.TRACE_ID, "trace-1")
                .header(TraceHeaders.REQUEST_ID, "request-1")
                .header(OperationContextHeaders.ACTOR_TYPE, "SERVICE")
                .header(OperationContextHeaders.ACTOR_ID, "service-a")
                .header(OperationContextHeaders.ACTOR_NAME, "Service A")
                .header(OperationContextHeaders.TENANT_ID, "tenant-a")
                .build());

        Mono<Void> result = filter.filter(exchange, currentExchange -> Mono.deferContextual(contextView -> {
            RequestContext requestContext = ReactiveRequestContext.requestContext(contextView).orElseThrow();
            OperationContextSnapshot snapshot = ReactiveRequestContext.operationContextSnapshot(contextView).orElseThrow();

            assertEquals("trace-1", ReactiveRequestContext.traceId(contextView).orElseThrow());
            assertEquals("request-1", ReactiveRequestContext.requestId(contextView).orElseThrow());
            assertEquals("GET", requestContext.method());
            assertEquals("/items", requestContext.path());
            assertEquals(OperationActorType.SERVICE, snapshot.context().actor().type());
            assertEquals("service-a", snapshot.context().actor().id());
            assertEquals("tenant-a", snapshot.context().tenantId());
            return Mono.empty();
        }));

        StepVerifier.create(result).verifyComplete();
        assertEquals("trace-1", exchange.getResponse().getHeaders().getFirst(TraceHeaders.TRACE_ID));
        assertEquals("request-1", exchange.getResponse().getHeaders().getFirst(TraceHeaders.REQUEST_ID));
    }

    @Test
    void shouldNotCreateOperationContextWhenActorHeaderMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/items").build());

        Mono<Void> result = filter.filter(exchange, currentExchange -> Mono.deferContextual(contextView -> {
            assertFalse(ReactiveRequestContext.operationContextSnapshot(contextView).isPresent());
            return Mono.empty();
        }));

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void shouldNotDefaultActorTypeWhenActorTypeHeaderMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/items")
                .header(OperationContextHeaders.ACTOR_ID, "service-a")
                .header(OperationContextHeaders.TENANT_ID, "tenant-a")
                .build());

        Mono<Void> result = filter.filter(exchange, currentExchange -> Mono.deferContextual(contextView -> {
            assertFalse(ReactiveRequestContext.operationContextSnapshot(contextView).isPresent());
            return Mono.empty();
        }));

        StepVerifier.create(result).verifyComplete();
    }
}
