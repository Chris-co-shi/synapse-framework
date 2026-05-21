package com.indigo.synapse.security.jwk;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwkKeyDescriptorTest {

    @Test
    void shouldDescribeJwkKeyAndCheckExpiration() {
        Instant createdAt = Instant.parse("2026-05-20T10:00:00Z");
        JwkKeyDescriptor descriptor = new JwkKeyDescriptor(
                "kid-20260520",
                "RS256",
                JwkKeyDescriptor.SIGNATURE_USE,
                createdAt,
                createdAt.plusSeconds(3600)
        );

        assertFalse(descriptor.isExpired(createdAt.plusSeconds(3599)));
        assertTrue(descriptor.isExpired(createdAt.plusSeconds(3600)));
    }

    @Test
    void shouldRejectInvalidJwkDescriptor() {
        Instant now = Instant.parse("2026-05-20T10:00:00Z");

        assertThrows(IllegalArgumentException.class, () -> new JwkKeyDescriptor("", "RS256", "sig", now, null));
        assertThrows(IllegalArgumentException.class, () -> new JwkKeyDescriptor("kid", "", "sig", now, null));
        assertThrows(IllegalArgumentException.class, () -> new JwkKeyDescriptor("kid", "RS256", "", now, null));
        assertThrows(IllegalArgumentException.class, () -> new JwkKeyDescriptor("kid", "RS256", "sig", now, now));
    }
}
