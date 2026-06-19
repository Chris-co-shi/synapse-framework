package com.indigo.synapse.oauth2.resource.webmvc.gatewayproof;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.security.autoconfigure.SynapseSecurityProperties;
import com.indigo.synapse.security.gatewayproof.GatewayProof;
import com.indigo.synapse.security.gatewayproof.GatewayProofCanonicalRequest;
import com.indigo.synapse.security.gatewayproof.GatewayProofHeaders;
import com.indigo.synapse.security.gatewayproof.GatewayProofTokenHasher;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerificationResult;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerificationStatus;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerifier;
import com.indigo.synapse.webmvc.exception.CommonErrorHttpStatusResolver;
import com.indigo.synapse.webmvc.exception.CompositeErrorHttpStatusResolver;
import com.indigo.synapse.webmvc.exception.WebErrorResponseWriter;
import com.indigo.synapse.webmvc.exception.WebExceptionResponseFactory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayProofVerificationFilterTest {

    @Test
    void shouldContinueWhenGatewayProofIsValid() throws Exception {
        RecordingVerifier verifier = new RecordingVerifier(GatewayProofVerificationResult.success());
        GatewayProofVerificationFilter filter = filter(verifier, properties(true, true));
        MockHttpServletRequest request = request("/api/items", "b=2&a=1");
        request.addHeader("Authorization", "Bearer token-a");
        addProofHeaders(request);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(verifier.request.method()).isEqualTo("GET");
        assertThat(verifier.request.path()).isEqualTo("/api/items");
        assertThat(verifier.request.query()).isEqualTo("b=2&a=1");
        assertThat(verifier.request.bearerTokenHash()).isEqualTo(new GatewayProofTokenHasher().sha256Hex("token-a"));
    }

    @Test
    void shouldRejectWhenGatewayProofIsMissing() throws Exception {
        GatewayProofVerificationFilter filter = filter(
                new RecordingVerifier(GatewayProofVerificationResult.failure(GatewayProofVerificationStatus.MISSING, "missing")),
                properties(true, true)
        );
        MockHttpServletRequest request = request("/api/items", "");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void shouldSkipPermitPath() throws Exception {
        RecordingVerifier verifier = new RecordingVerifier(GatewayProofVerificationResult.failure(
                GatewayProofVerificationStatus.MISSING, "missing"
        ));
        GatewayProofVerificationFilter filter = filter(verifier, properties(true, true));
        MockHttpServletRequest request = request("/actuator/health", "");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(verifier.proof).isNull();
    }

    @Test
    void shouldSkipWhenGatewayProofIsNotRequired() throws Exception {
        RecordingVerifier verifier = new RecordingVerifier(GatewayProofVerificationResult.failure(
                GatewayProofVerificationStatus.MISSING, "missing"
        ));
        GatewayProofVerificationFilter filter = filter(verifier, properties(true, false));
        MockHttpServletRequest request = request("/api/items", "");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(verifier.proof).isNull();
    }

    private static GatewayProofVerificationFilter filter(
            GatewayProofVerifier verifier,
            SynapseSecurityProperties.GatewayProof properties
    ) {
        return new GatewayProofVerificationFilter(
                properties,
                verifier,
                new GatewayProofTokenHasher(),
                new GatewayProofAccessDeniedHandler(
                        new WebExceptionResponseFactory(new CompositeErrorHttpStatusResolver(
                                List.of(new CommonErrorHttpStatusResolver())
                        )),
                        new WebErrorResponseWriter(new ObjectMapper().findAndRegisterModules())
                )
        );
    }

    private static SynapseSecurityProperties.GatewayProof properties(boolean enabled, boolean required) {
        SynapseSecurityProperties.GatewayProof properties = new SynapseSecurityProperties.GatewayProof();
        properties.setEnabled(enabled);
        properties.setRequired(required);
        return properties;
    }

    private static MockHttpServletRequest request(String path, String query) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setQueryString(query);
        return request;
    }

    private static void addProofHeaders(MockHttpServletRequest request) {
        request.addHeader(GatewayProofHeaders.VERSION, "v1");
        request.addHeader(GatewayProofHeaders.GATEWAY_ID, "synapse-gateway");
        request.addHeader(GatewayProofHeaders.TIMESTAMP, "1700000000000");
        request.addHeader(GatewayProofHeaders.NONCE, "nonce-1");
        request.addHeader(GatewayProofHeaders.SIGNATURE, "signature");
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
}
