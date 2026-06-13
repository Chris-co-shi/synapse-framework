package com.indigo.synapse.message.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageContextHeadersTest {

    @Test
    void shouldDefineOperationContextHeaders() {
        assertEquals("x-synapse-trace-id", MessageContextHeaders.TRACE_ID);
        assertEquals("x-synapse-request-id", MessageContextHeaders.REQUEST_ID);
        assertEquals("x-synapse-tenant-id", MessageContextHeaders.TENANT_ID);
        assertEquals("x-synapse-actor-type", MessageContextHeaders.ACTOR_TYPE);
        assertEquals("x-synapse-initiator-type", MessageContextHeaders.INITIATOR_TYPE);
        assertEquals("x-synapse-source-entrypoint", MessageContextHeaders.SOURCE_ENTRYPOINT);
    }
}
