package com.indigo.synapse.security.gatewayproof;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GatewayProofTokenHasherTest {

    private final GatewayProofTokenHasher hasher = new GatewayProofTokenHasher();

    @Test
    void shouldHashBearerTokenAsLowerHexSha256() {
        assertEquals(
                "2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b",
                hasher.sha256Hex("secret")
        );
    }

    @Test
    void shouldReturnEmptyHashWhenBearerTokenIsMissing() {
        assertEquals("", hasher.sha256Hex(null));
        assertEquals("", hasher.sha256Hex(" "));
    }
}
