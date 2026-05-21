package com.indigo.synapse.security.jwk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityKeyPolicyTest {

    @Test
    void shouldDetectDefaultKeyIds() {
        assertTrue(SecurityKeyPolicy.isDefaultKeyId("synapse-default"));
        assertTrue(SecurityKeyPolicy.isDefaultKeyId(" DEV "));
        assertFalse(SecurityKeyPolicy.isDefaultKeyId("kid-20260520"));
    }

    @Test
    void shouldRejectDefaultKeyIdInProduction() {
        SecurityKeyPolicy.validateSigningKeyId("synapse-default", false);

        assertThrows(IllegalStateException.class, () -> SecurityKeyPolicy.validateSigningKeyId("synapse-default", true));
        assertThrows(IllegalArgumentException.class, () -> SecurityKeyPolicy.validateSigningKeyId("", true));
    }
}
