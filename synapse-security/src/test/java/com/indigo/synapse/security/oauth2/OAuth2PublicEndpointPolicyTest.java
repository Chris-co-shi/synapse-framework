package com.indigo.synapse.security.oauth2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OAuth2PublicEndpointPolicyTest {

    @Test
    void shouldAllowOnlyOAuth2AndJwkDiscoveryEndpoints() {
        assertTrue(OAuth2PublicEndpointPolicy.isPublic("/oauth2/authorize"));
        assertTrue(OAuth2PublicEndpointPolicy.isPublic(".well-known/jwks.json"));

        assertFalse(OAuth2PublicEndpointPolicy.isPublic("/api/admin/users"));
        assertFalse(OAuth2PublicEndpointPolicy.isPublic("/openapi/index.html"));
        assertFalse(OAuth2PublicEndpointPolicy.isPublic(null));
    }
}
