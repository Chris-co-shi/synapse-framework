package com.indigo.synapse.security.header;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TrustedHeaderCanonicalizerTest {

    private final TrustedHeaderCanonicalizer canonicalizer = new TrustedHeaderCanonicalizer();

    @Test
    void shouldCanonicalizeHeadersInStableOrderWithoutSignature() {
        Map<String, String> headers = new HashMap<>();
        headers.put(SecurityHeaders.SIGNATURE, "signature");
        headers.put(SecurityHeaders.USERNAME, " admin ");
        headers.put(SecurityHeaders.USER_ID, " 1 ");
        headers.put(SecurityHeaders.TIMESTAMP, "1780000000000");
        headers.put(SecurityHeaders.NONCE, "nonce-1");

        String canonical = canonicalizer.canonicalize(headers);

        assertEquals("""
                X-Synapse-User-Id=1
                X-Synapse-Username=admin
                X-Synapse-Tenant-Id=
                X-Synapse-Roles=
                X-Synapse-Permissions=
                X-Synapse-Trace-Id=
                X-Synapse-Request-Id=
                X-Synapse-Source=
                X-Synapse-Timestamp=1780000000000
                X-Synapse-Nonce=nonce-1
                """, canonical);
        assertFalse(canonical.contains(SecurityHeaders.SIGNATURE));
        assertFalse(canonical.contains("signature"));
    }
}
