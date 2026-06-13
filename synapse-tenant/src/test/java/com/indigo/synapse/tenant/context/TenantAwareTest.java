package com.indigo.synapse.tenant.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TenantAwareTest {

    @Test
    void shouldExposeTenantIdContract() {
        TenantAware tenantAware = () -> "tenant-a";

        assertEquals("tenant-a", tenantAware.tenantId());
    }
}
