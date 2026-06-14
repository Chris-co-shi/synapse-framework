package com.indigo.synapse.security.header;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrustedHeaderSignatureVerifierTest {

    private final TrustedHeaderSignatureVerifier verifier = new TrustedHeaderSignatureVerifier();

    @Test
    void shouldVerifyCorrectSignature() {
        Map<String, String> headers = headers();
        headers.put(SecurityHeaders.SIGNATURE, verifier.sign(headers, "secret-value"));

        assertTrue(verifier.verify(headers, "secret-value"));
    }

    @Test
    void shouldRejectWrongOrMissingSignature() {
        Map<String, String> headers = headers();
        headers.put(SecurityHeaders.SIGNATURE, "wrong");

        assertFalse(verifier.verify(headers, "secret-value"));
        headers.remove(SecurityHeaders.SIGNATURE);
        assertFalse(verifier.verify(headers, "secret-value"));
    }

    @Test
    void shouldRejectBlankSecret() {
        Map<String, String> headers = headers();

        assertThrows(IllegalArgumentException.class, () -> verifier.sign(headers, " "));
        assertThrows(IllegalArgumentException.class, () -> verifier.verify(headers, " "));
    }

    private static Map<String, String> headers() {
        Map<String, String> headers = new HashMap<>();
        headers.put(SecurityHeaders.USER_ID, "1");
        headers.put(SecurityHeaders.USERNAME, "admin");
        headers.put(SecurityHeaders.TIMESTAMP, "1780000000000");
        headers.put(SecurityHeaders.NONCE, "nonce-1");
        return headers;
    }
}
