package com.indigo.synapse.oauth2.resource.webflux.gatewayproof;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.security.autoconfigure.SynapseSecurityProperties;
import com.indigo.synapse.security.gatewayproof.GatewayProof;
import com.indigo.synapse.security.gatewayproof.GatewayProofCanonicalRequest;
import com.indigo.synapse.security.gatewayproof.GatewayProofHeaders;
import com.indigo.synapse.security.gatewayproof.GatewayProofTokenHasher;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerificationResult;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerificationStatus;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerifier;
import com.indigo.synapse.webflux.exception.CommonErrorHttpStatusResolver;
import com.indigo.synapse.webflux.exception.CompositeErrorHttpStatusResolver;
import com.indigo.synapse.webflux.exception.ReactiveWebErrorResponseWriter;
import com.indigo.synapse.webflux.exception.WebFluxExceptionResponseFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayProofWebFilterTest {

    @Test
    void shouldContinueWhenGatewayProofIsValid() {
        RecordingVerifier verifier = new RecordingVerifier(GatewayProofVerificationResult.success());
        GatewayProofWebFilter filter = filter(verifier, properties(true, true));
        ServerWebExchange exchange = exchange("/api/items?b=2&a=1", "Bearer token-a", true);
        RecordingChain chain = new RecordingChain();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(chain.called.get()).isTrue();
        assertThat(verifier.request.method()).isEqualTo("GET");
        assertThat(verifier.request.path()).isEqualTo("/api/items");
        assertThat(verifier.request.query()).isEqualTo("b=2&a=1");
        assertThat(verifier.request.bearerTokenHash()).isEqualTo(new GatewayProofTokenHasher().sha256Hex("token-a"));
    }

    @Test
    void shouldRejectWhenGatewayProofIsMissing() {
        GatewayProofWebFilter filter = filter(
                new RecordingVerifier(GatewayProofVerificationResult.failure(GatewayProofVerificationStatus.MISSING, "missing")),
                properties(true, true)
        );
        ServerWebExchange exchange = exchange("/api/items", "Bearer token-a", false);
        RecordingChain chain = new RecordingChain();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(chain.called.get()).isFalse();
    }

    @Test
    void shouldSkipPermitPath() {
        RecordingVerifier verifier = new RecordingVerifier(GatewayProofVerificationResult.failure(
                GatewayProofVerificationStatus.MISSING, "missing"
        ));
        GatewayProofWebFilter filter = filter(verifier, properties(true, true));
        ServerWebExchange exchange = exchange("/actuator/health", null, false);
        RecordingChain chain = new RecordingChain();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(chain.called.get()).isTrue();
        assertThat(verifier.proof).isNull();
    }

    @Test
    void shouldUseEmptyTokenHashWhenAuthorizationHeaderIsMalformed() {
        RecordingVerifier verifier = new RecordingVerifier(GatewayProofVerificationResult.success());
        GatewayProofWebFilter filter = filter(verifier, properties(true, true));
        ServerWebExchange exchange = exchange("/api/items", "Basic abc", true);
        RecordingChain chain = new RecordingChain();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(chain.called.get()).isTrue();
        assertThat(verifier.request.bearerTokenHash()).isEmpty();
    }

    private static GatewayProofWebFilter filter(
            GatewayProofVerifier verifier,
            SynapseSecurityProperties.GatewayProof properties
    ) {
        return new GatewayProofWebFilter(
                properties,
                verifier,
                new GatewayProofTokenHasher(),
                new GatewayProofServerAccessDeniedHandler(
                        new WebFluxExceptionResponseFactory(new CompositeErrorHttpStatusResolver(
                                List.of(new CommonErrorHttpStatusResolver())
                        )),
                        new ReactiveWebErrorResponseWriter(new ObjectMapper().findAndRegisterModules())
                )
        );
    }

    private static SynapseSecurityProperties.GatewayProof properties(boolean enabled, boolean required) {
        SynapseSecurityProperties.GatewayProof properties = new SynapseSecurityProperties.GatewayProof();
        properties.setEnabled(enabled);
        properties.setRequired(required);
        return properties;
    }

    private static ServerWebExchange exchange(String uri, String authorization, boolean proof) {
        MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.get(uri);
        if (authorization != null) {
            builder.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        if (proof) {
            builder.header(GatewayProofHeaders.VERSION, "v1");
            builder.header(GatewayProofHeaders.GATEWAY_ID, "synapse-gateway");
            builder.header(GatewayProofHeaders.TIMESTAMP, "1700000000000");
            builder.header(GatewayProofHeaders.NONCE, "nonce-1");
            builder.header(GatewayProofHeaders.SIGNATURE, "signature");
        }
        return MockServerWebExchange.from(builder);
    }

    private static final class RecordingVerifier implements GatewayProofVerifier {

        private final GatewayProofVerificationResult result;
        private GatewayProof proof;
        private GatewayProofCanonicalRequest request;

        private RecordingVerifier(GatewayProofVerificationResult result) {
            this.result = result;
        }

        @Override
        public GatewayProofVerificationResult verify(GatewayProof proof, GatewayProofCanonicalRequest request) {
            this.proof = proof;
            this.request = request;
            return result;
        }
    }

    private static final class RecordingChain implements WebFilterChain {

        private final AtomicBoolean called = new AtomicBoolean();

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            called.set(true);
            return Mono.empty();
        }
    }
}
