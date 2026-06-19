package com.indigo.synapse.security.gatewayproof;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GatewayProofCanonicalizerTest {

    private final GatewayProofCanonicalizer canonicalizer = new GatewayProofCanonicalizer();

    @Test
    void shouldBuildCanonicalStringWithNormalizedQuery() {
        GatewayProofCanonicalRequest request = new GatewayProofCanonicalRequest(
                "v1",
                "synapse-gateway",
                "1700000000000",
                "nonce-1",
                "post",
                "/api/items",
                "b=2&a=3&a=1&space=hello%20world&plus=a+b&empty",
                "abc123"
        );

        String canonical = canonicalizer.canonicalize(request);

        assertEquals("""
                v1
                synapse-gateway
                1700000000000
                nonce-1
                POST
                /api/items
                a=1&a=3&b=2&empty=&plus=a%2Bb&space=hello%20world
                abc123""", canonical);
    }

    @Test
    void shouldUseSlashForEmptyPathAndEmptyQuery() {
        GatewayProofCanonicalRequest request = new GatewayProofCanonicalRequest(
                "v1",
                "synapse-gateway",
                "1700000000000",
                "nonce-1",
                "GET",
                "",
                "",
                ""
        );

        String canonical = canonicalizer.canonicalize(request);

        assertEquals("""
                v1
                synapse-gateway
                1700000000000
                nonce-1
                GET
                /
                
                """, canonical);
    }
}
