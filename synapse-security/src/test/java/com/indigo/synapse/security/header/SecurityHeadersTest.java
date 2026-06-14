package com.indigo.synapse.security.header;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityHeadersTest {

    @Test
    void shouldExposeStableHeaderNames() {
        assertEquals("X-Synapse-User-Id", SecurityHeaders.USER_ID);
        assertEquals("X-Synapse-Username", SecurityHeaders.USERNAME);
        assertEquals("X-Synapse-Tenant-Id", SecurityHeaders.TENANT_ID);
        assertEquals("X-Synapse-Roles", SecurityHeaders.ROLES);
        assertEquals("X-Synapse-Permissions", SecurityHeaders.PERMISSIONS);
        assertEquals("X-Synapse-Trace-Id", SecurityHeaders.TRACE_ID);
        assertEquals("X-Synapse-Request-Id", SecurityHeaders.REQUEST_ID);
        assertEquals("X-Synapse-Source", SecurityHeaders.SOURCE);
        assertEquals("X-Synapse-Signature", SecurityHeaders.SIGNATURE);
        assertEquals("X-Synapse-Timestamp", SecurityHeaders.TIMESTAMP);
        assertEquals("X-Synapse-Nonce", SecurityHeaders.NONCE);
    }
}
