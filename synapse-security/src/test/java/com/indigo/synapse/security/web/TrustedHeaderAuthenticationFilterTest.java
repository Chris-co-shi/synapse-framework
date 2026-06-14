package com.indigo.synapse.security.web;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.security.autoconfigure.SynapseSecurityProperties;
import com.indigo.synapse.security.context.SecurityContext;
import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.security.header.SecurityHeaders;
import com.indigo.synapse.security.header.TrustedHeaderAuthenticatedUserResolver;
import com.indigo.synapse.security.header.TrustedHeaderSignatureVerifier;
import com.indigo.synapse.security.header.TrustedHeaderTimestampValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrustedHeaderAuthenticationFilterTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-14T10:00:00Z"), ZoneOffset.UTC);
    private static final String SECRET = "secret-value";

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
        OperationContextHolder.clear();
    }

    @Test
    void shouldPassThroughWhenTrustedHeaderDisabled() throws Exception {
        TrustedHeaderAuthenticationFilter filter = filter(properties(false, true, SECRET));
        PassThroughChain chain = new PassThroughChain();

        filter.doFilter(request(signedHeaders()), null, chain);

        assertTrue(chain.invoked);
        assertTrue(SecurityContext.currentUser().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldSetSecurityAndOperationContextForValidHeaders() throws Exception {
        TrustedHeaderAuthenticationFilter filter = filter(properties(true, true, SECRET));
        CapturingChain chain = new CapturingChain();

        filter.doFilter(request(signedHeaders()), null, chain);

        assertTrue(chain.invoked);
        assertEquals("1", chain.userId);
        assertEquals("admin", chain.username);
        assertEquals("tenant-a", chain.tenantId);
        assertEquals(OperationActorType.USER, chain.actorType);
        assertTrue(SecurityContext.currentUser().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldClearContextWhenChainThrows() {
        TrustedHeaderAuthenticationFilter filter = filter(properties(true, true, SECRET));
        FilterChain chain = (request, response) -> {
            throw new ServletException("failed");
        };

        assertThrows(ServletException.class, () -> filter.doFilter(request(signedHeaders()), null, chain));
        assertTrue(SecurityContext.currentUser().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldClearContextWhenAuthenticationFails() {
        TrustedHeaderAuthenticationFilter filter = filter(properties(true, true, SECRET));
        Map<String, String> headers = signedHeaders();
        headers.put(SecurityHeaders.SIGNATURE, "wrong");

        SynapseAuthenticationException exception = assertThrows(
                SynapseAuthenticationException.class,
                () -> filter.doFilter(request(headers), null, new CapturingChain())
        );

        assertEquals(CommonErrorCode.SECURITY_INVALID_SIGNATURE, exception.errorCode());
        assertTrue(SecurityContext.currentUser().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldRestorePreviousOperationContextAfterFilterEnds() throws Exception {
        TrustedHeaderAuthenticationFilter filter = filter(properties(true, true, SECRET));
        OperationContext jobContext = context(OperationActorType.JOB, "job-1");

        try (OperationContextScope ignored = OperationContextHolder.scope(jobContext)) {
            filter.doFilter(request(signedHeaders()), null, new CapturingChain());

            assertEquals(OperationActorType.JOB, OperationContextHolder.requireCurrent().actor().type());
            assertEquals("job-1", OperationContextHolder.requireCurrent().actor().id());
        }
    }

    @Test
    void shouldRejectExpiredTimestamp() {
        TrustedHeaderAuthenticationFilter filter = filter(properties(true, true, SECRET));
        Map<String, String> headers = baseHeaders();
        headers.put(SecurityHeaders.TIMESTAMP, Long.toString(CLOCK.instant().minus(Duration.ofMinutes(10)).toEpochMilli()));
        sign(headers);

        SynapseAuthenticationException exception = assertThrows(
                SynapseAuthenticationException.class,
                () -> filter.doFilter(request(headers), null, new CapturingChain())
        );

        assertEquals(CommonErrorCode.SECURITY_TRUSTED_HEADER_EXPIRED, exception.errorCode());
    }

    @Test
    void shouldRejectMissingRequiredHeader() {
        TrustedHeaderAuthenticationFilter filter = filter(properties(true, true, SECRET));
        Map<String, String> headers = signedHeaders();
        headers.remove(SecurityHeaders.USER_ID);
        sign(headers);

        SynapseAuthenticationException exception = assertThrows(
                SynapseAuthenticationException.class,
                () -> filter.doFilter(request(headers), null, new CapturingChain())
        );

        assertEquals(CommonErrorCode.SECURITY_INVALID_TRUSTED_HEADER, exception.errorCode());
    }

    @Test
    void shouldAllowMissingSecretWhenSignatureDisabled() throws Exception {
        SynapseSecurityProperties properties = properties(true, false, null);
        TrustedHeaderAuthenticationFilter filter = filter(properties);
        Map<String, String> headers = baseHeaders();
        CapturingChain chain = new CapturingChain();

        filter.doFilter(request(headers), null, chain);

        assertTrue(chain.invoked);
        assertEquals("1", chain.userId);
    }

    @Test
    void shouldReadHeaderNamesCaseInsensitively() throws Exception {
        TrustedHeaderAuthenticationFilter filter = filter(properties(true, true, SECRET));
        Map<String, String> headers = new HashMap<>();
        signedHeaders().forEach((key, value) -> headers.put(key.toLowerCase(Locale.ROOT), value));
        CapturingChain chain = new CapturingChain();

        filter.doFilter(request(headers), null, chain);

        assertEquals("1", chain.userId);
    }

    private static TrustedHeaderAuthenticationFilter filter(SynapseSecurityProperties properties) {
        return new TrustedHeaderAuthenticationFilter(
                properties,
                new TrustedHeaderAuthenticatedUserResolver(),
                new TrustedHeaderSignatureVerifier(),
                new TrustedHeaderTimestampValidator(),
                CLOCK
        );
    }

    private static SynapseSecurityProperties properties(boolean enabled, boolean signatureEnabled, String secret) {
        SynapseSecurityProperties properties = new SynapseSecurityProperties();
        SynapseSecurityProperties.TrustedHeader trustedHeader = properties.getTrustedHeader();
        trustedHeader.setEnabled(enabled);
        trustedHeader.setSignatureEnabled(signatureEnabled);
        trustedHeader.setSecret(secret);
        trustedHeader.setTimestampTolerance(Duration.ofMinutes(5));
        return properties;
    }

    private static Map<String, String> signedHeaders() {
        Map<String, String> headers = baseHeaders();
        sign(headers);
        return headers;
    }

    private static Map<String, String> baseHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(SecurityHeaders.USER_ID, " 1 ");
        headers.put(SecurityHeaders.USERNAME, " admin ");
        headers.put(SecurityHeaders.TENANT_ID, " tenant-a ");
        headers.put(SecurityHeaders.ROLES, "ADMIN");
        headers.put(SecurityHeaders.PERMISSIONS, "system:user:list");
        headers.put(SecurityHeaders.TIMESTAMP, Long.toString(CLOCK.instant().toEpochMilli()));
        headers.put(SecurityHeaders.NONCE, "nonce-1");
        return headers;
    }

    private static void sign(Map<String, String> headers) {
        TrustedHeaderSignatureVerifier verifier = new TrustedHeaderSignatureVerifier();
        headers.put(SecurityHeaders.SIGNATURE, verifier.sign(headers, SECRET));
    }

    private static HttpServletRequest request(Map<String, String> headers) {
        Map<String, String> copy = Map.copyOf(headers);
        return (HttpServletRequest) Proxy.newProxyInstance(
                TrustedHeaderAuthenticationFilterTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getHeader" -> copy.get((String) args[0]);
                    case "getHeaderNames" -> enumeration(copy.keySet());
                    default -> null;
                }
        );
    }

    private static Enumeration<String> enumeration(Iterable<String> values) {
        ArrayList<String> copy = new ArrayList<>();
        values.forEach(copy::add);
        return Collections.enumeration(copy);
    }

    private static OperationContext context(OperationActorType actorType, String actorId) {
        OperationActor actor = new OperationActor(actorType, actorId, actorId + "-name", "tenant-a", Map.of());
        return new OperationContext(
                actor,
                null,
                null,
                null,
                "tenant-a",
                null,
                CLOCK.instant(),
                Map.of()
        );
    }

    private static final class CapturingChain implements FilterChain {

        private boolean invoked;
        private String userId;
        private String username;
        private String tenantId;
        private OperationActorType actorType;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            invoked = true;
            userId = SecurityContext.currentUser().orElseThrow().userId();
            username = SecurityContext.currentUser().orElseThrow().username();
            tenantId = SecurityContext.currentUser().orElseThrow().tenantId();
            actorType = OperationContextHolder.requireCurrent().actor().type();
        }
    }

    private static final class PassThroughChain implements FilterChain {

        private boolean invoked;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            invoked = true;
        }
    }
}
