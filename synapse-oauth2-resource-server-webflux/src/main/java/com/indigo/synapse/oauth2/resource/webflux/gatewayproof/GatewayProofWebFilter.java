package com.indigo.synapse.oauth2.resource.webflux.gatewayproof;

import com.indigo.synapse.security.autoconfigure.SynapseSecurityProperties;
import com.indigo.synapse.security.gatewayproof.GatewayProof;
import com.indigo.synapse.security.gatewayproof.GatewayProofCanonicalRequest;
import com.indigo.synapse.security.gatewayproof.GatewayProofHeaders;
import com.indigo.synapse.security.gatewayproof.GatewayProofTokenHasher;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerificationResult;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerifier;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Reactive GatewayProof 入站校验 WebFilter。
 *
 * <p>该 WebFilter 必须位于 OAuth2 Authentication 之前。它不阻塞 Reactor 线程，不使用 Servlet API，
 * 不读取 request body，不写 ThreadLocal；JWT 内容仍由后续 Reactive Resource Server 校验。</p>
 */
public final class GatewayProofWebFilter implements WebFilter {

    private final SynapseSecurityProperties.GatewayProof properties;
    private final GatewayProofVerifier verifier;
    private final GatewayProofTokenHasher tokenHasher;
    private final GatewayProofServerAccessDeniedHandler accessDeniedHandler;
    private final ServerBearerTokenAuthenticationConverter bearerTokenConverter =
            new ServerBearerTokenAuthenticationConverter();

    public GatewayProofWebFilter(
            SynapseSecurityProperties.GatewayProof properties,
            GatewayProofVerifier verifier,
            GatewayProofTokenHasher tokenHasher,
            GatewayProofServerAccessDeniedHandler accessDeniedHandler
    ) {
        this.properties = properties;
        this.verifier = verifier;
        this.tokenHasher = tokenHasher;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (shouldSkip(exchange)) {
            return chain.filter(exchange);
        }
        GatewayProof proof = proof(exchange);
        return bearerTokenConverter.convert(exchange)
                .map(authentication -> String.valueOf(authentication.getCredentials()))
                .onErrorReturn("")
                .defaultIfEmpty("")
                .flatMap(token -> {
                    GatewayProofVerificationResult result = verifier.verify(proof, canonicalRequest(exchange, proof, token));
                    return result.isSuccess() ? chain.filter(exchange) : accessDeniedHandler.handle(exchange, result);
                });
    }

    private boolean shouldSkip(ServerWebExchange exchange) {
        if (!properties.isEnabled() || !properties.isRequired()) {
            return true;
        }
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        return properties.getPermitPaths().stream().anyMatch(path::startsWith);
    }

    private GatewayProof proof(ServerWebExchange exchange) {
        try {
            return new GatewayProof(
                    exchange.getRequest().getHeaders().getFirst(GatewayProofHeaders.VERSION),
                    exchange.getRequest().getHeaders().getFirst(GatewayProofHeaders.GATEWAY_ID),
                    exchange.getRequest().getHeaders().getFirst(GatewayProofHeaders.TIMESTAMP),
                    exchange.getRequest().getHeaders().getFirst(GatewayProofHeaders.NONCE),
                    exchange.getRequest().getHeaders().getFirst(GatewayProofHeaders.SIGNATURE)
            );
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private GatewayProofCanonicalRequest canonicalRequest(ServerWebExchange exchange, GatewayProof proof, String token) {
        return new GatewayProofCanonicalRequest(
                proof == null ? null : proof.version(),
                proof == null ? null : proof.gatewayId(),
                proof == null ? null : proof.timestamp(),
                proof == null ? null : proof.nonce(),
                exchange.getRequest().getMethod().name(),
                exchange.getRequest().getURI().getRawPath(),
                exchange.getRequest().getURI().getRawQuery(),
                tokenHasher.sha256Hex(token)
        );
    }
}
