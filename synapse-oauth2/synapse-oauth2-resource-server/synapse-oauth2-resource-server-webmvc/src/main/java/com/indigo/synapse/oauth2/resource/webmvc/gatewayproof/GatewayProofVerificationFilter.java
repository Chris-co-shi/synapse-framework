package com.indigo.synapse.oauth2.resource.webmvc.gatewayproof;

import com.indigo.synapse.security.autoconfigure.SynapseSecurityProperties;
import com.indigo.synapse.security.gatewayproof.GatewayProof;
import com.indigo.synapse.security.gatewayproof.GatewayProofCanonicalRequest;
import com.indigo.synapse.security.gatewayproof.GatewayProofHeaders;
import com.indigo.synapse.security.gatewayproof.GatewayProofTokenHasher;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerificationResult;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet GatewayProof 入站校验 Filter。
 *
 * <p>该 Filter 必须位于 BearerTokenAuthenticationFilter 之前，用于证明请求经过可信 Gateway。
 * 它不解析 JWT claims，不建立认证主体，不写 SecurityContext。JWT 仍由后续 Resource Server Filter 校验。</p>
 */
public final class GatewayProofVerificationFilter extends OncePerRequestFilter {

    private final SynapseSecurityProperties.GatewayProof properties;
    private final GatewayProofVerifier verifier;
    private final GatewayProofTokenHasher tokenHasher;
    private final GatewayProofAccessDeniedHandler accessDeniedHandler;
    private final DefaultBearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();

    public GatewayProofVerificationFilter(
            SynapseSecurityProperties.GatewayProof properties,
            GatewayProofVerifier verifier,
            GatewayProofTokenHasher tokenHasher,
            GatewayProofAccessDeniedHandler accessDeniedHandler
    ) {
        this.properties = properties;
        this.verifier = verifier;
        this.tokenHasher = tokenHasher;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.isEnabled() || !properties.isRequired()) {
            return true;
        }
        String path = request.getRequestURI();
        return properties.getPermitPaths().stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        GatewayProof proof = proof(request);
        GatewayProofCanonicalRequest canonicalRequest = canonicalRequest(request, proof);
        GatewayProofVerificationResult result = verifier.verify(proof, canonicalRequest);
        if (!result.isSuccess()) {
            accessDeniedHandler.handle(response, result);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private GatewayProof proof(HttpServletRequest request) {
        try {
            return new GatewayProof(
                    request.getHeader(GatewayProofHeaders.VERSION),
                    request.getHeader(GatewayProofHeaders.GATEWAY_ID),
                    request.getHeader(GatewayProofHeaders.TIMESTAMP),
                    request.getHeader(GatewayProofHeaders.NONCE),
                    request.getHeader(GatewayProofHeaders.SIGNATURE)
            );
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private GatewayProofCanonicalRequest canonicalRequest(HttpServletRequest request, GatewayProof proof) {
        String token = bearerTokenResolver.resolve(request);
        return new GatewayProofCanonicalRequest(
                proof == null ? null : proof.version(),
                proof == null ? null : proof.gatewayId(),
                proof == null ? null : proof.timestamp(),
                proof == null ? null : proof.nonce(),
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                tokenHasher.sha256Hex(token)
        );
    }
}
