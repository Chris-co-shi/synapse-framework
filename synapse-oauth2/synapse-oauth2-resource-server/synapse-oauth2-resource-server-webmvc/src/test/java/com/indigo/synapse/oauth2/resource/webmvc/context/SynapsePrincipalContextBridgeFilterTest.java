package com.indigo.synapse.oauth2.resource.webmvc.context;

import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextPropagationKeys;
import com.indigo.synapse.oauth2.resource.webmvc.jwt.SynapseJwtAuthenticationToken;
import com.indigo.synapse.oauth2.resource.webmvc.jwt.TokenMetadata;
import com.indigo.synapse.security.context.AuthenticatedUser;
import com.indigo.synapse.security.context.CurrentPrincipalContext;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapsePrincipalContextBridgeFilterTest {

    private final SynapsePrincipalContextBridgeFilter filter =
            new SynapsePrincipalContextBridgeFilter();

    @AfterEach
    void clearSpringSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldClearPrincipalAfterServletRequestCompletes() throws Exception {
        AuthenticatedUser user = user("user-a");
        SecurityContextHolder.getContext().setAuthentication(authentication(user));

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (request, response) -> {
                    assertEquals(
                            user,
                            CurrentPrincipalContext.currentPrincipal().orElseThrow()
                    );
                    assertEquals(
                            "user-a",
                            OperationContextHolder.requireCurrent().actor().id()
                    );
                }
        );

        assertContextsCleared();
    }

    @Test
    void shouldClearPrincipalWhenServletChainThrows() {
        AuthenticatedUser user = user("user-a");
        SecurityContextHolder.getContext().setAuthentication(authentication(user));

        assertThrows(ServletException.class, () -> filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (request, response) -> {
                    assertEquals(
                            user,
                            CurrentPrincipalContext.currentPrincipal().orElseThrow()
                    );
                    throw new ServletException("request failed");
                }
        ));

        assertContextsCleared();
    }

    @Test
    void shouldNotLeakPrincipalIntoNextRequestOnSameThread() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(authentication(user("user-a")));
        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (request, response) -> assertEquals(
                        "user-a",
                        CurrentPrincipalContext.currentPrincipal()
                                .orElseThrow()
                                .principalId()
                )
        );

        SecurityContextHolder.clearContext();
        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (request, response) -> assertTrue(
                        CurrentPrincipalContext.currentPrincipal().isEmpty()
                )
        );

        assertContextsCleared();
    }

    @Test
    void shouldIgnoreForgedIdentityHeadersAndKeepAuthenticatedPrincipal() throws Exception {
        AuthenticatedUser user = user("verified-user");
        SecurityContextHolder.getContext().setAuthentication(authentication(user));
        MockHttpServletRequest request = forgedIdentityRequest();

        filter.doFilter(request, new MockHttpServletResponse(), (servletRequest, response) -> {
            var context = OperationContextHolder.requireCurrent();
            assertEquals("verified-user", context.actor().id());
            assertEquals("tenant-a", context.tenantId());
            assertEquals(context.actor(), context.initiator());
        });

        assertContextsCleared();
    }

    @Test
    void shouldNotEstablishPrincipalFromHeadersForUnauthenticatedRequest() throws Exception {
        filter.doFilter(forgedIdentityRequest(), new MockHttpServletResponse(), (request, response) -> {
            assertTrue(CurrentPrincipalContext.currentPrincipal().isEmpty());
            assertTrue(OperationContextHolder.current().isEmpty());
        });

        assertContextsCleared();
    }

    private static SynapseJwtAuthenticationToken authentication(AuthenticatedUser user) {
        Instant issuedAt = Instant.parse("2026-06-19T00:00:00Z");
        Jwt jwt = Jwt.withTokenValue("token-" + user.userId())
                .header("alg", "none")
                .subject(user.userId())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(300))
                .build();
        return new SynapseJwtAuthenticationToken(
                jwt,
                List.of(),
                user,
                new TokenMetadata("jti-" + user.userId(), "issuer")
        );
    }

    private static AuthenticatedUser user(String userId) {
        return new AuthenticatedUser(
                userId,
                userId,
                "tenant-a",
                Set.of("USER"),
                Set.of("resource:read")
        );
    }

    private static MockHttpServletRequest forgedIdentityRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        request.addHeader(OperationContextPropagationKeys.ACTOR_TYPE, "USER");
        request.addHeader(OperationContextPropagationKeys.ACTOR_ID, "forged-user");
        request.addHeader(OperationContextPropagationKeys.TENANT_ID, "forged-tenant");
        request.addHeader(OperationContextPropagationKeys.INITIATOR_TYPE, "SERVICE");
        request.addHeader(OperationContextPropagationKeys.INITIATOR_ID, "forged-initiator");
        return request;
    }

    private static void assertContextsCleared() {
        assertTrue(CurrentPrincipalContext.currentPrincipal().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }
}
